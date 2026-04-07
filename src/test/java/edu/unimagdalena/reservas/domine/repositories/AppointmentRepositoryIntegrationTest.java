package edu.unimagdalena.reservas.domine.repositories;

import edu.unimagdalena.reservas.domine.entities.*;
import edu.unimagdalena.reservas.domine.enums.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentRepositoryIntegrationTest extends AbstractRepositoryIT {

    @Autowired AppointmentRepository     appointmentRepo;
    @Autowired PatientRepository         patientRepo;
    @Autowired DoctorRepository          doctorRepo;
    @Autowired OfficeRepository          officeRepo;
    @Autowired AppointmentTypeRepository typeRepo;
    @Autowired SpecialtyRepository       specialtyRepo;

    private Patient         patient;
    private Doctor          doctor;
    private Office          office;
    private AppointmentType type;

    @BeforeEach
    void setUp() {
        var specialty = specialtyRepo.save(Specialty.builder().name("Nutrición").build());
        patient = patientRepo.save(Patient.builder().fullName("Paciente Test")
                .documentNumber("55555555").email("paciente@demo.com").build());
        doctor  = doctorRepo.save(Doctor.builder().fullName("Dr. Test")
                .email("drtest@demo.com").specialty(specialty).build());
        office  = officeRepo.save(Office.builder().name("Consultorio 1").build());
        type    = typeRepo.save(AppointmentType.builder().name("Consulta General").durationMinutes(30).build());
    }

    private Appointment buildAppointment(LocalDateTime start, LocalDateTime end, AppointmentStatus status) {
        return Appointment.builder()
                .patient(patient).doctor(doctor).office(office).appointmentType(type)
                .startAt(start).endAt(end).status(status).build();
    }

    @Test
    @DisplayName("existsDoctorOverlap: detecta traslape de horario para el doctor")
    void shouldDetectDoctorOverlap() {
        // Given — cita existente 10:00–10:30
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var end   = start.plusMinutes(30);
        appointmentRepo.save(buildAppointment(start, end, AppointmentStatus.SCHEDULED));

        // When — intento 10:15–10:45 (solapado)
        boolean overlaps = appointmentRepo.existsDoctorOverlap(
                doctor.getId(), start.plusMinutes(15), end.plusMinutes(15));

        // Then
        assertThat(overlaps).isTrue();
    }

    @Test
    @DisplayName("existsDoctorOverlap: no detecta traslape cuando no hay solapamiento")
    void shouldNotDetectOverlapWhenClear() {
        // Given — cita 10:00–10:30
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        appointmentRepo.save(buildAppointment(start, start.plusMinutes(30), AppointmentStatus.SCHEDULED));

        // When — intento 11:00–11:30 (sin solapamiento)
        boolean overlaps = appointmentRepo.existsDoctorOverlap(
                doctor.getId(), start.plusHours(1), start.plusHours(1).plusMinutes(30));

        // Then
        assertThat(overlaps).isFalse();
    }

    @Test
    @DisplayName("existsOfficeOverlap: detecta traslape de consultorio")
    void shouldDetectOfficeOverlap() {
        // Given
        var start = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        appointmentRepo.save(buildAppointment(start, start.plusMinutes(30), AppointmentStatus.CONFIRMED));

        // When — intento en el mismo rango
        boolean overlaps = appointmentRepo.existsOfficeOverlap(
                office.getId(), start, start.plusMinutes(30));

        assertThat(overlaps).isTrue();
    }

    @Test
    @DisplayName("existsDoctorOverlap: citas CANCELLED no cuentan como traslape")
    void shouldIgnoreCancelledAppointments() {
        // Given — cita cancelada en el mismo horario
        var start = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        appointmentRepo.save(buildAppointment(start, start.plusMinutes(30), AppointmentStatus.CANCELLED));

        // When
        boolean overlaps = appointmentRepo.existsDoctorOverlap(
                doctor.getId(), start, start.plusMinutes(30));

        assertThat(overlaps).isFalse();
    }

    @Test
    @DisplayName("findByStartAtBetween: retorna citas en el rango de fecha dado")
    void shouldFindByDateRange() {
        // Given
        var base = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        appointmentRepo.save(buildAppointment(base, base.plusMinutes(30), AppointmentStatus.SCHEDULED));
        appointmentRepo.save(buildAppointment(base.plusHours(2), base.plusHours(2).plusMinutes(30),
                AppointmentStatus.SCHEDULED));

        // When
        var results = appointmentRepo.findByStartAtBetween(base.minusMinutes(1), base.plusHours(3));

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("findByPatientIdAndStatus: filtra citas del paciente por estado")
    void shouldFindByPatientAndStatus() {
        // Given
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        appointmentRepo.save(buildAppointment(start, start.plusMinutes(30), AppointmentStatus.SCHEDULED));
        appointmentRepo.save(buildAppointment(start.plusHours(2), start.plusHours(2).plusMinutes(30),
                AppointmentStatus.CANCELLED));

        // When
        var scheduled = appointmentRepo.findByPatientIdAndStatus(patient.getId(), AppointmentStatus.SCHEDULED);
        var cancelled = appointmentRepo.findByPatientIdAndStatus(patient.getId(), AppointmentStatus.CANCELLED);

        assertThat(scheduled).hasSize(1);
        assertThat(cancelled).hasSize(1);
    }
}
