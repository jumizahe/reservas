package edu.unimagdalena.reservas.api.dto.response;

public record DoctorProductivityResponse(
        Long   doctorId,
        String doctorName,
        String specialtyName,
        Long   completedAppointments
) {}
