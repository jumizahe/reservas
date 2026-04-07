package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.request.CreateDoctorRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdateDoctorRequest;
import edu.unimagdalena.reservas.api.dto.response.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DoctorService {
    DoctorResponse create(CreateDoctorRequest req);
    DoctorResponse get(Long id);
    Page<DoctorResponse> list(Pageable pageable);
    List<DoctorResponse> listBySpecialty(Long specialtyId);
    DoctorResponse update(Long id, UpdateDoctorRequest req);
}
