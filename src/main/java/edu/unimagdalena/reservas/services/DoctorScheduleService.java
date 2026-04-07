package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.request.CreateDoctorScheduleRequest;
import edu.unimagdalena.reservas.api.dto.response.DoctorScheduleResponse;
import java.util.List;

public interface DoctorScheduleService {
    DoctorScheduleResponse create(Long doctorId, CreateDoctorScheduleRequest req);
    List<DoctorScheduleResponse> listByDoctor(Long doctorId);
}
