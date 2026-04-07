package edu.unimagdalena.reservas.services.mapper;

import edu.unimagdalena.reservas.api.dto.request.CreateAppointmentTypeRequest;
import edu.unimagdalena.reservas.api.dto.response.AppointmentTypeResponse;
import edu.unimagdalena.reservas.domine.entities.AppointmentType;

public class AppointmentTypeMapper {

    public static AppointmentType toEntity(CreateAppointmentTypeRequest req) {
        return AppointmentType.builder()
                .name(req.name())
                .durationMinutes(req.durationMinutes())
                .build();
    }

    public static AppointmentTypeResponse toResponse(AppointmentType at) {
        return new AppointmentTypeResponse(at.getId(), at.getName(), at.getDurationMinutes());
    }
}
