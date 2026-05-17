package com.example.arbor.service;

import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.TokenResetSenha;
import com.example.arbor.repository.TokenResetSenhaRepository;
import com.example.arbor.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenResetSenhaRepository tokenResetSenhaRepository;
    private final EmailService emailService;

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            TokenResetSenhaRepository tokenResetSenhaRepository,
            EmailService emailService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenResetSenhaRepository = tokenResetSenhaRepository;
        this.emailService = emailService;
    }

    public Usuario buscarEntidadePorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Executor não encontrado."));
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDTO buscarPorId(UUID id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        return new UsuarioResponseDTO(usuario);
    }

    public UsuarioResponseDTO buscarPorEmail(String email) {
        return repository.findByEmail(email)
                .map(UsuarioResponseDTO::new)
                .orElseThrow(() -> new RuntimeException("Usuário com e-mail " + email + " não encontrado."));
    }

    public List<UsuarioResponseDTO> buscarPorPerfil(Perfil perfil) {
        List<Usuario> usuarios = repository.findByPerfilAcesso(perfil);
        return usuarios.stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    public void solicitarResetSenha(String email) {
        Optional<Usuario> userOpt = repository.findByEmail(email);

        if (userOpt.isEmpty()) return;

        Usuario usuario = userOpt.get();

        TokenResetSenha token = new TokenResetSenha();
        token.setToken(UUID.randomUUID().toString());
        token.setUsuario(usuario);
        token.setDataExpiracao(LocalDateTime.now().plusMinutes(30));
        token.setUsado(false);

        tokenResetSenhaRepository.save(token);

        emailService.enviarEmailReset(usuario.getEmail(), token.getToken());
    }

    public void resetarSenha(String tokenStr, String novaSenha) {

        TokenResetSenha token = tokenResetSenhaRepository
                .findByTokenAndUsadoFalse(tokenStr)
                .orElseThrow(() -> new RuntimeException("Token inválido ou já usado"));

        if (token.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        Usuario usuario = token.getUsuario();

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        repository.save(usuario);

        token.setUsado(true);
        tokenResetSenhaRepository.save(token);
    }

    @Transactional
    public UsuarioResponseDTO salvar(UsuarioRequestDTO dto, Usuario executor) {
        if (dto.perfilAcesso() == Perfil.ADMINISTRADOR
                || dto.perfilAcesso() == Perfil.GESTOR
                || dto.perfilAcesso() == Perfil.PESQUISADOR) {
            if (executor == null || executor.getPerfilAcesso() != Perfil.GESTOR) {
                throw new RuntimeException("Erro: Apenas gestores podem atribuir níveis altos de acesso.");
            }
        }

        if (repository.existsByEmail(dto.email())) {
            throw new RuntimeException("E-mail já cadastrado, pae!");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setPerfilAcesso(dto.perfilAcesso());

        return new UsuarioResponseDTO(repository.save(usuario));
    }

    @Transactional
    public void deletar(UUID id, Usuario executor) {
        if (executor.getPerfilAcesso() != Perfil.GESTOR) {
            throw new RuntimeException("Acesso negado: Você não tem permissão para remover utilizadores.");
        }

        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        repository.delete(usuario);
    }
}