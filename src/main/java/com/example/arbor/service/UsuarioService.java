package com.example.arbor.service;

import com.example.arbor.dto.request.AlterarSenhaRequestDTO;
import com.example.arbor.dto.request.AtualizarPerfilRequestDTO;
import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.UsuarioPerfilResponseDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.exception.AcessoNegadoException;
import com.example.arbor.exception.ConflitoException;
import com.example.arbor.exception.RecursoNaoEncontradoException;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.exception.TokenInvalidoException;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.model.Usuario;
import com.example.arbor.repository.TokenRecuperacaoRepository;
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Executor não encontrado."));
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
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        validarVisibilidade(usuario, executor);

        return new UsuarioResponseDTO(usuario);
    }

    public UsuarioResponseDTO buscarPorEmail(String email, Usuario executor) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com e-mail " + email + " não encontrado."));

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

    @Transactional(readOnly = true)
    public UsuarioPerfilResponseDTO buscarMeuPerfil(Usuario autenticado) {
        return UsuarioPerfilResponseDTO.from(buscarUsuarioAutenticado(autenticado));
    }

    @Transactional
    public UsuarioPerfilResponseDTO atualizarMeuPerfil(Usuario autenticado, AtualizarPerfilRequestDTO dto) {
        Usuario usuario = buscarUsuarioAutenticado(autenticado);
        boolean atualizado = false;

        if (dto.nome() != null) {
            String nome = dto.nome().trim();
            if (nome.isBlank()) {
                throw new RequisicaoInvalidaException("Nome não pode ser vazio.");
            }
            usuario.setNome(nome);
            atualizado = true;
        }

        if (dto.email() != null) {
            String email = dto.email().trim();
            if (email.isBlank()) {
                throw new RequisicaoInvalidaException("E-mail não pode ser vazio.");
            }
            if (!email.equalsIgnoreCase(usuario.getEmail()) && repository.existsByEmailAndIdNot(email, usuario.getId())) {
                throw new ConflitoException("E-mail já cadastrado.");
            }
            usuario.setEmail(email);
            atualizado = true;
        }

        if (!atualizado) {
            throw new RequisicaoInvalidaException("Informe ao menos um campo para atualização.");
        }

        return UsuarioPerfilResponseDTO.from(repository.save(usuario));
    }

    @Transactional
    public void alterarMinhaSenha(Usuario autenticado, AlterarSenhaRequestDTO dto) {
        Usuario usuario = buscarUsuarioAutenticado(autenticado);

        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new RequisicaoInvalidaException("Senha atual inválida.");
        }

        if (!dto.novaSenha().equals(dto.confirmarSenha())) {
            throw new RequisicaoInvalidaException("As senhas não coincidem.");
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        incrementarRefreshTokenVersion(usuario);
        repository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO salvar(UsuarioRequestDTO dto, Usuario executor) {
        if (dto.perfilAcesso() == Perfil.ADMINISTRADOR
                || dto.perfilAcesso() == Perfil.GESTOR
                || dto.perfilAcesso() == Perfil.PESQUISADOR) {
            if (executor == null || executor.getPerfilAcesso() != Perfil.GESTOR) {
                throw new AcessoNegadoException("Apenas gestores podem atribuir níveis altos de acesso.");
            }
        }

        if (repository.existsByEmail(dto.email())) {
            throw new ConflitoException("E-mail já cadastrado.");
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
            throw new AcessoNegadoException("Você não tem permissão para remover usuários.");
        }

        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        usuario.setAtivo(false);
        incrementarRefreshTokenVersion(usuario);
        repository.save(usuario);
    }

    private Usuario buscarUsuarioAutenticado(Usuario autenticado) {
        if (autenticado == null || autenticado.getId() == null) {
            throw new TokenInvalidoException("Usuário autenticado não encontrado.");
        }

        return repository.findById(autenticado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private void incrementarRefreshTokenVersion(Usuario usuario) {
        int refreshTokenVersion = usuario.getRefreshTokenVersion() == null ? 0 : usuario.getRefreshTokenVersion();
        usuario.setRefreshTokenVersion(refreshTokenVersion + 1);
    }

    private void validarVisibilidade(Usuario usuario, Usuario executor) {
        if (Boolean.FALSE.equals(usuario.getAtivo()) && executor.getPerfilAcesso() != Perfil.GESTOR) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado.");
        }
    }
}
