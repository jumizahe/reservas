package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.request.CreateAppointmentTypeRequest;
import edu.unimagdalena.reservas.api.dto.response.AppointmentTypeResponse;
import java.util.List;

public interface AppointmentTypeService {
    AppointmentTypeResponse create(CreateAppointmentTypeRequest req);
    List<AppointmentTypeResponse> list();
}
