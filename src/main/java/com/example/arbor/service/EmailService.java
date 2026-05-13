package com.example.arbor.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailReset(String destino, String token) {

        String link = "http://localhost:8080/resetar-senha?token=" + token;

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destino);
        mensagem.setSubject("Recuperação de senha");
        mensagem.setText("Clique no link para redefinir sua senha:\n" + link);

        mailSender.send(mensagem);
    }
}