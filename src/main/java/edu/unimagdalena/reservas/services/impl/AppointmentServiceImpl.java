package edu.unimagdalena.reservas.services.impl;

import edu.unimagdalena.reservas.api.dto.request.CancelAppointmentRequest;
import edu.unimagdalena.reservas.api.dto.request.CreateAppointmentRequest;
import edu.unimagdalena.reservas.api.dto.response.AppointmentResponse;
import edu.unimagdalena.reservas.domine.entities.Appointment;
import edu.unimagdalena.reservas.domine.enums.AppointmentStatus;
import edu.unimagdalena.reservas.domine.enums.OfficeStatus;
import edu.unimagdalena.reservas.domine.enums.PatientStatus;
import edu.unimagdalena.reservas.domine.repositories.*;
import edu.unimagdalena.reservas.exception.BusinessException;
import edu.unimagdalena.reservas.exception.ConflictException;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.services.AppointmentService;
import edu.unimagdalena.reservas.services.mapper.AppointmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository     appointmentRepo;
    private final PatientRepository         patientRepo;
    private final DoctorRepository          doctorRepo;
    private final OfficeRepository          officeRepo;
    private final AppointmentTypeRepository appointmentTypeRepo;
    private final DoctorScheduleRepository  scheduleRepo;

    @Override
    public AppointmentResponse create(CreateAppointmentRequest req) {
        // R1 — Paciente existe y está activo
        var patient = patientRepo.findById(req.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", req.patientId()));
        if (patient.getStatus() != PatientStatus.ACTIVE)
            throw new BusinessException("Patient is not active");

        // R2 — Doctor existe y está activo
        var doctor = doctorRepo.findById(req.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", req.doctorId()));
        if (!doctor.getActive())
            throw new BusinessException("Doctor is not active");

        // R3 — Consultorio existe y está disponible
        var office = officeRepo.findById(req.officeId())
                .orElseThrow(() -> new ResourceNotFoundException("Office", req.officeId()));
        if (office.getStatus() != OfficeStatus.AVAILABLE)
            throw new BusinessException("Office is not available");

        // R4 — No se puede crear cita en fecha/hora pasada (también cubierto por @Future en DTO)
        if (!req.startAt().isAfter(LocalDateTime.now()))
            throw new BusinessException("Appointment must be scheduled in the future");

        // Calcular endAt con duración del tipo de cita
        var appointmentType = appointmentTypeRepo.findById(req.appointmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("AppointmentType", req.appointmentTypeId()));
        var endAt = req.startAt().plusMinutes(appointmentType.getDurationMinutes());

        // R5 — La cita debe quedar dentro del horario laboral del doctor
        var dayOfWeek = req.startAt().getDayOfWeek();
        var schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(doctor.getId(), dayOfWeek)
                .orElseThrow(() -> new BusinessException(
                        "Doctor has no schedule for " + dayOfWeek));
        if (req.startAt().toLocalTime().isBefore(schedule.getStartTime()) ||
            endAt.toLocalTime().isAfter(schedule.getEndTime()))
            throw new BusinessException("Appointment is outside the doctor's working hours");

        // R6 — No traslape de doctor
        if (appointmentRepo.existsDoctorOverlap(doctor.getId(), req.startAt(), endAt))
            throw new ConflictException("Doctor already has an appointment in that time slot");

        // R7 — No traslape de consultorio
        if (appointmentRepo.existsOfficeOverlap(office.getId(), req.startAt(), endAt))
            throw new ConflictException("Office is already occupied in that time slot");

        // R8 — No traslape del paciente
        if (appointmentRepo.existsPatientOverlap(patient.getId(), req.startAt(), endAt))
            throw new ConflictException("Patient already has an appointment in that time slot");

        var appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .office(office)
                .appointmentType(appointmentType)
                .startAt(req.startAt())
                .endAt(endAt)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        return AppointmentMapper.toResponse(appointmentRepo.save(appointment));
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse get(Long id) {
        return appointmentRepo.findById(id)
                .map(AppointmentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> list(Pageable pageable) {
        return appointmentRepo.findAll(pageable).map(AppointmentMapper::toResponse);
    }

    @Override
    public AppointmentResponse confirm(Long id) {
        var appointment = findOrThrow(id);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED)
            throw new BusinessException("Only SCHEDULED appointments can be confirmed. Current status: " + appointment.getStatus());
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return AppointmentMapper.toResponse(appointment);
    }

    @Override
    public AppointmentResponse cancel(Long id, CancelAppointmentRequest req) {
        var appointment = findOrThrow(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED)
            throw new BusinessException("Cannot cancel a COMPLETED appointment");
        if (appointment.getStatus() == AppointmentStatus.NO_SHOW)
            throw new BusinessException("Cannot cancel a NO_SHOW appointment");
        if (appointment.getStatus() == AppointmentStatus.CANCELLED)
            throw new BusinessException("Appointment is already CANCELLED");
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(req.reason());
        return AppointmentMapper.toResponse(appointment);
    }

    @Override
    public AppointmentResponse complete(Long id, String observations) {
        var appointment = findOrThrow(id);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED)
            throw new BusinessException("Only CONFIRMED appointments can be completed. Current status: " + appointment.getStatus());
        if (LocalDateTime.now().isBefore(appointment.getStartAt()))
            throw new BusinessException("Cannot complete an appointment before its scheduled start time");
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setObservations(observations);
        return AppointmentMapper.toResponse(appointment);
    }

    @Override
    public AppointmentResponse markNoShow(Long id) {
        var appointment = findOrThrow(id);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED)
            throw new BusinessException("Only CONFIRMED appointments can be marked as NO_SHOW. Current status: " + appointment.getStatus());
        if (LocalDateTime.now().isBefore(appointment.getStartAt()))
            throw new BusinessException("Cannot mark NO_SHOW before the appointment start time");
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        return AppointmentMapper.toResponse(appointment);
    }

    private Appointment findOrThrow(Long id) {
        return appointmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }
}
