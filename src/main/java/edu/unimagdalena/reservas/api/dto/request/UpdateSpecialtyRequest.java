package edu.unimagdalena.reservas.api.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSpecialtyRequest(
        @Size(max = 100)  String name,
        @Size(max = 300)  String description
) {}
