package edu.unimagdalena.reservas.services.mapper;

import edu.unimagdalena.reservas.api.dto.response.DoctorScheduleResponse;
import edu.unimagdalena.reservas.domine.entities.DoctorSchedule;

public class DoctorScheduleMapper {

    public static DoctorScheduleResponse toResponse(DoctorSchedule ds) {
        return new DoctorScheduleResponse(
                ds.getId(),
                ds.getDoctor().getId(),
                ds.getDoctor().getFullName(),
                ds.getDayOfWeek(),
                ds.getStartTime(),
                ds.getEndTime()
        );
    }
}
