package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.domine.entities.*;
import edu.unimagdalena.reservas.domine.enums.AppointmentStatus;
import edu.unimagdalena.reservas.domine.repositories.*;
import edu.unimagdalena.reservas.exception.BusinessException;
import edu.unimagdalena.reservas.services.impl.AvailabilityServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock DoctorRepository         doctorRepo;
    @Mock DoctorScheduleRepository scheduleRepo;
    @Mock AppointmentRepository    appointmentRepo;
    @InjectMocks AvailabilityServiceImpl service;

    private static final LocalDate NEXT_MONDAY =
            LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    @Test
    @DisplayName("getAvailableSlots: devuelve todos los slots cuando no hay citas")
    void shouldReturnAllSlotsWhenNoneBooked() {
        // Given — horario 08:00–10:00, slots de 30 min → 4 slots
        var specialty = Specialty.builder().id(1L).name("Medicina").build();
        var doctor    = Doctor.builder().id(1L).fullName("Dr. Test").specialty(specialty).active(true).build();
        var schedule  = DoctorSchedule.builder().doctor(doctor).dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0)).build();

        when(doctorRepo.findById(1L)).thenReturn(Optional.of(doctor));
        when(scheduleRepo.findByDoctorIdAndDayOfWeek(eq(1L), eq(DayOfWeek.MONDAY)))
                .thenReturn(Optional.of(schedule));
        when(appointmentRepo.findByDoctorIdAndDate(eq(1L), any())).thenReturn(List.of());

        // When
        var slots = service.getAvailableSlots(1L, NEXT_MONDAY, 30);

        // Then
        assertThat(slots).hasSize(4);
        assertThat(slots.get(0).startAt()).isEqualTo(NEXT_MONDAY.atTime(8, 0));
        assertThat(slots.get(0).endAt()).isEqualTo(NEXT_MONDAY.atTime(8, 30));
    }

    @Test
    @DisplayName("getAvailableSlots: excluye el slot ocupado por una cita existente")
    void shouldExcludeBookedSlot() {
        // Given — horario 08:00–10:00, cita existente 08:00–08:30
        var specialty = Specialty.builder().id(1L).name("Medicina").build();
        var doctor    = Doctor.builder().id(1L).fullName("Dr. Test").specialty(specialty).active(true).build();
        var schedule  = DoctorSchedule.builder().doctor(doctor).dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0)).build();

        var bookedStart = NEXT_MONDAY.atTime(8, 0);
        var booked = Appointment.builder()
                .startAt(bookedStart).endAt(bookedStart.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED).build();

        when(doctorRepo.findById(1L)).thenReturn(Optional.of(doctor));
        when(scheduleRepo.findByDoctorIdAndDayOfWeek(eq(1L), eq(DayOfWeek.MONDAY)))
                .thenReturn(Optional.of(schedule));
        when(appointmentRepo.findByDoctorIdAndDate(eq(1L), any())).thenReturn(List.of(booked));

        // When
        var slots = service.getAvailableSlots(1L, NEXT_MONDAY, 30);

        // Then — 3 slots disponibles de 4 totales
        assertThat(slots).hasSize(3);
        assertThat(slots).noneMatch(s -> s.startAt().equals(bookedStart));
    }

    @Test
    @DisplayName("getAvailableSlots: lanza BusinessException si doctor está inactivo")
    void shouldThrowWhenDoctorInactive() {
        var specialty = Specialty.builder().id(1L).name("Medicina").build();
        var inactive  = Doctor.builder().id(1L).fullName("Dr. Off").specialty(specialty).active(false).build();
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.getAvailableSlots(1L, NEXT_MONDAY, 30))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("getAvailableSlots: lanza BusinessException si duración < 5 minutos")
    void shouldThrowWhenDurationTooShort() {
        assertThatThrownBy(() -> service.getAvailableSlots(1L, NEXT_MONDAY, 3))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Minimum slot");
    }
}
