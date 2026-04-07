package edu.unimagdalena.reservas.services.mapper;

import edu.unimagdalena.reservas.api.dto.request.CreateSpecialtyRequest;
import edu.unimagdalena.reservas.api.dto.response.SpecialtyResponse;
import edu.unimagdalena.reservas.domine.entities.Specialty;

public class SpecialtyMapper {

    public static Specialty toEntity(CreateSpecialtyRequest req) {
        return Specialty.builder()
                .name(req.name())
                .description(req.description())
                .build();
    }

    public static SpecialtyResponse toResponse(Specialty s) {
        return new SpecialtyResponse(s.getId(), s.getName(), s.getDescription());
    }
}
