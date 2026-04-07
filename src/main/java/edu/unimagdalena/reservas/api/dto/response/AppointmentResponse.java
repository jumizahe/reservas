package edu.unimagdalena.reservas.api.dto.response;

import edu.unimagdalena.reservas.domine.enums.AppointmentStatus;
import java.time.Instant;
import java.time.LocalDateTime;

public record AppointmentResponse(
        Long              id,
        Long              patientId,
        String            patientName,
        Long              doctorId,
        String            doctorName,
        Long              officeId,
        String            officeName,
        Long              appointmentTypeId,
        String            appointmentTypeName,
        LocalDateTime     startAt,
        LocalDateTime     endAt,
        AppointmentStatus status,
        String            cancellationReason,
        String            observations,
        Instant           createdAt,
        Instant           updatedAt
) {}
