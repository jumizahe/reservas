package edu.unimagdalena.reservas.api.dto.response;

public record OfficeOccupancyResponse(
        Long   officeId,
        String officeName,
        Long   totalAppointments
) {}
