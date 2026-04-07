package edu.unimagdalena.reservas.domine.repositories;

import edu.unimagdalena.reservas.domine.entities.Patient;
import edu.unimagdalena.reservas.domine.enums.PatientStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class PatientRepositoryIntegrationTest extends AbstractRepositoryIT {

    @Autowired PatientRepository repo;

    @Test
    @DisplayName("findByEmail: encuentra paciente por email exacto")
    void shouldFindByEmail() {
        // Given
        repo.save(Patient.builder()
                .fullName("María López").documentNumber("12345678")
                .email("maria@demo.com").build());

        // When
        var result = repo.findByEmail("maria@demo.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("María López");
    }

    @Test
    @DisplayName("findByDocumentNumber: encuentra paciente por documento")
    void shouldFindByDocumentNumber() {
        // Given
        repo.save(Patient.builder()
                .fullName("Carlos Ruiz").documentNumber("98765432")
                .email("carlos@demo.com").build());

        // When
        var result = repo.findByDocumentNumber("98765432");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("carlos@demo.com");
    }

    @Test
    @DisplayName("findByStatus: lista solo pacientes activos")
    void shouldListByStatus() {
        // Given
        repo.save(Patient.builder().fullName("Activo Uno").documentNumber("11111111")
                .email("activo@demo.com").status(PatientStatus.ACTIVE).build());
        repo.save(Patient.builder().fullName("Inactivo Dos").documentNumber("22222222")
                .email("inactivo@demo.com").status(PatientStatus.INACTIVE).build());

        // When
        var actives = repo.findByStatus(PatientStatus.ACTIVE,
                org.springframework.data.domain.PageRequest.of(0, 10));

        // Then
        assertThat(actives.getContent()).hasSize(1);
        assertThat(actives.getContent().get(0).getFullName()).isEqualTo("Activo Uno");
    }

    @Test
    @DisplayName("findByEmail: retorna empty cuando no existe")
    void shouldReturnEmptyWhenNotFound() {
        assertThat(repo.findByEmail("noexiste@demo.com")).isEmpty();
    }
}
