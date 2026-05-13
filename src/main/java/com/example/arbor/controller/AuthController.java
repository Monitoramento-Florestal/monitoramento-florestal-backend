package com.example.arbor.controller;

import com.example.arbor.dto.request.LoginRequestDTO;
import com.example.arbor.dto.request.RefreshRequestDTO;
import com.example.arbor.dto.response.LoginResponseDTO;
import com.example.arbor.dto.request.UsuarioRequestDTO;
import com.example.arbor.dto.response.UsuarioResponseDTO;
import com.example.arbor.model.Usuario;
import com.example.arbor.security.JwtService;
import com.example.arbor.service.UsuarioService;
import com.example.arbor.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthenticationManager authManager,
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
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha()));


        Usuario usuario = (Usuario) auth.getPrincipal();


        Integer refreshVersion = usuario.getRefreshTokenVersion();
        int nextVersion = (refreshVersion == null ? 0 : refreshVersion) + 1;
        usuario.setRefreshTokenVersion(nextVersion);
        usuarioRepository.save(usuario);


        String accessToken = jwtService.gerarTokenAcesso(usuario);
        String refreshToken = jwtService.gerarTokenRefresh(usuario);


        return ResponseEntity.ok(new LoginResponseDTO(accessToken, refreshToken, usuario.getEmail(), usuario.getNome()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequestDTO dto) {
        String refreshToken = dto.refreshToken();

        final String username;
        try {
            username = jwtService.extrairUsername(refreshToken);
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token invalido ou expirado.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!(userDetails instanceof Usuario usuario)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario invalido para refresh token.");
        }

        if (!jwtService.isRefreshTokenValido(refreshToken, usuario)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token invalido ou expirado.");
        }

        Integer refreshVersion = usuario.getRefreshTokenVersion();
        int nextVersion = (refreshVersion == null ? 0 : refreshVersion) + 1;
        usuario.setRefreshTokenVersion(nextVersion);
        usuarioRepository.save(usuario);

        String novoAccessToken = jwtService.gerarTokenAcesso(usuario);
        String novoRefreshToken = jwtService.gerarTokenRefresh(usuario);

        return ResponseEntity.ok(new LoginResponseDTO(novoAccessToken, novoRefreshToken, usuario.getEmail(), usuario.getNome()));
    }

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponseDTO> registrar(@RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.salvar(dto, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
