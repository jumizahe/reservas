package edu.unimagdalena.reservas.services.mapper;

import edu.unimagdalena.reservas.api.dto.request.UpdateDoctorRequest;
import edu.unimagdalena.reservas.api.dto.response.DoctorResponse;
import edu.unimagdalena.reservas.domine.entities.Doctor;
import edu.unimagdalena.reservas.domine.entities.Specialty;

public class DoctorMapper {

    public static DoctorResponse toResponse(Doctor d) {
        return new DoctorResponse(d.getId(), d.getFullName(), d.getEmail(), d.getLicenseNumber(),
                d.getActive(), d.getSpecialty().getId(), d.getSpecialty().getName(),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    public static void patch(Doctor entity, UpdateDoctorRequest req, Specialty specialty) {
        if (req.fullName()      != null) entity.setFullName(req.fullName());
        if (req.email()         != null) entity.setEmail(req.email());
        if (req.licenseNumber() != null) entity.setLicenseNumber(req.licenseNumber());
        if (req.active()        != null) entity.setActive(req.active());
        if (specialty           != null) entity.setSpecialty(specialty);
    }
}
