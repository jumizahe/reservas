package edu.unimagdalena.reservas.domine.repositories;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base para tests de repositorios.
 * Levanta Postgres real en contenedor Docker vía Testcontainers.
 *
 * REQUISITO: Docker Desktop debe estar corriendo.
 * Para activar estos tests: mvn test -Ddocker.available=true
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "docker.available", matches = "true")
public abstract class AbstractRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
}
