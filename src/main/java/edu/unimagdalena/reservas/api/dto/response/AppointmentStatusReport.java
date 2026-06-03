package edu.unimagdalena.reservas.api.dto.response;

public record AppointmentStatusReport(
        String status,
        Long count
) {}
