package edu.unimagdalena.reservas.services.impl;

import edu.unimagdalena.reservas.api.dto.request.CreateOfficeRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdateOfficeRequest;
import edu.unimagdalena.reservas.api.dto.response.OfficeResponse;
import edu.unimagdalena.reservas.domine.repositories.OfficeRepository;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.services.OfficeService;
import edu.unimagdalena.reservas.services.mapper.OfficeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OfficeServiceImpl implements OfficeService {

    private final OfficeRepository repo;

    @Override
    public OfficeResponse create(CreateOfficeRequest req) {
        return OfficeMapper.toResponse(repo.save(OfficeMapper.toEntity(req)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeResponse> list() {
        return repo.findAll().stream().map(OfficeMapper::toResponse).toList();
    }

    @Override
    public OfficeResponse update(Long id, UpdateOfficeRequest req) {
        var office = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", id));
        OfficeMapper.patch(office, req);
        return OfficeMapper.toResponse(office);
    }
}
