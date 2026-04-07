package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.response.AvailabilitySlotResponse;
import edu.unimagdalena.reservas.api.error.GlobalExceptionHandler;
import edu.unimagdalena.reservas.exception.BusinessException;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.services.AvailabilityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvailabilityController.class)
@Import(GlobalExceptionHandler.class)
class AvailabilityControllerTest {

    @Autowired MockMvc              mvc;
    @MockBean  AvailabilityService  service;

    @Test
    @DisplayName("GET /api/availability/doctors/{id} → 200 con lista de slots")
    void shouldReturnSlots() throws Exception {
        var tomorrow = LocalDate.now().plusDays(1);
        var start    = tomorrow.atTime(8, 0);
        var slots    = List.of(
                new AvailabilitySlotResponse(start, start.plusMinutes(30), 1L, "Dr. Test"),
                new AvailabilitySlotResponse(start.plusMinutes(30), start.plusMinutes(60), 1L, "Dr. Test"));
        when(service.getAvailableSlots(eq(1L), eq(tomorrow), eq(30))).thenReturn(slots);

        mvc.perform(get("/api/availability/doctors/1")
                        .param("date", tomorrow.toString())
                        .param("durationMinutes", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Test"));
    }

    @Test
    @DisplayName("GET /api/availability/doctors/{id} → 404 cuando doctor no existe")
    void shouldReturn404WhenDoctorNotFound() throws Exception {
        when(service.getAvailableSlots(eq(99L), any(), anyInt()))
                .thenThrow(new ResourceNotFoundException("Doctor", 99L));

        mvc.perform(get("/api/availability/doctors/99")
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/availability/doctors/{id} → 422 cuando doctor no tiene horario ese día")
    void shouldReturn422WhenNoSchedule() throws Exception {
        when(service.getAvailableSlots(eq(1L), any(), anyInt()))
                .thenThrow(new BusinessException("Doctor has no schedule for SUNDAY"));

        mvc.perform(get("/api/availability/doctors/1")
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Doctor has no schedule for SUNDAY"));
    }

    @Test
    @DisplayName("GET /api/availability/doctors/{id} → 200 con lista vacía si no hay slots libres")
    void shouldReturnEmptyWhenFullyBooked() throws Exception {
        when(service.getAvailableSlots(eq(1L), any(), anyInt())).thenReturn(List.of());

        mvc.perform(get("/api/availability/doctors/1")
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
