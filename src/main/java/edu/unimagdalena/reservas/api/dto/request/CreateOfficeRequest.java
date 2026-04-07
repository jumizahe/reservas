package edu.unimagdalena.reservas.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOfficeRequest(
        @NotBlank @Size(max = 50)  String name,
        @Size(max = 200)           String location
) {}
