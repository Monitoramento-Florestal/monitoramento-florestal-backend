package com.example.arbor.config;

import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final SecurityConfig securityConfig = new SecurityConfig(usuarioRepository);

    @Test
    void userDetailsServiceDeveCarregarUsuarioPorEmail() {
        Usuario usuario = usuario(true);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        UserDetails userDetails = securityConfig.userDetailsService().loadUserByUsername(usuario.getEmail());

        assertThat(userDetails.getUsername()).isEqualTo(usuario.getEmail());
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_GESTOR");
    }

    @Test
    void userDetailsServiceDevePreservarUsuarioDesativadoComoDisabled() {
        Usuario usuario = usuario(false);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        UserDetails userDetails = securityConfig.userDetailsService().loadUserByUsername(usuario.getEmail());

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void userDetailsServiceDeveLancarQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findByEmail("ausente@arbor.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> securityConfig.userDetailsService().loadUserByUsername("ausente@arbor.local"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuario nao encontrado: ausente@arbor.local");
    }

    private Usuario usuario(boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.fromString("a9516b6a-93f1-44e0-9964-b6ed98b4dbd2"));
        usuario.setNome("Gestor Arbor");
        usuario.setEmail("gestor@arbor.local");
        usuario.setSenha("senha-criptografada");
        usuario.setPerfilAcesso(Perfil.GESTOR);
        usuario.setAtivo(ativo);
        return usuario;
    }
}
