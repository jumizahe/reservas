package edu.unimagdalena.reservas.api.dto.response;

public record NoShowPatientResponse(
        Long   patientId,
        String fullName,
        String documentNumber,
        Long   noShowCount
) {}
