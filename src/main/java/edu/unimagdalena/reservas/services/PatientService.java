package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.request.CreatePatientRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdatePatientRequest;
import edu.unimagdalena.reservas.api.dto.response.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {
    PatientResponse create(CreatePatientRequest req);
    PatientResponse get(Long id);
    Page<PatientResponse> list(Pageable pageable);
    PatientResponse update(Long id, UpdatePatientRequest req);
}
