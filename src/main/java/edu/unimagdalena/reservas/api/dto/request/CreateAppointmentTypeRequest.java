package edu.unimagdalena.reservas.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppointmentTypeRequest(
        @NotBlank @Size(max = 100) String  name,
        @NotNull @Min(5)           Integer durationMinutes
) {}
