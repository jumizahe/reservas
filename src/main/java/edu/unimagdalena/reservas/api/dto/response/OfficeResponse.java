package edu.unimagdalena.reservas.api.dto.response;

import edu.unimagdalena.reservas.domine.enums.OfficeStatus;

public record OfficeResponse(
        Long         id,
        String       name,
        String       location,
        OfficeStatus status
) {}
