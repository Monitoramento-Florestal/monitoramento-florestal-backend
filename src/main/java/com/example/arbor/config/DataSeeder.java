package com.example.arbor.config;

import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);
    private static final List<Perfil> PERFIS_ADMINISTRATIVOS = List.of(Perfil.ADMINISTRADOR, Perfil.GESTOR);

    @Bean
    CommandLineRunner bootstrapPrimeiroAdministrador(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            Environment environment,
            @Value("${arbor.bootstrap.admin.enabled:true}") boolean enabled,
            @Value("${arbor.bootstrap.admin.name:}") String nome,
            @Value("${arbor.bootstrap.admin.email:}") String email,
            @Value("${arbor.bootstrap.admin.password:}") String senha,
            @Value("${arbor.bootstrap.admin.role:GESTOR}") String perfil) {
        return args -> {
            if (repository.existsByPerfilAcessoIn(PERFIS_ADMINISTRATIVOS)) {
                LOGGER.info("Bootstrap administrativo ignorado: ja existe usuario gestor/administrador.");
                return;
            }

            boolean prod = environment.acceptsProfiles(Profiles.of("prod"));
            if (!enabled) {
                if (prod) {
                    throw new IllegalStateException(
                            "Nenhum usuario gestor/administrador encontrado e bootstrap administrativo desabilitado.");
                }
                LOGGER.warn("Bootstrap administrativo desabilitado e nenhum usuario gestor/administrador encontrado.");
                return;
            }

            if (isBlank(email) || isBlank(senha)) {
                if (prod) {
                    throw new IllegalStateException(
                            "Nenhum usuario gestor/administrador encontrado. Defina ARBOR_BOOTSTRAP_ADMIN_EMAIL e ARBOR_BOOTSTRAP_ADMIN_PASSWORD.");
                }
                LOGGER.warn("Bootstrap administrativo ignorado: e-mail ou senha nao configurados.");
                return;
            }

            Perfil perfilAcesso = parsePerfilAdministrativo(perfil);
            validarSenhaBootstrap(senha);

            if (repository.existsByEmail(email.trim())) {
                throw new IllegalStateException("E-mail de bootstrap administrativo ja cadastrado: " + email.trim());
            }

            Usuario usuario = new Usuario();
            usuario.setNome(isBlank(nome) ? "Administrador Arbor" : nome.trim());
            usuario.setEmail(email.trim());
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setPerfilAcesso(perfilAcesso);
            usuario.setAtivo(true);
            usuario.setRefreshTokenVersion(0);

            repository.save(usuario);
            LOGGER.info("Usuario administrativo inicial criado com perfil {} e e-mail {}.", perfilAcesso, usuario.getEmail());
        };
    }

    private Perfil parsePerfilAdministrativo(String perfil) {
        try {
            Perfil perfilAcesso = Perfil.valueOf(perfil.trim().toUpperCase());
            if (!PERFIS_ADMINISTRATIVOS.contains(perfilAcesso)) {
                throw new IllegalStateException("Perfil de bootstrap deve ser ADMINISTRADOR ou GESTOR.");
            }
            return perfilAcesso;
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Perfil de bootstrap invalido: " + perfil, ex);
        }
    }

    private void validarSenhaBootstrap(String senha) {
        if (senha.length() < 12 || !senha.matches(".*[A-Z].*") || !senha.matches(".*\\d.*")) {
            throw new IllegalStateException(
                    "Senha de bootstrap administrativo deve ter ao menos 12 caracteres, uma letra maiuscula e um numero.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
