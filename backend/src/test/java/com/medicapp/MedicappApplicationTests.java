package com.medicapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Arranca el contexto completo (incluye conexión a PostgreSQL y migraciones de Flyway).
 * Solo se ejecuta si la variable DB_URL está definida, para que ./mvnw verify
 * funcione sin base de datos (por ejemplo, en la CI).
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class MedicappApplicationTests {

    @Test
    void contextLoads() {
    }
}
