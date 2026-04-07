package edu.unimagdalena.reservas.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDoctorRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email           String email,
        @Size(max = 20)            String licenseNumber,
        @NotNull                   Long   specialtyId
) {}
