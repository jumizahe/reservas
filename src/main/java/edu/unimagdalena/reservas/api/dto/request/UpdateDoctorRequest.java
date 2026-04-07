package edu.unimagdalena.reservas.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateDoctorRequest(
        @Size(max = 150) String  fullName,
        @Email           String  email,
        @Size(max = 20)  String  licenseNumber,
        Long                     specialtyId,
        Boolean                  active
) {}
