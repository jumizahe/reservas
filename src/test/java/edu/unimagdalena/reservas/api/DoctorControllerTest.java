package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.response.DoctorResponse;
import edu.unimagdalena.reservas.api.error.GlobalExceptionHandler;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.services.DoctorService;
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

@WebMvcTest(DoctorController.class)
@Import(GlobalExceptionHandler.class)
class DoctorControllerTest {

    @Autowired MockMvc       mvc;
    @MockBean  DoctorService service;

    private DoctorResponse sample() {
        return new DoctorResponse(1L, "Dr. García", "garcia@d.com", "LIC-001",
                true, 1L, "Medicina General", Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("POST /api/doctors → 201 Created")
    void shouldCreate() throws Exception {
        when(service.create(any())).thenReturn(sample());

        mvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Dr. García\",\"email\":\"garcia@d.com\",\"specialtyId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Dr. García"));
    }

    @Test
    @DisplayName("POST /api/doctors → 400 cuando faltan campos requeridos")
    void shouldReturn400WhenMissingFields() throws Exception {
        mvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/doctors → 200 con lista paginada")
    void shouldList() throws Exception {
        when(service.list(any())).thenReturn(new PageImpl<>(List.of(sample())));

        mvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/doctors/{id} → 404 cuando no existe")
    void shouldReturn404() throws Exception {
        when(service.get(99L)).thenThrow(new ResourceNotFoundException("Doctor", 99L));

        mvc.perform(get("/api/doctors/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/doctors/by-specialty/{id} → 200 con lista filtrada")
    void shouldListBySpecialty() throws Exception {
        when(service.listBySpecialty(1L)).thenReturn(List.of(sample()));

        mvc.perform(get("/api/doctors/by-specialty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialtyName").value("Medicina General"));
    }
}
