package edu.unimagdalena.reservas.api.dto.request;

import edu.unimagdalena.reservas.domine.enums.PatientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdatePatientRequest(
        @Size(max = 150) String fullName,
        @Email           String email,
        @Size(max = 20)  String phone,
        PatientStatus    status
) {}
