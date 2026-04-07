package edu.unimagdalena.reservas.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePatientRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(max = 20)  String documentNumber,
        @NotBlank @Email           String email,
        @Size(max = 20)            String phone
) {}
