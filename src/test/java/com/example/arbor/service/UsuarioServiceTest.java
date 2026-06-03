package com.example.arbor.service;

import com.example.arbor.dto.request.AlterarSenhaRequestDTO;
import com.example.arbor.dto.request.AtualizarPerfilRequestDTO;
import com.example.arbor.dto.response.UsuarioPerfilResponseDTO;
import com.example.arbor.exception.ConflitoException;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.repository.TokenRecuperacaoRepository;
import com.example.arbor.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenRecuperacaoRepository tokenRecuperacaoRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioService service;

    @Test
    void buscarMeuPerfilDeveRetornarUsuarioAutenticado() {
        Usuario usuario = usuario();
        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        UsuarioPerfilResponseDTO response = service.buscarMeuPerfil(usuario);

        assertThat(response.id()).isEqualTo(usuario.getId());
        assertThat(response.nome()).isEqualTo(usuario.getNome());
        assertThat(response.email()).isEqualTo(usuario.getEmail());
        assertThat(response.perfilAcesso()).isEqualTo(usuario.getPerfilAcesso());
        assertThat(response.ativo()).isTrue();
    }

    @Test
    void atualizarMeuPerfilDeveAlterarNomeEEmailSemAlterarPerfil() {
        Usuario usuario = usuario();
        AtualizarPerfilRequestDTO dto = new AtualizarPerfilRequestDTO(" Novo Nome ", "novo@arbor.local");

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(repository.existsByEmailAndIdNot("novo@arbor.local", usuario.getId())).thenReturn(false);
        when(repository.save(usuario)).thenReturn(usuario);

        UsuarioPerfilResponseDTO response = service.atualizarMeuPerfil(usuario, dto);

        assertThat(response.nome()).isEqualTo("Novo Nome");
        assertThat(response.email()).isEqualTo("novo@arbor.local");
        assertThat(response.perfilAcesso()).isEqualTo(Perfil.PESQUISADOR);
        verify(repository).save(usuario);
    }

    @Test
    void atualizarMeuPerfilDeveBloquearEmailDuplicado() {
        Usuario usuario = usuario();
        AtualizarPerfilRequestDTO dto = new AtualizarPerfilRequestDTO(null, "existente@arbor.local");

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(repository.existsByEmailAndIdNot("existente@arbor.local", usuario.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.atualizarMeuPerfil(usuario, dto))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("E-mail já cadastrado.");
    }

    @Test
    void atualizarMeuPerfilDeveExigirAoMenosUmCampo() {
        Usuario usuario = usuario();
        AtualizarPerfilRequestDTO dto = new AtualizarPerfilRequestDTO(null, null);

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.atualizarMeuPerfil(usuario, dto))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("Informe ao menos um campo para atualização.");
    }

    @Test
    void alterarMinhaSenhaDeveValidarSenhaAtualEInvalidarRefreshTokens() {
        Usuario usuario = usuario();
        AlterarSenhaRequestDTO dto = new AlterarSenhaRequestDTO("SenhaAtual1", "NovaSenha1", "NovaSenha1");

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("SenhaAtual1", usuario.getSenha())).thenReturn(true);
        when(passwordEncoder.encode("NovaSenha1")).thenReturn("nova-senha-criptografada");

        service.alterarMinhaSenha(usuario, dto);

        assertThat(usuario.getSenha()).isEqualTo("nova-senha-criptografada");
        assertThat(usuario.getRefreshTokenVersion()).isEqualTo(3);
        verify(repository).save(usuario);
    }

    @Test
    void alterarMinhaSenhaDeveBloquearSenhaAtualInvalida() {
        Usuario usuario = usuario();
        AlterarSenhaRequestDTO dto = new AlterarSenhaRequestDTO("SenhaErrada1", "NovaSenha1", "NovaSenha1");

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("SenhaErrada1", usuario.getSenha())).thenReturn(false);

        assertThatThrownBy(() -> service.alterarMinhaSenha(usuario, dto))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("Senha atual inválida.");
    }

    @Test
    void alterarMinhaSenhaDeveValidarConfirmacao() {
        Usuario usuario = usuario();
        AlterarSenhaRequestDTO dto = new AlterarSenhaRequestDTO("SenhaAtual1", "NovaSenha1", "OutraSenha1");

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("SenhaAtual1", usuario.getSenha())).thenReturn(true);

        assertThatThrownBy(() -> service.alterarMinhaSenha(usuario, dto))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("As senhas não coincidem.");
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.fromString("2a7be8b4-96e2-43ab-93d7-d2fbc9ee5041"));
        usuario.setNome("Pesquisador Arbor");
        usuario.setEmail("pesquisador@arbor.local");
        usuario.setSenha("senha-atual-criptografada");
        usuario.setPerfilAcesso(Perfil.PESQUISADOR);
        usuario.setRefreshTokenVersion(2);
        usuario.setAtivo(true);
        return usuario;
    }
}
