package edu.unimagdalena.reservas.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelAppointmentRequest(
        @NotBlank @Size(max = 300) String reason
) {}
