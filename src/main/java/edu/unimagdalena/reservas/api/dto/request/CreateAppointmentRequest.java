package edu.unimagdalena.reservas.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateAppointmentRequest(
        @NotNull              Long          patientId,
        @NotNull              Long          doctorId,
        @NotNull              Long          officeId,
        @NotNull              Long          appointmentTypeId,
        @NotNull @Future      LocalDateTime startAt
        // endAt NO se recibe del cliente — lo calcula el servicio con la duración del tipo de cita
) {}
