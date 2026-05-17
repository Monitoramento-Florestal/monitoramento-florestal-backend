package com.example.arbor.service;

import com.example.arbor.dto.request.RedefinirSenhaDTO;
import com.example.arbor.dto.request.SolicitarRecuperacaoDTO;
import com.example.arbor.dto.request.VerificarCodigoDTO;
import com.example.arbor.exception.RequisicaoInvalidaException;
import com.example.arbor.model.TokenRecuperacao;
import com.example.arbor.model.Usuario;
import com.example.arbor.repository.TokenRecuperacaoRepository;
import com.example.arbor.repository.UsuarioRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class RecuperacaoSenhaService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public RecuperacaoSenhaService(UsuarioRepository usuarioRepository,
                                   TokenRecuperacaoRepository tokenRepository,
                                   JavaMailSender mailSender,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public void solicitarRecuperacao(SolicitarRecuperacaoDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RequisicaoInvalidaException("Nenhuma conta encontrada com esse e-mail."));
        
        tokenRepository.deleteByUsuario(usuario);
        tokenRepository.flush();

        String codigo = gerarCodigo6Digitos();

        TokenRecuperacao token = new TokenRecuperacao();
        token.setCodigo(codigo);
        token.setUsuario(usuario);
        token.setExpiracao(LocalDateTime.now().plusMinutes(15));
        token.setUtilizado(false);

        tokenRepository.save(token);

        enviarEmail(usuario.getEmail(), usuario.getNome(), codigo);
    }
    
    @Transactional(readOnly = true)
    public void verificarCodigo(VerificarCodigoDTO dto) {
        TokenRecuperacao token = tokenRepository.findByCodigoAndUtilizadoFalse(dto.codigo())
                .orElseThrow(() -> new RequisicaoInvalidaException("Código inválido ou já utilizado."));

        if (token.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new RequisicaoInvalidaException("Código expirado. Solicite um novo código.");
        }
    }
    
    @Transactional
    public void redefinirSenha(RedefinirSenhaDTO dto) {
        if (!dto.novaSenha().equals(dto.confirmarSenha())) {
            throw new RequisicaoInvalidaException("As senhas não coincidem.");
        }

        TokenRecuperacao token = tokenRepository.findByCodigoAndUtilizadoFalse(dto.codigo())
                .orElseThrow(() -> new RequisicaoInvalidaException("Código inválido ou já utilizado."));

        if (token.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new RequisicaoInvalidaException("Código expirado. Solicite um novo código.");
        }

        Usuario usuario = token.getUsuario();
        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);

        token.setUtilizado(true);
        tokenRepository.save(token);
    }
    
    private String gerarCodigo6Digitos() {
        SecureRandom random = new SecureRandom();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }

    private void enviarEmail(String destinatario, String nomeUsuario, String codigo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destinatario);
        mensagem.setSubject("Arbor - Código de recuperação de senha");
        mensagem.setText(
                "Olá, " + nomeUsuario + "!\n\n" +
                "Seu código de recuperação de senha é:\n\n" +
                "  " + codigo + "\n\n" +
                "Este código é válido por 15 minutos.\n" +
                "Se você não solicitou a recuperação, ignore este e-mail.\n\n" +
                "— Equipe Arbor"
        );
        mailSender.send(mensagem);
    }
}
