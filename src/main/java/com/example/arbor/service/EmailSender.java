package com.example.arbor.service;

public interface EmailSender {
    void enviar(String destinatario, String assunto, String corpo);
}
