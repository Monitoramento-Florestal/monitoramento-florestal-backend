package com.example.arbor.service;

import com.example.arbor.dto.request.AlterarSenhaRequestDTO;
import com.example.arbor.dto.request.AtualizarPerfilRequestDTO;
import com.example.arbor.dto.request.AtualizarUsuarioAdminRequestDTO;
import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.PageResponseDTO;
import com.example.arbor.dto.response.UsuarioPerfilResponseDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.exception.AcessoNegadoException;
import com.example.arbor.exception.ConflitoException;
import com.example.arbor.exception.RecursoNaoEncontradoException;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.exception.TokenInvalidoException;
import com.example.arbor.model.Usuario;
import com.example.arbor.model.enums.Perfil;
import com.example.arbor.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    @Transactional(readOnly = true)
    public Usuario buscarEntidadePorId(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<UsuarioResponseDTO> listarTodos(
            Perfil perfilAcesso,
            Boolean ativo,
            String search,
            int page,
            int limit,
            Usuario executor) {
        validarOperadorAdministrativo(executor);

        int pageNumber = validarPage(page);
        int pageSize = validarLimit(limit);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("nome").ascending());
        Page<Usuario> usuarios = repository.findAll(usuarioSpecification(perfilAcesso, ativo, search), pageable);

        return new PageResponseDTO<>(
                usuarios.getContent().stream().map(UsuarioResponseDTO::new).toList(),
                pageNumber,
                pageSize,
                usuarios.getTotalElements());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(UUID id, Usuario executor) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));

        validarVisibilidade(usuario, executor);
        return new UsuarioResponseDTO(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorEmail(String email, Usuario executor) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario com e-mail " + email + " nao encontrado."));

        validarVisibilidade(usuario, executor);
        return new UsuarioResponseDTO(usuario);
    }

    @Transactional(readOnly = true)
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
                throw new RequisicaoInvalidaException("Nome nao pode ser vazio.");
            }
            usuario.setNome(nome);
            atualizado = true;
        }

        if (dto.email() != null) {
            String email = dto.email().trim();
            if (email.isBlank()) {
                throw new RequisicaoInvalidaException("E-mail nao pode ser vazio.");
            }
            if (!email.equalsIgnoreCase(usuario.getEmail()) && repository.existsByEmailAndIdNot(email, usuario.getId())) {
                throw new ConflitoException("E-mail ja cadastrado.");
            }
            usuario.setEmail(email);
            atualizado = true;
        }

        if (!atualizado) {
            throw new RequisicaoInvalidaException("Informe ao menos um campo para atualizacao.");
        }

        return UsuarioPerfilResponseDTO.from(repository.save(usuario));
    }

    @Transactional
    public void alterarMinhaSenha(Usuario autenticado, AlterarSenhaRequestDTO dto) {
        Usuario usuario = buscarUsuarioAutenticado(autenticado);

        if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
            throw new RequisicaoInvalidaException("Senha atual invalida.");
        }

        if (!dto.novaSenha().equals(dto.confirmarSenha())) {
            throw new RequisicaoInvalidaException("As senhas nao coincidem.");
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        incrementarRefreshTokenVersion(usuario);
        repository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO salvar(UsuarioRequestDTO dto, Usuario executor) {
        validarAtribuicaoPerfil(dto.perfilAcesso(), executor);

        if (repository.existsByEmail(dto.email())) {
            throw new ConflitoException("E-mail ja cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome().trim());
        usuario.setEmail(dto.email().trim());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setPerfilAcesso(dto.perfilAcesso());
        usuario.setAtivo(true);

        return new UsuarioResponseDTO(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(UUID id, AtualizarUsuarioAdminRequestDTO dto, Usuario executor) {
        validarOperadorAdministrativo(executor);
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
        boolean atualizado = false;
        boolean invalidaRefreshToken = false;

        if (dto.nome() != null) {
            String nome = dto.nome().trim();
            if (nome.isBlank()) {
                throw new RequisicaoInvalidaException("Nome nao pode ser vazio.");
            }
            usuario.setNome(nome);
            atualizado = true;
        }

        if (dto.email() != null) {
            String email = dto.email().trim();
            if (email.isBlank()) {
                throw new RequisicaoInvalidaException("E-mail nao pode ser vazio.");
            }
            if (!email.equalsIgnoreCase(usuario.getEmail()) && repository.existsByEmailAndIdNot(email, usuario.getId())) {
                throw new ConflitoException("E-mail ja cadastrado.");
            }
            usuario.setEmail(email);
            atualizado = true;
        }

        if (dto.perfilAcesso() != null && dto.perfilAcesso() != usuario.getPerfilAcesso()) {
            validarAtribuicaoPerfil(dto.perfilAcesso(), executor);
            usuario.setPerfilAcesso(dto.perfilAcesso());
            atualizado = true;
            invalidaRefreshToken = true;
        }

        if (dto.ativo() != null && !dto.ativo().equals(usuario.getAtivo())) {
            usuario.setAtivo(dto.ativo());
            atualizado = true;
            if (Boolean.FALSE.equals(dto.ativo())) {
                invalidaRefreshToken = true;
            }
        }

        if (!atualizado) {
            throw new RequisicaoInvalidaException("Informe ao menos um campo para atualizacao.");
        }

        if (invalidaRefreshToken) {
            incrementarRefreshTokenVersion(usuario);
        }

        return new UsuarioResponseDTO(repository.save(usuario));
    }

    @Transactional
    public void deletar(UUID id, Usuario executor) {
        validarOperadorAdministrativo(executor);

        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
        usuario.setAtivo(false);
        incrementarRefreshTokenVersion(usuario);
        repository.save(usuario);
    }

    private Usuario buscarUsuarioAutenticado(Usuario autenticado) {
        if (autenticado == null || autenticado.getId() == null) {
            throw new TokenInvalidoException("Usuario autenticado nao encontrado.");
        }

        return repository.findById(autenticado.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
    }

    private void incrementarRefreshTokenVersion(Usuario usuario) {
        int refreshTokenVersion = usuario.getRefreshTokenVersion() == null ? 0 : usuario.getRefreshTokenVersion();
        usuario.setRefreshTokenVersion(refreshTokenVersion + 1);
    }

    private void validarOperadorAdministrativo(Usuario executor) {
        if (!isOperadorAdministrativo(executor)) {
            throw new AcessoNegadoException("Voce nao tem permissao para gerenciar usuarios.");
        }
    }

    private boolean isOperadorAdministrativo(Usuario usuario) {
        return usuario != null
                && usuario.getPerfilAcesso() != null
                && usuario.getPerfilAcesso().isAdministrativo();
    }

    private void validarAtribuicaoPerfil(Perfil perfil, Usuario executor) {
        if (perfil.isAdministrativo() && !isAdministrador(executor)) {
            throw new AcessoNegadoException(
                    "Apenas administradores podem criar usuarios ou atribuir perfis administrativos.");
        }

        if (perfil == Perfil.PESQUISADOR && !isOperadorAdministrativo(executor)) {
            throw new AcessoNegadoException(
                    "Apenas gestores ou administradores podem atribuir o perfil de pesquisador.");
        }
    }

    private boolean isAdministrador(Usuario usuario) {
        return usuario != null && usuario.getPerfilAcesso() == Perfil.ADMINISTRADOR;
    }

    private int validarPage(int page) {
        if (page < 0) {
            throw new RequisicaoInvalidaException("Page nao pode ser negativo.");
        }
        return page;
    }

    private int validarLimit(int limit) {
        if (limit < 1 || limit > 100) {
            throw new RequisicaoInvalidaException("Limit deve estar entre 1 e 100.");
        }
        return limit;
    }

    private Specification<Usuario> usuarioSpecification(Perfil perfilAcesso, Boolean ativo, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (perfilAcesso != null) {
                predicates.add(criteriaBuilder.equal(root.get("perfilAcesso"), perfilAcesso));
            }

            if (ativo != null) {
                predicates.add(criteriaBuilder.equal(root.get("ativo"), ativo));
            }

            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), term)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validarVisibilidade(Usuario usuario, Usuario executor) {
        if (Boolean.FALSE.equals(usuario.getAtivo()) && !isOperadorAdministrativo(executor)) {
            throw new RecursoNaoEncontradoException("Usuario nao encontrado.");
        }
    }
}
