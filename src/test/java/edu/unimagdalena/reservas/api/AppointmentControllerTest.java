package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.response.AppointmentResponse;
import edu.unimagdalena.reservas.api.error.GlobalExceptionHandler;
import edu.unimagdalena.reservas.domine.enums.AppointmentStatus;
import edu.unimagdalena.reservas.exception.BusinessException;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.services.AppointmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@Import(GlobalExceptionHandler.class)
class AppointmentControllerTest {

    @Autowired MockMvc             mvc;
    @MockBean  AppointmentService  service;

    private AppointmentResponse sample(AppointmentStatus status) {
        return new AppointmentResponse(1L, 1L, "Ana", 2L, "Dr. García", 3L, "C1",
                4L, "General", LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusMinutes(30),
                status, null, null, Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("POST /api/appointments → 201 Created")
    void shouldCreate() throws Exception {
        when(service.create(any())).thenReturn(sample(AppointmentStatus.SCHEDULED));

        var body = """
                {"patientId":1,"doctorId":2,"officeId":3,"appointmentTypeId":4,
                 "startAt":"%s"}
                """.formatted(LocalDateTime.now().plusDays(1));

        mvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("POST /api/appointments → 409 si hay traslape")
    void shouldReturn409OnOverlap() throws Exception {
        when(service.create(any())).thenThrow(new ConflictException("Doctor already has an appointment"));

        var body = """
                {"patientId":1,"doctorId":2,"officeId":3,"appointmentTypeId":4,
                 "startAt":"%s"}
                """.formatted(LocalDateTime.now().plusDays(1));

        mvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/appointments/{id} → 200 OK")
    void shouldGet() throws Exception {
        when(service.get(1L)).thenReturn(sample(AppointmentStatus.CONFIRMED));

        mvc.perform(get("/api/appointments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/appointments/{id} → 404 cuando no existe")
    void shouldReturn404() throws Exception {
        when(service.get(99L)).thenThrow(new ResourceNotFoundException("Appointment", 99L));

        mvc.perform(get("/api/appointments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/confirm → 200 OK")
    void shouldConfirm() throws Exception {
        when(service.confirm(1L)).thenReturn(sample(AppointmentStatus.CONFIRMED));

        mvc.perform(put("/api/appointments/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/confirm → 422 si estado inválido")
    void shouldReturn422OnBadConfirm() throws Exception {
        when(service.confirm(1L)).thenThrow(new BusinessException("Only SCHEDULED appointments can be confirmed"));

        mvc.perform(put("/api/appointments/1/confirm"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/cancel → 200 con motivo")
    void shouldCancel() throws Exception {
        when(service.cancel(any(), any())).thenReturn(sample(AppointmentStatus.CANCELLED));

        mvc.perform(put("/api/appointments/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"No puede asistir\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/cancel → 400 sin motivo")
    void shouldReturn400WithoutReason() throws Exception {
        mvc.perform(put("/api/appointments/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/no-show → 200 OK")
    void shouldMarkNoShow() throws Exception {
        when(service.markNoShow(1L)).thenReturn(sample(AppointmentStatus.NO_SHOW));

        mvc.perform(put("/api/appointments/1/no-show"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }
}
