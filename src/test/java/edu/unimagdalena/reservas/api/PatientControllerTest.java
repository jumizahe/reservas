package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.response.PatientResponse;
import edu.unimagdalena.reservas.api.error.GlobalExceptionHandler;
import edu.unimagdalena.reservas.domine.enums.PatientStatus;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.services.PatientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@Import(GlobalExceptionHandler.class)
class PatientControllerTest {

    @Autowired MockMvc         mvc;
    @MockBean  PatientService  service;

    private PatientResponse sample() {
        return new PatientResponse(1L, "Ana López", "12345678", "ana@d.com",
                "+57300", PatientStatus.ACTIVE, Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("POST /api/patients → 201 Created")
    void shouldCreate() throws Exception {
        when(service.create(any())).thenReturn(sample());

        mvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana López\",\"documentNumber\":\"12345678\",\"email\":\"ana@d.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Ana López"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/patients → 409 si el email ya existe")
    void shouldReturn409OnDuplicateEmail() throws Exception {
        when(service.create(any())).thenThrow(new ConflictException("Email already registered"));

        mvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana\",\"documentNumber\":\"99\",\"email\":\"ana@d.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/patients → 400 cuando email es inválido")
    void shouldReturn400OnBadEmail() throws Exception {
        mvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana\",\"documentNumber\":\"99\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/patients → 200 con lista paginada")
    void shouldList() throws Exception {
        when(service.list(any())).thenReturn(new PageImpl<>(List.of(sample())));

        mvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/patients/{id} → 404 cuando no existe")
    void shouldReturn404() throws Exception {
        when(service.get(99L)).thenThrow(new ResourceNotFoundException("Patient", 99L));

        mvc.perform(get("/api/patients/99"))
                .andExpect(status().isNotFound());
    }
}
