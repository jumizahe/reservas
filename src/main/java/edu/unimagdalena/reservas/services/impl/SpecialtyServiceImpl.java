package edu.unimagdalena.reservas.services.impl;

import edu.unimagdalena.reservas.api.dto.request.CreateSpecialtyRequest;
import edu.unimagdalena.reservas.api.dto.response.SpecialtyResponse;
import edu.unimagdalena.reservas.domine.repositories.SpecialtyRepository;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.services.SpecialtyService;
import edu.unimagdalena.reservas.services.mapper.SpecialtyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository repo;

    @Override
    public SpecialtyResponse create(CreateSpecialtyRequest req) {
        if (repo.findByNameIgnoreCase(req.name()).isPresent())
            throw new ConflictException("Specialty already exists: " + req.name());
        return SpecialtyMapper.toResponse(repo.save(SpecialtyMapper.toEntity(req)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyResponse> list() {
        return repo.findAll().stream().map(SpecialtyMapper::toResponse).toList();
    }
}
