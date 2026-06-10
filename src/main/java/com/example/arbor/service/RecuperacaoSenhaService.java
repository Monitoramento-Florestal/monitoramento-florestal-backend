package com.example.arbor.service;

import com.example.arbor.dto.request.RedefinirSenhaDTO;
import com.example.arbor.dto.request.SolicitarRecuperacaoDTO;
import com.example.arbor.dto.request.VerificarCodigoDTO;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.model.TokenRecuperacao;
import com.example.arbor.model.Usuario;
import com.example.arbor.repository.TokenRecuperacaoRepository;
import com.example.arbor.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class RecuperacaoSenhaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecuperacaoSenhaService.class);

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoRepository tokenRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    public RecuperacaoSenhaService(
            UsuarioRepository usuarioRepository,
            TokenRecuperacaoRepository tokenRepository,
            EmailSender emailSender,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.emailSender = emailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void solicitarRecuperacao(SolicitarRecuperacaoDTO dto) {
        usuarioRepository.findByEmail(dto.email()).ifPresent(usuario -> {
            tokenRepository.deleteByUsuario(usuario);
            tokenRepository.flush();

            TokenRecuperacao token = new TokenRecuperacao();
            token.setCodigo(gerarCodigo6Digitos());
            token.setUsuario(usuario);
            token.setExpiracao(LocalDateTime.now().plusMinutes(15));
            token.setUtilizado(false);

            tokenRepository.save(token);
            enviarEmail(usuario.getEmail(), usuario.getNome(), token.getCodigo());
        });
    }

    @Transactional(readOnly = true)
    public void verificarCodigo(VerificarCodigoDTO dto) {
        buscarTokenValido(dto.codigo(), dto.email());
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaDTO dto) {
        if (!dto.novaSenha().equals(dto.confirmarSenha())) {
            throw new RequisicaoInvalidaException("As senhas nao coincidem.");
        }

        TokenRecuperacao token = buscarTokenValido(dto.codigo(), dto.email());
        Usuario usuario = token.getUsuario();
        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        incrementarRefreshTokenVersion(usuario);
        usuarioRepository.save(usuario);

        token.setUtilizado(true);
        tokenRepository.save(token);
    }

    private TokenRecuperacao buscarTokenValido(String codigo, String email) {
        TokenRecuperacao token = tokenRepository.findByCodigoAndUsuario_EmailAndUtilizadoFalse(codigo, email)
                .orElseThrow(() -> new RequisicaoInvalidaException("Codigo invalido ou ja utilizado."));

        if (token.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new RequisicaoInvalidaException("Codigo expirado. Solicite um novo codigo.");
        }

        return token;
    }

    private String gerarCodigo6Digitos() {
        SecureRandom random = new SecureRandom();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }

    private void incrementarRefreshTokenVersion(Usuario usuario) {
        int refreshTokenVersion = usuario.getRefreshTokenVersion() == null ? 0 : usuario.getRefreshTokenVersion();
        usuario.setRefreshTokenVersion(refreshTokenVersion + 1);
    }

    private void enviarEmail(String destinatario, String nomeUsuario, String codigo) {
        String assunto = "Arbor - Codigo de recuperacao de senha";
        String corpo = "Ola, " + nomeUsuario + "!\n\n" +
                "Seu codigo de recuperacao de senha e:\n\n" +
                "  " + codigo + "\n\n" +
                "Este codigo e valido por 15 minutos.\n" +
                "Se voce nao solicitou a recuperacao, ignore este e-mail.\n\n" +
                "Equipe Arbor";

        try {
            emailSender.enviar(destinatario, assunto, corpo);
        } catch (RuntimeException ex) {
            LOGGER.warn("Falha ao enviar e-mail de recuperacao de senha para usuario destinatario", ex);
        }
    }
}
