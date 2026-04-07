package edu.unimagdalena.reservas.services.impl;

import edu.unimagdalena.reservas.api.dto.request.CreateDoctorRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdateDoctorRequest;
import edu.unimagdalena.reservas.api.dto.response.DoctorResponse;
import edu.unimagdalena.reservas.domine.entities.Doctor;
import edu.unimagdalena.reservas.domine.repositories.DoctorRepository;
import edu.unimagdalena.reservas.domine.repositories.SpecialtyRepository;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.services.DoctorService;
import edu.unimagdalena.reservas.services.mapper.DoctorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository repo;
    private final SpecialtyRepository specialtyRepo;

    @Override
    public DoctorResponse create(CreateDoctorRequest req) {
        if (repo.findByEmail(req.email()).isPresent())
            throw new ConflictException("Email already registered: " + req.email());
        var specialty = specialtyRepo.findById(req.specialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty", req.specialtyId()));
        var doctor = Doctor.builder()
                .fullName(req.fullName())
                .email(req.email())
                .licenseNumber(req.licenseNumber())
                .specialty(specialty)
                .build();
        return DoctorMapper.toResponse(repo.save(doctor));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse get(Long id) {
        return repo.findById(id)
                .map(DoctorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(DoctorMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> listBySpecialty(Long specialtyId) {
        return repo.findBySpecialtyIdAndActiveTrue(specialtyId)
                .stream().map(DoctorMapper::toResponse).toList();
    }

    @Override
    public DoctorResponse update(Long id, UpdateDoctorRequest req) {
        var doctor = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        var specialty = req.specialtyId() != null
                ? specialtyRepo.findById(req.specialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialty", req.specialtyId()))
                : null;
        DoctorMapper.patch(doctor, req, specialty);
        return DoctorMapper.toResponse(doctor);
    }
}
