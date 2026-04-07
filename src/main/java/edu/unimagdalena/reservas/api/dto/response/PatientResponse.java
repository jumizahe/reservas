package edu.unimagdalena.reservas.api.dto.response;

import edu.unimagdalena.reservas.domine.enums.PatientStatus;
import java.time.Instant;

public record PatientResponse(
        Long          id,
        String        fullName,
        String        documentNumber,
        String        email,
        String        phone,
        PatientStatus status,
        Instant       createdAt,
        Instant       updatedAt
) {}
