package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.request.CreateOfficeRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdateOfficeRequest;
import edu.unimagdalena.reservas.api.dto.response.OfficeResponse;
import java.util.List;

public interface OfficeService {
    OfficeResponse create(CreateOfficeRequest req);
    List<OfficeResponse> list();
    OfficeResponse update(Long id, UpdateOfficeRequest req);
}
