package edu.unimagdalena.reservas.api.dto.response;

import java.time.LocalDateTime;

public record AvailabilitySlotResponse(
        LocalDateTime startAt,
        LocalDateTime endAt,
        Long          doctorId,
        String        doctorName
) {}
