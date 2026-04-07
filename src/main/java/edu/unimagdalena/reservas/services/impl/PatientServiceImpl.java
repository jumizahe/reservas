package edu.unimagdalena.reservas.services.impl;

import edu.unimagdalena.reservas.api.dto.request.CreatePatientRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdatePatientRequest;
import edu.unimagdalena.reservas.api.dto.response.PatientResponse;
import edu.unimagdalena.reservas.domine.repositories.PatientRepository;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.services.PatientService;
import edu.unimagdalena.reservas.services.mapper.PatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repo;

    @Override
    public PatientResponse create(CreatePatientRequest req) {
        if (repo.findByEmail(req.email()).isPresent())
            throw new ConflictException("Email already registered: " + req.email());
        if (repo.findByDocumentNumber(req.documentNumber()).isPresent())
            throw new ConflictException("Document number already registered: " + req.documentNumber());
        return PatientMapper.toResponse(repo.save(PatientMapper.toEntity(req)));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse get(Long id) {
        return repo.findById(id)
                .map(PatientMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(PatientMapper::toResponse);
    }

    @Override
    public PatientResponse update(Long id, UpdatePatientRequest req) {
        var patient = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        PatientMapper.patch(patient, req);
        return PatientMapper.toResponse(patient);
    }
}
