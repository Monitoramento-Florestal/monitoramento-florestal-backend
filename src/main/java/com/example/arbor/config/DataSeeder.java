package com.example.arbor.config;

import com.example.arbor.model.enums.Perfil;
import com.example.arbor.model.Usuario;
import com.example.arbor.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsuarios(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {

            if (repo.count() == 0) {

                repo.save(createUser("Admin", "admin@arbor.com", "123456", Perfil.ADMINISTRADOR, encoder));
                repo.save(createUser("Gestor", "gestor@arbor.com", "123456", Perfil.GESTOR, encoder));
                repo.save(createUser("Pesquisador", "pesq@arbor.com", "123456", Perfil.PESQUISADOR, encoder));
                repo.save(createUser("Publico", "publico@arbor.com", "123456", Perfil.PUBLICO_GERAL, encoder));

                System.out.println("Seed de usuários executado com sucesso!");
            }
        };
    }

    private Usuario createUser(String nome, String email, String senha, Perfil perfil, PasswordEncoder encoder) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(encoder.encode(senha));
        u.setPerfilAcesso(perfil);
        return u;
    }
}