package com.example.arbor.service;

import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.TokenRecuperacao;
import com.example.arbor.repository.TokenRecuperacaoRepository;
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
    private final TokenRecuperacaoRepository TokenRecuperacaoRepository;
    private final EmailService emailService;

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            TokenRecuperacaoRepository TokenRecuperacaoRepository,
            EmailService emailService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.TokenRecuperacaoRepository = TokenRecuperacaoRepository;
        this.emailService = emailService;
    }

    public Usuario buscarEntidadePorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Executor não encontrado."));
    }

    public List<UsuarioResponseDTO> listarTodos(Boolean ativo, Usuario executor) {
        List<Usuario> usuarios;

        if (executor.getPerfilAcesso() == Perfil.GESTOR) {
            usuarios = ativo == null ? repository.findAll() : repository.findByAtivo(ativo);
        } else {
            usuarios = repository.findByAtivo(true);
        }

        return usuarios.stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDTO buscarPorId(UUID id, Usuario executor) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        validarVisibilidade(usuario, executor);

        return new UsuarioResponseDTO(usuario);
    }

    public UsuarioResponseDTO buscarPorEmail(String email, Usuario executor) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário com e-mail " + email + " não encontrado."));

        validarVisibilidade(usuario, executor);

        return new UsuarioResponseDTO(usuario);
    }

    public List<UsuarioResponseDTO> buscarPorPerfil(Perfil perfil, Boolean ativo) {
        List<Usuario> usuarios = ativo == null
                ? repository.findByPerfilAcesso(perfil)
                : repository.findByPerfilAcessoAndAtivo(perfil, ativo);
        return usuarios.stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
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
        usuario.setAtivo(true);

        return new UsuarioResponseDTO(repository.save(usuario));
    }

    @Transactional
    public void deletar(UUID id, Usuario executor) {
        if (executor.getPerfilAcesso() != Perfil.GESTOR) {
            throw new RuntimeException("Acesso negado: Você não tem permissão para remover utilizadores.");
        }

        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        usuario.setAtivo(false);
        int refreshTokenVersion = usuario.getRefreshTokenVersion() == null ? 0 : usuario.getRefreshTokenVersion();
        usuario.setRefreshTokenVersion(refreshTokenVersion + 1);
        repository.save(usuario);
    }

    private void validarVisibilidade(Usuario usuario, Usuario executor) {
        if (Boolean.FALSE.equals(usuario.getAtivo()) && executor.getPerfilAcesso() != Perfil.GESTOR) {
            throw new RuntimeException("Usuário não encontrado.");
        }
    }
}
