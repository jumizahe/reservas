package edu.unimagdalena.reservas.services.mapper;

import edu.unimagdalena.reservas.api.dto.response.AppointmentResponse;
import edu.unimagdalena.reservas.domine.entities.Appointment;

public class AppointmentMapper {

    public static AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getPatient().getId(),
                a.getPatient().getFullName(),
                a.getDoctor().getId(),
                a.getDoctor().getFullName(),
                a.getOffice().getId(),
                a.getOffice().getName(),
                a.getAppointmentType().getId(),
                a.getAppointmentType().getName(),
                a.getStartAt(),
                a.getEndAt(),
                a.getStatus(),
                a.getCancellationReason(),
                a.getObservations(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
