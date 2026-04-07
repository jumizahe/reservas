package edu.unimagdalena.reservas.services.mapper;

import edu.unimagdalena.reservas.api.dto.request.CreateOfficeRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdateOfficeRequest;
import edu.unimagdalena.reservas.api.dto.response.OfficeResponse;
import edu.unimagdalena.reservas.domine.entities.Office;

public class OfficeMapper {

    public static Office toEntity(CreateOfficeRequest req) {
        return Office.builder()
                .name(req.name())
                .location(req.location())
                .build();
    }

    public static OfficeResponse toResponse(Office o) {
        return new OfficeResponse(o.getId(), o.getName(), o.getLocation(), o.getStatus());
    }

    public static void patch(Office entity, UpdateOfficeRequest req) {
        if (req.name()     != null) entity.setName(req.name());
        if (req.location() != null) entity.setLocation(req.location());
        if (req.status()   != null) entity.setStatus(req.status());
    }
}
