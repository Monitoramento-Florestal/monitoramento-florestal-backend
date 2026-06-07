package com.example.arbor.controller;

import com.example.arbor.dto.request.LoginRequestDTO;
import com.example.arbor.dto.request.RefreshRequestDTO;
import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.AuthUserResponseDTO;
import com.example.arbor.dto.response.LoginResponseDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.exception.TokenInvalidoException;
import com.example.arbor.model.Usuario;
import com.example.arbor.repository.UsuarioRepository;
import com.example.arbor.security.JwtService;
import com.example.arbor.service.UsuarioService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(
            AuthenticationManager authManager,
            JwtService jwtService,
            UsuarioService usuarioService,
            UserDetailsService userDetailsService,
            UsuarioRepository usuarioRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.userDetailsService = userDetailsService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha()));
        Usuario usuario = (Usuario) auth.getPrincipal();

        incrementarRefreshTokenVersion(usuario);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new LoginResponseDTO(
                jwtService.gerarTokenAcesso(usuario),
                jwtService.gerarTokenRefresh(usuario),
                AuthUserResponseDTO.from(usuario)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO dto) {
        String refreshToken = dto.refreshToken();

        final String username;
        try {
            username = jwtService.extrairUsername(refreshToken);
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException ex) {
            throw new TokenInvalidoException("Refresh token invalido ou expirado.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!(userDetails instanceof Usuario usuario)) {
            throw new TokenInvalidoException("Usuario invalido para refresh token.");
        }

        if (!jwtService.isRefreshTokenValido(refreshToken, usuario)) {
            throw new TokenInvalidoException("Refresh token invalido ou expirado.");
        }

        incrementarRefreshTokenVersion(usuario);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new LoginResponseDTO(
                jwtService.gerarTokenAcesso(usuario),
                jwtService.gerarTokenRefresh(usuario),
                AuthUserResponseDTO.from(usuario)));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthUserResponseDTO> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(AuthUserResponseDTO.from(usuario));
    }

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.salvar(dto, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void incrementarRefreshTokenVersion(Usuario usuario) {
        Integer refreshVersion = usuario.getRefreshTokenVersion();
        int nextVersion = (refreshVersion == null ? 0 : refreshVersion) + 1;
        usuario.setRefreshTokenVersion(nextVersion);
    }
}
