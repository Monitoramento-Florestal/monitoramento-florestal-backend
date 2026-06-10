package com.example.arbor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
@Profile("!dev")
public class BrevoEmailSender implements EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrevoEmailSender.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public BrevoEmailSender(
            @Value("${brevo.api.key:}") String apiKey,
            @Value("${brevo.sender.email:arbor@ufrpe.br}") String senderEmail,
            @Value("${brevo.sender.name:Arbor}") String senderName) {
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void enviar(String destinatario, String assunto, String corpo) {
        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn("BREVO_API_KEY nao configurada. Email nao enviado.");
            return;
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "sender", Map.of("email", senderEmail, "name", senderName),
                    "to", Collections.singletonList(Map.of("email", destinatario)),
                    "subject", assunto,
                    "textContent", corpo
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody), headers);

            restTemplate.postForObject(BREVO_API_URL, entity, String.class);
            LOGGER.info("Email enviado via Brevo para: {}", destinatario);
        } catch (Exception ex) {
            LOGGER.warn("Falha ao enviar email via Brevo para {}: {}", destinatario, ex.getMessage());
        }
    }
}
