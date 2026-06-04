package com.example.arbor.config;

import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class DataSeederTest {

    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final Environment environment = mock(Environment.class);
    private final DataSeeder dataSeeder = new DataSeeder();

    @Test
    void naoDeveCriarUsuarioQuandoJaExisteGestorOuAdministrador() throws Exception {
        when(repository.existsByPerfilAcessoIn(List.of(Perfil.ADMINISTRADOR, Perfil.GESTOR))).thenReturn(true);

        CommandLineRunner runner = dataSeeder.bootstrapPrimeiroAdministrador(
                repository,
                passwordEncoder,
                environment,
                true,
                "Admin Dev",
                "admin-dev@arbor.local",
                "DevAdmin123!",
                "ADMINISTRADOR");

        runner.run();

        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void deveCriarAdministradorQuandoNaoExisteUsuarioAdministrativoEBootstrapEstaConfigurado() throws Exception {
        when(repository.existsByPerfilAcessoIn(List.of(Perfil.ADMINISTRADOR, Perfil.GESTOR))).thenReturn(false);
        when(repository.existsByEmail("admin-dev@arbor.local")).thenReturn(false);
        when(passwordEncoder.encode("DevAdmin123!")).thenReturn("senha-criptografada");

        CommandLineRunner runner = dataSeeder.bootstrapPrimeiroAdministrador(
                repository,
                passwordEncoder,
                environment,
                true,
                "Admin Dev",
                "admin-dev@arbor.local",
                "DevAdmin123!",
                "ADMINISTRADOR");

        runner.run();

        verify(repository).save(org.mockito.ArgumentMatchers.argThat(usuario ->
                usuario.getNome().equals("Admin Dev")
                        && usuario.getEmail().equals("admin-dev@arbor.local")
                        && usuario.getSenha().equals("senha-criptografada")
                        && usuario.getPerfilAcesso() == Perfil.ADMINISTRADOR
                        && Boolean.TRUE.equals(usuario.getAtivo())
                        && usuario.getRefreshTokenVersion() == 0));
    }

    @Test
    void deveFalharEmProducaoQuandoNaoExisteAdminENaoHaCredenciaisBootstrap() {
        when(repository.existsByPerfilAcessoIn(List.of(Perfil.ADMINISTRADOR, Perfil.GESTOR))).thenReturn(false);
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        CommandLineRunner runner = dataSeeder.bootstrapPrimeiroAdministrador(
                repository,
                passwordEncoder,
                environment,
                true,
                "",
                "",
                "",
                "GESTOR");

        assertThatThrownBy(runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Nenhum usuario gestor/administrador encontrado. Defina ARBOR_BOOTSTRAP_ADMIN_EMAIL e ARBOR_BOOTSTRAP_ADMIN_PASSWORD.");
    }

    @Test
    void deveRejeitarSenhaFracaNoBootstrap() {
        when(repository.existsByPerfilAcessoIn(List.of(Perfil.ADMINISTRADOR, Perfil.GESTOR))).thenReturn(false);

        CommandLineRunner runner = dataSeeder.bootstrapPrimeiroAdministrador(
                repository,
                passwordEncoder,
                environment,
                true,
                "Admin Dev",
                "admin-dev@arbor.local",
                "123456",
                "GESTOR");

        assertThatThrownBy(runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Senha de bootstrap administrativo deve ter ao menos 12 caracteres, uma letra maiuscula e um numero.");
    }

    @Test
    void deveRejeitarPerfilNaoAdministrativoNoBootstrap() {
        when(repository.existsByPerfilAcessoIn(List.of(Perfil.ADMINISTRADOR, Perfil.GESTOR))).thenReturn(false);

        CommandLineRunner runner = dataSeeder.bootstrapPrimeiroAdministrador(
                repository,
                passwordEncoder,
                environment,
                true,
                "Pesquisador",
                "pesquisador@arbor.local",
                "DevAdmin123!",
                "PESQUISADOR");

        assertThatThrownBy(runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Perfil de bootstrap deve ser ADMINISTRADOR ou GESTOR.");
    }
}
