package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.request.CancelAppointmentRequest;
import edu.unimagdalena.reservas.api.dto.request.CreateAppointmentRequest;
import edu.unimagdalena.reservas.api.dto.response.AppointmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {
    AppointmentResponse create(CreateAppointmentRequest req);
    AppointmentResponse get(Long id);
    Page<AppointmentResponse> list(Pageable pageable);
    AppointmentResponse confirm(Long id);
    AppointmentResponse cancel(Long id, CancelAppointmentRequest req);
    AppointmentResponse complete(Long id, String observations);
    AppointmentResponse markNoShow(Long id);
}
