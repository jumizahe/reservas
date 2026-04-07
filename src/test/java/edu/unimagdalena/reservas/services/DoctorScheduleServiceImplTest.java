package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.request.CreateDoctorScheduleRequest;
import edu.unimagdalena.reservas.domine.entities.*;
import edu.unimagdalena.reservas.domine.repositories.*;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.exception.ValidationException;
import edu.unimagdalena.reservas.services.impl.DoctorScheduleServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceImplTest {

    @Mock DoctorScheduleRepository scheduleRepo;
    @Mock DoctorRepository         doctorRepo;
    @InjectMocks DoctorScheduleServiceImpl service;

    @Test
    @DisplayName("create: guarda horario correctamente")
    void shouldCreateSchedule() {
        var specialty = Specialty.builder().id(1L).name("Medicina").build();
        var doctor    = Doctor.builder().id(1L).fullName("Dr. García").email("g@d.com")
                .specialty(specialty).build();
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(doctor));
        when(scheduleRepo.existsByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY)).thenReturn(false);
        when(scheduleRepo.save(any())).thenAnswer(inv -> {
            DoctorSchedule ds = inv.getArgument(0);
            ds.setId(10L);
            return ds;
        });

        var req    = new CreateDoctorScheduleRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        var result = service.create(1L, req);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result.startTime()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    @DisplayName("create: lanza ConflictException si ya existe horario para ese día")
    void shouldThrowWhenDayAlreadyExists() {
        var specialty = Specialty.builder().id(1L).name("Medicina").build();
        var doctor    = Doctor.builder().id(1L).fullName("Dr. García").email("g@d.com")
                .specialty(specialty).build();
        when(doctorRepo.findById(1L)).thenReturn(Optional.of(doctor));
        when(scheduleRepo.existsByDoctorIdAndDayOfWeek(1L, DayOfWeek.MONDAY)).thenReturn(true);

        var req = new CreateDoctorScheduleRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        assertThatThrownBy(() -> service.create(1L, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("MONDAY");
    }

    @Test
    @DisplayName("create: lanza ValidationException si endTime <= startTime")
    void shouldThrowWhenEndBeforeStart() {
        var req = new CreateDoctorScheduleRequest(DayOfWeek.TUESDAY, LocalTime.of(12, 0), LocalTime.of(8, 0));

        assertThatThrownBy(() -> service.create(1L, req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("endTime must be after");
    }
}
