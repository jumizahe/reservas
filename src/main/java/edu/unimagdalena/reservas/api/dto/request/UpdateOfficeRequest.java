package edu.unimagdalena.reservas.api.dto.request;

import edu.unimagdalena.reservas.domine.enums.OfficeStatus;
import jakarta.validation.constraints.Size;

public record UpdateOfficeRequest(
        @Size(max = 50)  String       name,
        @Size(max = 200) String       location,
        OfficeStatus                  status
) {}
