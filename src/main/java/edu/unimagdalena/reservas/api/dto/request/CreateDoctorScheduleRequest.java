package edu.unimagdalena.reservas.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateDoctorScheduleRequest(
        @NotNull DayOfWeek  dayOfWeek,
        @NotNull LocalTime  startTime,
        @NotNull LocalTime  endTime
) {}
