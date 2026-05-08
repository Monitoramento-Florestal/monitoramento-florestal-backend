package com.example.arbor.service;

import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.model.Perfil;
import com.example.arbor.model.Usuario;
import com.example.arbor.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
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

    @Transactional
    public UsuarioResponseDTO salvar(UsuarioRequestDTO dto, Usuario executor) {
        if (dto.perfilAcesso() == Perfil.GESTOR || dto.perfilAcesso() == Perfil.PESQUISADOR) {
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