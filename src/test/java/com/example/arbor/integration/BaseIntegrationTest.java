package com.example.arbor.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Classe base para testes de integração.
 *
 * Usa o banco PostgreSQL/PostGIS local (porta 5434) em vez de Testcontainers,
 * porque o Docker Desktop no Windows não expõe o socket de forma compatível
 * com o Testcontainers neste ambiente.
 *
 * Isolamento garantido por @Transactional nas subclasses — cada teste faz
 * rollback automático no fim, sem deixar dados no banco.
 *
 * Pré-requisito: banco local a correr com as migrações Flyway já aplicadas.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5434/arbor_db?sslmode=disable",
        "spring.datasource.username=arbor_user",
        "spring.datasource.password=arbor_password",
        "spring.mail.host=localhost",
        "spring.mail.port=25",
        "spring.mail.username=",
        "spring.mail.password=",
        "spring.flyway.enabled=false"
})
public abstract class BaseIntegrationTest {
}