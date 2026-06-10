package com.example.arbor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class LogEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogEmailSender.class);

    @Override
    public void enviar(String destinatario, String assunto, String corpo) {
        LOGGER.info("========================================");
        LOGGER.info("DEV EMAIL — Para: {}", destinatario);
        LOGGER.info("DEV EMAIL — Assunto: {}", assunto);
        LOGGER.info("DEV EMAIL — Corpo:\n{}", corpo);
        LOGGER.info("========================================");
    }
}
