package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.request.CreateSpecialtyRequest;
import edu.unimagdalena.reservas.api.dto.response.SpecialtyResponse;
import java.util.List;

public interface SpecialtyService {
    SpecialtyResponse create(CreateSpecialtyRequest req);
    List<SpecialtyResponse> list();
}
