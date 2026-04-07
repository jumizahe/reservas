package edu.unimagdalena.reservas.api.dto.response;

public record AppointmentTypeResponse(
        Long    id,
        String  name,
        Integer durationMinutes
) {}
