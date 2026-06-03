package com.example.arbor.controller;

import com.example.arbor.dto.request.LoginRequestDTO;
import com.example.arbor.dto.request.RefreshRequestDTO;
import com.example.arbor.dto.response.AuthUserResponseDTO;
import com.example.arbor.dto.response.LoginResponseDTO;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.repository.UsuarioRepository;
import com.example.arbor.security.JwtService;
import com.example.arbor.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthController controller;

    @Test
    void loginDeveRetornarTokensEUsuario() {
        Usuario usuario = usuario();
        Authentication authentication = mock(Authentication.class);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);
        when(jwtService.gerarTokenAcesso(usuario)).thenReturn("access-token");
        when(jwtService.gerarTokenRefresh(usuario)).thenReturn("refresh-token");

        ResponseEntity<LoginResponseDTO> response = controller.login(new LoginRequestDTO(usuario.getEmail(), "senha123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("access-token");
        assertThat(response.getBody().refreshToken()).isEqualTo("refresh-token");
        assertThat(response.getBody().usuario()).isEqualTo(AuthUserResponseDTO.from(usuario));
        assertThat(usuario.getRefreshTokenVersion()).isEqualTo(4);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void refreshDeveRotacionarTokensQuandoValido() {
        Usuario usuario = usuario();

        when(jwtService.extrairUsername("refresh-token")).thenReturn(usuario.getEmail());
        when(userDetailsService.loadUserByUsername(usuario.getEmail())).thenReturn(usuario);
        when(jwtService.isRefreshTokenValido("refresh-token", usuario)).thenReturn(true);
        when(jwtService.gerarTokenAcesso(usuario)).thenReturn("novo-access-token");
        when(jwtService.gerarTokenRefresh(usuario)).thenReturn("novo-refresh-token");

        ResponseEntity<?> response = controller.refresh(new RefreshRequestDTO("refresh-token"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(LoginResponseDTO.class);

        LoginResponseDTO body = (LoginResponseDTO) response.getBody();
        assertThat(body.accessToken()).isEqualTo("novo-access-token");
        assertThat(body.refreshToken()).isEqualTo("novo-refresh-token");
        assertThat(body.usuario()).isEqualTo(AuthUserResponseDTO.from(usuario));
        assertThat(usuario.getRefreshTokenVersion()).isEqualTo(4);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void refreshDeveRetornarUnauthorizedQuandoTokenForInvalido() {
        when(jwtService.extrairUsername("refresh-token")).thenThrow(new IllegalArgumentException("token invalido"));

        ResponseEntity<?> response = controller.refresh(new RefreshRequestDTO("refresh-token"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("Refresh token invalido ou expirado.");
    }

    @Test
    void refreshDeveRetornarUnauthorizedQuandoRefreshNaoForValidoParaUsuario() {
        Usuario usuario = usuario();

        when(jwtService.extrairUsername("refresh-token")).thenReturn(usuario.getEmail());
        when(userDetailsService.loadUserByUsername(usuario.getEmail())).thenReturn(usuario);
        when(jwtService.isRefreshTokenValido("refresh-token", usuario)).thenReturn(false);

        ResponseEntity<?> response = controller.refresh(new RefreshRequestDTO("refresh-token"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("Refresh token invalido ou expirado.");
    }

    @Test
    void meDeveRetornarUsuarioAutenticado() {
        Usuario usuario = usuario();

        ResponseEntity<AuthUserResponseDTO> response = controller.me(usuario);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(AuthUserResponseDTO.from(usuario));
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.fromString("85da941b-a4f5-4cb4-8be4-a5ef2423e1d5"));
        usuario.setNome("Gestor Arbor");
        usuario.setEmail("gestor@arbor.local");
        usuario.setSenha("senha-criptografada");
        usuario.setPerfilAcesso(Perfil.GESTOR);
        usuario.setRefreshTokenVersion(3);
        usuario.setAtivo(true);
        return usuario;
    }
}
