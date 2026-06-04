package com.example.arbor.service;

import com.example.arbor.dto.request.RedefinirSenhaDTO;
import com.example.arbor.dto.request.SolicitarRecuperacaoDTO;
import com.example.arbor.dto.request.VerificarCodigoDTO;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.model.TokenRecuperacao;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.repository.TokenRecuperacaoRepository;
import com.example.arbor.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenRecuperacaoRepository tokenRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RecuperacaoSenhaService service;

    @Test
    void solicitarRecuperacaoNaoDeveRevelarEmailInexistente() {
        when(usuarioRepository.findByEmail("inexistente@arbor.local")).thenReturn(Optional.empty());

        service.solicitarRecuperacao(new SolicitarRecuperacaoDTO("inexistente@arbor.local"));

        verifyNoInteractions(tokenRepository, mailSender);
    }

    @Test
    void solicitarRecuperacaoDeveGerarTokenEEnviarEmailParaUsuarioExistente() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        service.solicitarRecuperacao(new SolicitarRecuperacaoDTO(usuario.getEmail()));

        ArgumentCaptor<TokenRecuperacao> tokenCaptor = ArgumentCaptor.forClass(TokenRecuperacao.class);
        ArgumentCaptor<SimpleMailMessage> emailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(tokenRepository).deleteByUsuario(usuario);
        verify(tokenRepository).flush();
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(mailSender).send(emailCaptor.capture());

        TokenRecuperacao token = tokenCaptor.getValue();
        assertThat(token.getCodigo()).hasSize(6);
        assertThat(token.getUsuario()).isEqualTo(usuario);
        assertThat(token.isUtilizado()).isFalse();
        assertThat(token.getExpiracao()).isAfter(LocalDateTime.now());
        assertThat(emailCaptor.getValue().getTo()).containsExactly(usuario.getEmail());
    }

    @Test
    void verificarCodigoDeveValidarTokenVinculadoAoEmail() {
        TokenRecuperacao token = tokenValido();
        when(tokenRepository.findByCodigoAndUsuario_EmailAndUtilizadoFalse("123456", "usuario@arbor.local"))
                .thenReturn(Optional.of(token));

        service.verificarCodigo(new VerificarCodigoDTO("123456", "usuario@arbor.local"));

        verify(tokenRepository).findByCodigoAndUsuario_EmailAndUtilizadoFalse("123456", "usuario@arbor.local");
    }

    @Test
    void verificarCodigoDeveBloquearTokenExpirado() {
        TokenRecuperacao token = tokenValido();
        token.setExpiracao(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByCodigoAndUsuario_EmailAndUtilizadoFalse("123456", "usuario@arbor.local"))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.verificarCodigo(new VerificarCodigoDTO("123456", "usuario@arbor.local")))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("Codigo expirado. Solicite um novo codigo.");
    }

    @Test
    void redefinirSenhaDeveAtualizarSenhaMarcarTokenComoUsadoEInvalidarRefreshTokens() {
        TokenRecuperacao token = tokenValido();
        Usuario usuario = token.getUsuario();
        RedefinirSenhaDTO dto = new RedefinirSenhaDTO(
                "123456",
                usuario.getEmail(),
                "NovaSenha1",
                "NovaSenha1");

        when(tokenRepository.findByCodigoAndUsuario_EmailAndUtilizadoFalse("123456", usuario.getEmail()))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NovaSenha1")).thenReturn("senha-nova-criptografada");

        service.redefinirSenha(dto);

        assertThat(usuario.getSenha()).isEqualTo("senha-nova-criptografada");
        assertThat(usuario.getRefreshTokenVersion()).isEqualTo(3);
        assertThat(token.isUtilizado()).isTrue();
        verify(usuarioRepository).save(usuario);
        verify(tokenRepository).save(token);
    }

    @Test
    void redefinirSenhaDeveValidarConfirmacaoAntesDeBuscarToken() {
        RedefinirSenhaDTO dto = new RedefinirSenhaDTO(
                "123456",
                "usuario@arbor.local",
                "NovaSenha1",
                "OutraSenha1");

        assertThatThrownBy(() -> service.redefinirSenha(dto))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("As senhas nao coincidem.");

        verify(tokenRepository, never()).findByCodigoAndUsuario_EmailAndUtilizadoFalse("123456", "usuario@arbor.local");
    }

    private TokenRecuperacao tokenValido() {
        TokenRecuperacao token = new TokenRecuperacao();
        token.setId(UUID.fromString("ca9687ec-90ad-4e0e-957f-2f6605d85669"));
        token.setCodigo("123456");
        token.setUsuario(usuario());
        token.setExpiracao(LocalDateTime.now().plusMinutes(10));
        token.setUtilizado(false);
        return token;
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.fromString("cb1f6115-6494-4635-8147-ad4ff50eec3d"));
        usuario.setNome("Usuario Arbor");
        usuario.setEmail("usuario@arbor.local");
        usuario.setSenha("senha-antiga-criptografada");
        usuario.setPerfilAcesso(Perfil.PUBLICO_GERAL);
        usuario.setRefreshTokenVersion(2);
        usuario.setAtivo(true);
        return usuario;
    }
}
