package edu.unimagdalena.reservas.services.impl;

import edu.unimagdalena.reservas.api.dto.request.CreateDoctorScheduleRequest;
import edu.unimagdalena.reservas.api.dto.response.DoctorScheduleResponse;
import edu.unimagdalena.reservas.domine.entities.DoctorSchedule;
import edu.unimagdalena.reservas.domine.repositories.DoctorRepository;
import edu.unimagdalena.reservas.domine.repositories.DoctorScheduleRepository;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.exception.ValidationException;
import edu.unimagdalena.reservas.services.DoctorScheduleService;
import edu.unimagdalena.reservas.services.mapper.DoctorScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepo;
    private final DoctorRepository         doctorRepo;

    @Override
    public DoctorScheduleResponse create(Long doctorId, CreateDoctorScheduleRequest req) {
        if (req.endTime().isBefore(req.startTime()) || req.endTime().equals(req.startTime()))
            throw new ValidationException("endTime must be after startTime");

        var doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        if (scheduleRepo.existsByDoctorIdAndDayOfWeek(doctorId, req.dayOfWeek()))
            throw new ConflictException("Doctor already has a schedule for " + req.dayOfWeek());

        var schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(req.dayOfWeek())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .build();
        return DoctorScheduleMapper.toResponse(scheduleRepo.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> listByDoctor(Long doctorId) {
        if (!doctorRepo.existsById(doctorId))
            throw new ResourceNotFoundException("Doctor", doctorId);
        return scheduleRepo.findByDoctorId(doctorId)
                .stream().map(DoctorScheduleMapper::toResponse).toList();
    }
}
