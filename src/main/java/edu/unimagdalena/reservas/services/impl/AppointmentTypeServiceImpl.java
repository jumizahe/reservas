package edu.unimagdalena.reservas.services.impl;

import edu.unimagdalena.reservas.api.dto.request.CreateAppointmentTypeRequest;
import edu.unimagdalena.reservas.api.dto.response.AppointmentTypeResponse;
import edu.unimagdalena.reservas.domine.repositories.AppointmentTypeRepository;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.services.AppointmentTypeService;
import edu.unimagdalena.reservas.services.mapper.AppointmentTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentTypeServiceImpl implements AppointmentTypeService {

    private final AppointmentTypeRepository repo;

    @Override
    public AppointmentTypeResponse create(CreateAppointmentTypeRequest req) {
        if (repo.findByNameIgnoreCase(req.name()).isPresent())
            throw new ConflictException("Appointment type already exists: " + req.name());
        return AppointmentTypeMapper.toResponse(repo.save(AppointmentTypeMapper.toEntity(req)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentTypeResponse> list() {
        return repo.findAll().stream().map(AppointmentTypeMapper::toResponse).toList();
    }
}
