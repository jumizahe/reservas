package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.request.CancelAppointmentRequest;
import edu.unimagdalena.reservas.api.dto.request.CreateAppointmentRequest;
import edu.unimagdalena.reservas.domine.entities.*;
import edu.unimagdalena.reservas.domine.enums.AppointmentStatus;
import edu.unimagdalena.reservas.domine.enums.OfficeStatus;
import edu.unimagdalena.reservas.domine.enums.PatientStatus;
import edu.unimagdalena.reservas.domine.repositories.*;
import edu.unimagdalena.reservas.exception.BusinessException;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.services.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock AppointmentRepository     appointmentRepo;
    @Mock PatientRepository         patientRepo;
    @Mock DoctorRepository          doctorRepo;
    @Mock OfficeRepository          officeRepo;
    @Mock AppointmentTypeRepository appointmentTypeRepo;
    @Mock DoctorScheduleRepository  scheduleRepo;
    @InjectMocks AppointmentServiceImpl service;

    private static final LocalDateTime TOMORROW_10 =
            LocalDateTime.now().plusDays(1).with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
                    .withHour(10).withMinute(0).withSecond(0).withNano(0);

    private Patient         activePatient;
    private Doctor          activeDoctor;
    private Office          availableOffice;
    private AppointmentType type30min;
    private DoctorSchedule  mondaySchedule;

    @BeforeEach
    void setUp() {
        var specialty = Specialty.builder().id(1L).name("Medicina").build();
        activePatient    = Patient.builder().id(1L).fullName("Ana").email("ana@d.com")
                .documentNumber("111").status(PatientStatus.ACTIVE).build();
        activeDoctor     = Doctor.builder().id(2L).fullName("Dr. García").email("garcia@d.com")
                .specialty(specialty).active(true).build();
        availableOffice  = Office.builder().id(3L).name("C1").status(OfficeStatus.AVAILABLE).build();
        type30min        = AppointmentType.builder().id(4L).name("General").durationMinutes(30).build();
        mondaySchedule   = DoctorSchedule.builder().id(5L).doctor(activeDoctor)
                .dayOfWeek(TOMORROW_10.getDayOfWeek())
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(17, 0)).build();
    }

    private void mockHappyPath() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(activePatient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(activeDoctor));
        when(officeRepo.findById(3L)).thenReturn(Optional.of(availableOffice));
        when(appointmentTypeRepo.findById(4L)).thenReturn(Optional.of(type30min));
        when(scheduleRepo.findByDoctorIdAndDayOfWeek(eq(2L), any())).thenReturn(Optional.of(mondaySchedule));
        when(appointmentRepo.existsDoctorOverlap(any(), any(), any())).thenReturn(false);
        when(appointmentRepo.existsOfficeOverlap(any(), any(), any())).thenReturn(false);
        when(appointmentRepo.existsPatientOverlap(any(), any(), any())).thenReturn(false);
        when(appointmentRepo.save(any())).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });
    }

    @Test
    @DisplayName("create: guarda cita y calcula endAt con duración del tipo")
    void shouldCreateAppointmentAndCalculateEndAt() {
        mockHappyPath();
        var req = new CreateAppointmentRequest(1L, 2L, 3L, 4L, TOMORROW_10);

        var result = service.create(req);

        assertThat(result.id()).isEqualTo(99L);
        assertThat(result.endAt()).isEqualTo(TOMORROW_10.plusMinutes(30));
        assertThat(result.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(appointmentRepo).save(any(Appointment.class));
    }

    @Test
    @DisplayName("create: lanza BusinessException si paciente está INACTIVO")
    void shouldThrowWhenPatientInactive() {
        activePatient.setStatus(PatientStatus.INACTIVE);
        when(patientRepo.findById(1L)).thenReturn(Optional.of(activePatient));

        assertThatThrownBy(() -> service.create(new CreateAppointmentRequest(1L, 2L, 3L, 4L, TOMORROW_10)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("create: lanza BusinessException si doctor está INACTIVO")
    void shouldThrowWhenDoctorInactive() {
        activeDoctor.setActive(false);
        when(patientRepo.findById(1L)).thenReturn(Optional.of(activePatient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(activeDoctor));

        assertThatThrownBy(() -> service.create(new CreateAppointmentRequest(1L, 2L, 3L, 4L, TOMORROW_10)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("create: lanza ConflictException si hay traslape con doctor")
    void shouldThrowOnDoctorOverlap() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(activePatient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(activeDoctor));
        when(officeRepo.findById(3L)).thenReturn(Optional.of(availableOffice));
        when(appointmentTypeRepo.findById(4L)).thenReturn(Optional.of(type30min));
        when(scheduleRepo.findByDoctorIdAndDayOfWeek(eq(2L), any())).thenReturn(Optional.of(mondaySchedule));
        when(appointmentRepo.existsDoctorOverlap(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateAppointmentRequest(1L, 2L, 3L, 4L, TOMORROW_10)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Doctor already has");
    }

    @Test
    @DisplayName("create: lanza ConflictException si hay traslape con consultorio")
    void shouldThrowOnOfficeOverlap() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(activePatient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(activeDoctor));
        when(officeRepo.findById(3L)).thenReturn(Optional.of(availableOffice));
        when(appointmentTypeRepo.findById(4L)).thenReturn(Optional.of(type30min));
        when(scheduleRepo.findByDoctorIdAndDayOfWeek(eq(2L), any())).thenReturn(Optional.of(mondaySchedule));
        when(appointmentRepo.existsDoctorOverlap(any(), any(), any())).thenReturn(false);
        when(appointmentRepo.existsOfficeOverlap(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateAppointmentRequest(1L, 2L, 3L, 4L, TOMORROW_10)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Office is already");
    }

    @Test
    @DisplayName("create: lanza BusinessException si la cita está fuera del horario del doctor")
    void shouldThrowWhenOutsideDoctorSchedule() {
        when(patientRepo.findById(1L)).thenReturn(Optional.of(activePatient));
        when(doctorRepo.findById(2L)).thenReturn(Optional.of(activeDoctor));
        when(officeRepo.findById(3L)).thenReturn(Optional.of(availableOffice));
        when(appointmentTypeRepo.findById(4L)).thenReturn(Optional.of(type30min));
        // Horario solo de 08:00–09:00, la cita a las 10:00 quedaría fuera
        var narrowSchedule = DoctorSchedule.builder().doctor(activeDoctor)
                .dayOfWeek(TOMORROW_10.getDayOfWeek())
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(9, 0)).build();
        when(scheduleRepo.findByDoctorIdAndDayOfWeek(eq(2L), any())).thenReturn(Optional.of(narrowSchedule));

        assertThatThrownBy(() -> service.create(new CreateAppointmentRequest(1L, 2L, 3L, 4L, TOMORROW_10)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("outside the doctor");
    }

    @Test
    @DisplayName("confirm: SCHEDULED → CONFIRMED correctamente")
    void shouldConfirmScheduledAppointment() {
        var appt = Appointment.builder().id(10L).patient(activePatient).doctor(activeDoctor)
                .office(availableOffice).appointmentType(type30min)
                .startAt(TOMORROW_10).endAt(TOMORROW_10.plusMinutes(30))
                .status(AppointmentStatus.SCHEDULED).build();
        when(appointmentRepo.findById(10L)).thenReturn(Optional.of(appt));

        var result = service.confirm(10L);

        assertThat(result.status()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("confirm: lanza BusinessException si ya está CANCELLED")
    void shouldThrowWhenConfirmingCancelled() {
        var appt = Appointment.builder().id(10L).patient(activePatient).doctor(activeDoctor)
                .office(availableOffice).appointmentType(type30min)
                .startAt(TOMORROW_10).endAt(TOMORROW_10.plusMinutes(30))
                .status(AppointmentStatus.CANCELLED).build();
        when(appointmentRepo.findById(10L)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> service.confirm(10L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("cancel: CONFIRMED → CANCELLED con motivo")
    void shouldCancelWithReason() {
        var appt = Appointment.builder().id(10L).patient(activePatient).doctor(activeDoctor)
                .office(availableOffice).appointmentType(type30min)
                .startAt(TOMORROW_10).endAt(TOMORROW_10.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED).build();
        when(appointmentRepo.findById(10L)).thenReturn(Optional.of(appt));

        var result = service.cancel(10L, new CancelAppointmentRequest("Paciente no puede asistir"));

        assertThat(result.status()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(result.cancellationReason()).isEqualTo("Paciente no puede asistir");
    }

    @Test
    @DisplayName("cancel: lanza BusinessException si ya está COMPLETED")
    void shouldThrowWhenCancellingCompleted() {
        var appt = Appointment.builder().id(10L).patient(activePatient).doctor(activeDoctor)
                .office(availableOffice).appointmentType(type30min)
                .startAt(TOMORROW_10).endAt(TOMORROW_10.plusMinutes(30))
                .status(AppointmentStatus.COMPLETED).build();
        when(appointmentRepo.findById(10L)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> service.cancel(10L, new CancelAppointmentRequest("motivo")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("complete: CONFIRMED → COMPLETED con observaciones")
    void shouldCompleteAppointment() {
        var pastStart = LocalDateTime.now().minusHours(1);
        var appt = Appointment.builder().id(10L).patient(activePatient).doctor(activeDoctor)
                .office(availableOffice).appointmentType(type30min)
                .startAt(pastStart).endAt(pastStart.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED).build();
        when(appointmentRepo.findById(10L)).thenReturn(Optional.of(appt));

        var result = service.complete(10L, "Atención normal");

        assertThat(result.status()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(result.observations()).isEqualTo("Atención normal");
    }

    @Test
    @DisplayName("complete: lanza BusinessException si la hora actual es anterior al inicio")
    void shouldThrowWhenCompletingBeforeStart() {
        var appt = Appointment.builder().id(10L).patient(activePatient).doctor(activeDoctor)
                .office(availableOffice).appointmentType(type30min)
                .startAt(TOMORROW_10).endAt(TOMORROW_10.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED).build();
        when(appointmentRepo.findById(10L)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> service.complete(10L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("before its scheduled start");
    }

    @Test
    @DisplayName("markNoShow: CONFIRMED → NO_SHOW después del inicio")
    void shouldMarkNoShow() {
        var pastStart = LocalDateTime.now().minusMinutes(5);
        var appt = Appointment.builder().id(10L).patient(activePatient).doctor(activeDoctor)
                .office(availableOffice).appointmentType(type30min)
                .startAt(pastStart).endAt(pastStart.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED).build();
        when(appointmentRepo.findById(10L)).thenReturn(Optional.of(appt));

        var result = service.markNoShow(10L);

        assertThat(result.status()).isEqualTo(AppointmentStatus.NO_SHOW);
    }

    @Test
    @DisplayName("markNoShow: lanza BusinessException antes del inicio")
    void shouldThrowNoShowBeforeStart() {
        var appt = Appointment.builder().id(10L).patient(activePatient).doctor(activeDoctor)
                .office(availableOffice).appointmentType(type30min)
                .startAt(TOMORROW_10).endAt(TOMORROW_10.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED).build();
        when(appointmentRepo.findById(10L)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> service.markNoShow(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("before the appointment start");
    }
}
