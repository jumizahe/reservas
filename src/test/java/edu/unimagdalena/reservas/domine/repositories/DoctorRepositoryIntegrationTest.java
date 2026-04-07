package edu.unimagdalena.reservas.domine.repositories;

import edu.unimagdalena.reservas.domine.entities.Doctor;
import edu.unimagdalena.reservas.domine.entities.Specialty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorRepositoryIntegrationTest extends AbstractRepositoryIT {

    @Autowired DoctorRepository    doctorRepo;
    @Autowired SpecialtyRepository specialtyRepo;

    private Specialty medicina;
    private Specialty psicologia;

    @BeforeEach
    void setUp() {
        medicina   = specialtyRepo.save(Specialty.builder().name("Medicina General").build());
        psicologia = specialtyRepo.save(Specialty.builder().name("Psicología").build());
    }

    @Test
    @DisplayName("findBySpecialtyIdAndActiveTrue: retorna solo doctores activos de la especialidad")
    void shouldFindActiveBySpecialty() {
        // Given
        doctorRepo.save(Doctor.builder().fullName("Dr. Activo").email("activo@demo.com")
                .specialty(medicina).active(true).build());
        doctorRepo.save(Doctor.builder().fullName("Dr. Inactivo").email("inactivo@demo.com")
                .specialty(medicina).active(false).build());
        doctorRepo.save(Doctor.builder().fullName("Dr. Psico").email("psico@demo.com")
                .specialty(psicologia).active(true).build());

        // When
        var result = doctorRepo.findBySpecialtyIdAndActiveTrue(medicina.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("Dr. Activo");
    }

    @Test
    @DisplayName("findByEmail: encuentra doctor por email")
    void shouldFindByEmail() {
        // Given
        doctorRepo.save(Doctor.builder().fullName("Dr. Email").email("doctor@demo.com")
                .specialty(medicina).build());

        // When / Then
        assertThat(doctorRepo.findByEmail("doctor@demo.com")).isPresent();
    }

    @Test
    @DisplayName("findByActiveTrue: retorna solo doctores activos paginados")
    void shouldListActiveDoctors() {
        // Given
        doctorRepo.save(Doctor.builder().fullName("Dr. A").email("a@demo.com")
                .specialty(medicina).active(true).build());
        doctorRepo.save(Doctor.builder().fullName("Dr. B").email("b@demo.com")
                .specialty(medicina).active(false).build());

        // When
        var page = doctorRepo.findByActiveTrue(
                org.springframework.data.domain.PageRequest.of(0, 10));

        // Then
        assertThat(page.getContent()).hasSize(1);
    }
}
