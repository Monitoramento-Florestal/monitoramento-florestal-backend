package com.example.arbor.config;

import com.example.arbor.repository.UsuarioRepository;
import com.example.arbor.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Define as regras de acesso por rota
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            JwtAuthFilter jwtAuthFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Desativa CSRF (APIs REST não usam)
                .authorizeHttpRequests(auth -> auth
                        // Rotas públicas (não precisam de token):
                        .requestMatchers(
                                "/api/auth/**", // login e registro
                                "/swagger-ui/**", // documentação
                                "/v3/api-docs/**" // spec OpenAPI
                        ).permitAll()
                        // Tudo o mais exige autenticação:
                        .anyRequest().authenticated())
                // Sem sessão! JWT é stateless — o token substitui a sessão
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Registra nosso provedor de autenticação
                .authenticationProvider(authenticationProvider)
                // Adiciona nosso filtro JWT ANTES do filtro padrão de senha
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Diz ao Spring como buscar usuários (por email, no banco)
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    // Provedor de autenticação: combina UserDetailsService + PasswordEncoder
    // No Spring Security 7, o PasswordEncoder é detectado automaticamente via @Bean
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // Gerenciador de autenticação (necessário para o AuthController usar)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // BCrypt: algoritmo de hash para senhas (nunca salve senha em texto puro!)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
