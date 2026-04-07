package edu.unimagdalena.reservas.api.dto.response;

import java.time.Instant;

public record DoctorResponse(
        Long    id,
        String  fullName,
        String  email,
        String  licenseNumber,
        Boolean active,
        Long    specialtyId,
        String  specialtyName,
        Instant createdAt,
        Instant updatedAt
) {}
