package edu.unimagdalena.reservas.domine.repositories;

import edu.unimagdalena.reservas.domine.entities.*;
import edu.unimagdalena.reservas.domine.enums.AppointmentStatus;
import edu.unimagdalena.reservas.domine.enums.OfficeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OfficeRepositoryIntegrationTest extends AbstractRepositoryIT {

    @Autowired OfficeRepository          officeRepo;
    @Autowired AppointmentRepository     appointmentRepo;
    @Autowired PatientRepository         patientRepo;
    @Autowired DoctorRepository          doctorRepo;
    @Autowired AppointmentTypeRepository typeRepo;
    @Autowired SpecialtyRepository       specialtyRepo;

    private Office          office1;
    private Office          office2;
    private Patient         patient;
    private Doctor          doctor;
    private AppointmentType type;

    @BeforeEach
    void setUp() {
        office1 = officeRepo.save(Office.builder().name("Consultorio A").build());
        office2 = officeRepo.save(Office.builder().name("Consultorio B").status(OfficeStatus.MAINTENANCE).build());
        var specialty = specialtyRepo.save(Specialty.builder().name("Medicina").build());
        patient = patientRepo.save(Patient.builder().fullName("P Test")
                .documentNumber("77777777").email("p@demo.com").build());
        doctor  = doctorRepo.save(Doctor.builder().fullName("D Test")
                .email("d@demo.com").specialty(specialty).build());
        type    = typeRepo.save(AppointmentType.builder().name("Básica").durationMinutes(20).build());
    }

    @Test
    @DisplayName("findByStatus: retorna consultorios por estado")
    void shouldFindByStatus() {
        var available   = officeRepo.findByStatus(OfficeStatus.AVAILABLE);
        var maintenance = officeRepo.findByStatus(OfficeStatus.MAINTENANCE);

        assertThat(available).hasSize(1);
        assertThat(available.get(0).getName()).isEqualTo("Consultorio A");
        assertThat(maintenance).hasSize(1);
    }

    @Test
    @DisplayName("findOfficeOccupancy: calcula ocupación de consultorios por rango")
    void shouldCalculateOccupancy() {
        // Given — 2 citas en office1, 1 en office2
        var base = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        var a1 = Appointment.builder().patient(patient).doctor(doctor).office(office1)
                .appointmentType(type).startAt(base).endAt(base.plusMinutes(20))
                .status(AppointmentStatus.COMPLETED).build();
        var a2 = Appointment.builder().patient(patient).doctor(doctor).office(office1)
                .appointmentType(type).startAt(base.plusHours(1)).endAt(base.plusHours(1).plusMinutes(20))
                .status(AppointmentStatus.CONFIRMED).build();
        var a3 = Appointment.builder().patient(patient).doctor(doctor).office(office2)
                .appointmentType(type).startAt(base.plusHours(2)).endAt(base.plusHours(2).plusMinutes(20))
                .status(AppointmentStatus.SCHEDULED).build();
        appointmentRepo.saveAll(List.of(a1, a2, a3));

        // When
        var occupancy = officeRepo.findOccupancyByDateRange(base.minusMinutes(1), base.plusHours(4));

        // Then
        assertThat(occupancy).isNotEmpty();
        var firstRow = occupancy.get(0);
        assertThat(((Number) firstRow[2]).longValue()).isGreaterThanOrEqualTo(2L);
    }
}
