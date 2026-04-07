package edu.unimagdalena.reservas.api.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DoctorScheduleResponse(
        Long       id,
        Long       doctorId,
        String     doctorName,
        DayOfWeek  dayOfWeek,
        LocalTime  startTime,
        LocalTime  endTime
) {}
