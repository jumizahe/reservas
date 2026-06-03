package edu.unimagdalena.reservas.services.impl;

import edu.unimagdalena.reservas.api.dto.request.CreateSpecialtyRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdateSpecialtyRequest;
import edu.unimagdalena.reservas.api.dto.response.SpecialtyResponse;
import edu.unimagdalena.reservas.domine.entities.Specialty;
import edu.unimagdalena.reservas.domine.repositories.SpecialtyRepository;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
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

    @Override
    public SpecialtyResponse update(Long id, UpdateSpecialtyRequest req) {
        Specialty specialty = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", id));
        if (req.name() != null) {
            repo.findByNameIgnoreCase(req.name())
                    .filter(s -> !s.getId().equals(id))
                    .ifPresent(s -> { throw new ConflictException("Specialty already exists: " + req.name()); });
            specialty.setName(req.name());
        }
        if (req.description() != null) specialty.setDescription(req.description());
        return SpecialtyMapper.toResponse(specialty);
    }
}
