package edu.unimagdalena.reservas.services.mapper;

import edu.unimagdalena.reservas.api.dto.request.CreatePatientRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdatePatientRequest;
import edu.unimagdalena.reservas.api.dto.response.PatientResponse;
import edu.unimagdalena.reservas.domine.entities.Patient;

public class PatientMapper {

    public static Patient toEntity(CreatePatientRequest req) {
        return Patient.builder()
                .fullName(req.fullName())
                .documentNumber(req.documentNumber())
                .email(req.email())
                .phone(req.phone())
                .build();
    }

    public static PatientResponse toResponse(Patient p) {
        return new PatientResponse(p.getId(), p.getFullName(), p.getDocumentNumber(),
                p.getEmail(), p.getPhone(), p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
    }

    public static void patch(Patient entity, UpdatePatientRequest req) {
        if (req.fullName() != null) entity.setFullName(req.fullName());
        if (req.email()    != null) entity.setEmail(req.email());
        if (req.phone()    != null) entity.setPhone(req.phone());
        if (req.status()   != null) entity.setStatus(req.status());
    }
}
