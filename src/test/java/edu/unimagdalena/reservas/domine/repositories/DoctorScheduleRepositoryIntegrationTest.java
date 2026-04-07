package edu.unimagdalena.reservas.domine.repositories;

import edu.unimagdalena.reservas.domine.entities.Doctor;
import edu.unimagdalena.reservas.domine.entities.DoctorSchedule;
import edu.unimagdalena.reservas.domine.entities.Specialty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorScheduleRepositoryIntegrationTest extends AbstractRepositoryIT {

    @Autowired DoctorScheduleRepository scheduleRepo;
    @Autowired DoctorRepository         doctorRepo;
    @Autowired SpecialtyRepository      specialtyRepo;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        var specialty = specialtyRepo.save(Specialty.builder().name("Fisioterapia").build());
        doctor = doctorRepo.save(Doctor.builder().fullName("Dr. Fisio").email("fisio@demo.com")
                .specialty(specialty).build());
    }

    @Test
    @DisplayName("findByDoctorId: lista todos los horarios de un doctor")
    void shouldListSchedulesByDoctor() {
        // Given
        scheduleRepo.save(DoctorSchedule.builder().doctor(doctor).dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build());
        scheduleRepo.save(DoctorSchedule.builder().doctor(doctor).dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(18, 0)).build());

        // When
        var result = scheduleRepo.findByDoctorId(doctor.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("dayOfWeek")
                .containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    }

    @Test
    @DisplayName("findByDoctorIdAndDayOfWeek: encuentra horario específico del día")
    void shouldFindByDoctorAndDay() {
        // Given
        scheduleRepo.save(DoctorSchedule.builder().doctor(doctor).dayOfWeek(DayOfWeek.FRIDAY)
                .startTime(LocalTime.of(7, 0)).endTime(LocalTime.of(11, 0)).build());

        // When
        var result = scheduleRepo.findByDoctorIdAndDayOfWeek(doctor.getId(), DayOfWeek.FRIDAY);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStartTime()).isEqualTo(LocalTime.of(7, 0));
    }

    @Test
    @DisplayName("existsByDoctorIdAndDayOfWeek: detecta duplicado correctamente")
    void shouldDetectDuplicate() {
        // Given
        scheduleRepo.save(DoctorSchedule.builder().doctor(doctor).dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build());

        // When / Then
        assertThat(scheduleRepo.existsByDoctorIdAndDayOfWeek(doctor.getId(), DayOfWeek.TUESDAY)).isTrue();
        assertThat(scheduleRepo.existsByDoctorIdAndDayOfWeek(doctor.getId(), DayOfWeek.THURSDAY)).isFalse();
    }
}
