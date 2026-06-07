package com.example.arbor.service;

import com.example.arbor.dto.request.AlterarSenhaRequestDTO;
import com.example.arbor.dto.request.AtualizarPerfilRequestDTO;
import com.example.arbor.dto.request.AtualizarUsuarioAdminRequestDTO;
import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.PageResponseDTO;
import com.example.arbor.dto.response.UsuarioPerfilResponseDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.exception.AcessoNegadoException;
import com.example.arbor.exception.ConflitoException;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    void listarTodosDeveRetornarPaginaParaGestor() {
        Usuario executor = usuario(Perfil.GESTOR);
        Usuario usuario = usuario(Perfil.PESQUISADOR);

        when(repository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(usuario)));

        PageResponseDTO<UsuarioResponseDTO> response = service.listarTodos(
                Perfil.PESQUISADOR,
                true,
                "pesquisador",
                0,
                20,
                executor);

        assertThat(response.items()).hasSize(1);
        assertThat(response.page()).isZero();
        assertThat(response.limit()).isEqualTo(20);
        assertThat(response.total()).isEqualTo(1);
        assertThat(response.items().getFirst().perfilAcesso()).isEqualTo(Perfil.PESQUISADOR);
    }

    @Test
    void listarTodosDeveRetornarPaginaParaAdministrador() {
        Usuario executor = usuario(Perfil.ADMINISTRADOR);
        Usuario usuario = usuario(Perfil.PESQUISADOR);

        when(repository.findAll(anySpecification(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(usuario)));

        PageResponseDTO<UsuarioResponseDTO> response = service.listarTodos(
                null, null, null, 0, 20, executor);

        assertThat(response.items()).hasSize(1);
    }

    @Test
    void listarTodosDeveBloquearPerfilSemPermissaoAdministrativa() {
        Usuario executor = usuario(Perfil.PESQUISADOR);

        assertThatThrownBy(() -> service.listarTodos(null, null, null, 0, 20, executor))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessage("Voce nao tem permissao para gerenciar usuarios.");
    }

    @Test
    void buscarMeuPerfilDeveRetornarUsuarioAutenticado() {
        Usuario usuario = usuario(Perfil.PESQUISADOR);
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
        Usuario usuario = usuario(Perfil.PESQUISADOR);
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
        Usuario usuario = usuario(Perfil.PESQUISADOR);
        AtualizarPerfilRequestDTO dto = new AtualizarPerfilRequestDTO(null, "existente@arbor.local");

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(repository.existsByEmailAndIdNot("existente@arbor.local", usuario.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.atualizarMeuPerfil(usuario, dto))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("E-mail ja cadastrado.");
    }

    @Test
    void atualizarMeuPerfilDeveExigirAoMenosUmCampo() {
        Usuario usuario = usuario(Perfil.PESQUISADOR);
        AtualizarPerfilRequestDTO dto = new AtualizarPerfilRequestDTO(null, null);

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.atualizarMeuPerfil(usuario, dto))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("Informe ao menos um campo para atualizacao.");
    }

    @Test
    void atualizarUsuarioDeveAlterarDadosAdministrativosEInvalidarRefreshQuandoRoleMudar() {
        Usuario executor = usuario(Perfil.ADMINISTRADOR);
        Usuario usuario = usuario(Perfil.PESQUISADOR);
        AtualizarUsuarioAdminRequestDTO dto = new AtualizarUsuarioAdminRequestDTO(
                "Gestor Interno",
                "gestor-interno@arbor.local",
                Perfil.GESTOR,
                false);

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(repository.existsByEmailAndIdNot("gestor-interno@arbor.local", usuario.getId())).thenReturn(false);
        when(repository.save(usuario)).thenReturn(usuario);

        UsuarioResponseDTO response = service.atualizarUsuario(usuario.getId(), dto, executor);

        assertThat(response.nome()).isEqualTo("Gestor Interno");
        assertThat(response.email()).isEqualTo("gestor-interno@arbor.local");
        assertThat(response.perfilAcesso()).isEqualTo(Perfil.GESTOR);
        assertThat(response.ativo()).isFalse();
        assertThat(usuario.getRefreshTokenVersion()).isEqualTo(3);
        verify(repository).save(usuario);
    }

    @Test
    void administradorDevePoderCriarGestor() {
        Usuario executor = usuario(Perfil.ADMINISTRADOR);
        UsuarioRequestDTO dto = novoUsuario(Perfil.GESTOR);
        when(repository.existsByEmail(dto.email())).thenReturn(false);
        when(passwordEncoder.encode(dto.senha())).thenReturn("senha-criptografada");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponseDTO response = service.salvar(dto, executor);

        assertThat(response.perfilAcesso()).isEqualTo(Perfil.GESTOR);
    }

    @Test
    void gestorDevePoderCriarPesquisador() {
        Usuario executor = usuario(Perfil.GESTOR);
        UsuarioRequestDTO dto = novoUsuario(Perfil.PESQUISADOR);
        when(repository.existsByEmail(dto.email())).thenReturn(false);
        when(passwordEncoder.encode(dto.senha())).thenReturn("senha-criptografada");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponseDTO response = service.salvar(dto, executor);

        assertThat(response.perfilAcesso()).isEqualTo(Perfil.PESQUISADOR);
    }

    @Test
    void gestorNaoDevePoderCriarOutroGestor() {
        Usuario executor = usuario(Perfil.GESTOR);
        UsuarioRequestDTO dto = novoUsuario(Perfil.GESTOR);

        assertThatThrownBy(() -> service.salvar(dto, executor))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessage("Apenas administradores podem criar usuarios ou atribuir perfis administrativos.");
    }

    @Test
    void gestorNaoDevePoderCriarAdministrador() {
        Usuario executor = usuario(Perfil.GESTOR);
        UsuarioRequestDTO dto = novoUsuario(Perfil.ADMINISTRADOR);

        assertThatThrownBy(() -> service.salvar(dto, executor))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessage("Apenas administradores podem criar usuarios ou atribuir perfis administrativos.");
    }

    @Test
    void gestorNaoDevePoderPromoverUsuarioParaGestor() {
        Usuario executor = usuario(Perfil.GESTOR);
        Usuario usuario = usuario(Perfil.PESQUISADOR);
        AtualizarUsuarioAdminRequestDTO dto = new AtualizarUsuarioAdminRequestDTO(
                null, null, Perfil.GESTOR, null);
        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.atualizarUsuario(usuario.getId(), dto, executor))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessage("Apenas administradores podem criar usuarios ou atribuir perfis administrativos.");
    }

    @Test
    void alterarMinhaSenhaDeveValidarSenhaAtualEInvalidarRefreshTokens() {
        Usuario usuario = usuario(Perfil.PESQUISADOR);
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
        Usuario usuario = usuario(Perfil.PESQUISADOR);
        AlterarSenhaRequestDTO dto = new AlterarSenhaRequestDTO("SenhaErrada1", "NovaSenha1", "NovaSenha1");

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("SenhaErrada1", usuario.getSenha())).thenReturn(false);

        assertThatThrownBy(() -> service.alterarMinhaSenha(usuario, dto))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("Senha atual invalida.");
    }

    @Test
    void alterarMinhaSenhaDeveValidarConfirmacao() {
        Usuario usuario = usuario(Perfil.PESQUISADOR);
        AlterarSenhaRequestDTO dto = new AlterarSenhaRequestDTO("SenhaAtual1", "NovaSenha1", "OutraSenha1");

        when(repository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("SenhaAtual1", usuario.getSenha())).thenReturn(true);

        assertThatThrownBy(() -> service.alterarMinhaSenha(usuario, dto))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("As senhas nao coincidem.");
    }

    private Usuario usuario(Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.fromString("2a7be8b4-96e2-43ab-93d7-d2fbc9ee5041"));
        usuario.setNome("Pesquisador Arbor");
        usuario.setEmail("pesquisador@arbor.local");
        usuario.setSenha("senha-atual-criptografada");
        usuario.setPerfilAcesso(perfil);
        usuario.setRefreshTokenVersion(2);
        usuario.setAtivo(true);
        return usuario;
    }

    private UsuarioRequestDTO novoUsuario(Perfil perfil) {
        return new UsuarioRequestDTO(
                "Novo Usuario",
                "novo@arbor.local",
                "Senha123",
                perfil);
    }

    @SuppressWarnings("unchecked")
    private Specification<Usuario> anySpecification() {
        return any(Specification.class);
    }
}
