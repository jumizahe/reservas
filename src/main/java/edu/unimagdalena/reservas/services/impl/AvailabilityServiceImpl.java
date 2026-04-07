package edu.unimagdalena.reservas.services.impl;

import edu.unimagdalena.reservas.api.dto.response.AvailabilitySlotResponse;
import edu.unimagdalena.reservas.domine.entities.Appointment;
import edu.unimagdalena.reservas.domine.repositories.AppointmentRepository;
import edu.unimagdalena.reservas.domine.repositories.DoctorRepository;
import edu.unimagdalena.reservas.domine.repositories.DoctorScheduleRepository;
import edu.unimagdalena.reservas.exception.BusinessException;
import edu.unimagdalena.reservas.exception.ResourceNotFoundException;
import edu.unimagdalena.reservas.services.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailabilityServiceImpl implements AvailabilityService {

    private final DoctorRepository         doctorRepo;
    private final DoctorScheduleRepository scheduleRepo;
    private final AppointmentRepository    appointmentRepo;

    @Override
    public List<AvailabilitySlotResponse> getAvailableSlots(Long doctorId, LocalDate date, int durationMinutes) {
        if (durationMinutes < 5)
            throw new BusinessException("Minimum slot duration is 5 minutes");

        var doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        if (!doctor.getActive())
            throw new BusinessException("Doctor is not active");

        var schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek())
                .orElseThrow(() -> new BusinessException(
                        "Doctor has no schedule for " + date.getDayOfWeek()));

        // Obtener citas existentes del día (no canceladas)
        var existing = appointmentRepo.findByDoctorIdAndDate(doctorId, date.atStartOfDay());

        // Generar todos los slots posibles y filtrar los que no solapan
        List<AvailabilitySlotResponse> slots = new ArrayList<>();
        LocalTime cursor = schedule.getStartTime();
        LocalTime workEnd = schedule.getEndTime();

        while (!cursor.plusMinutes(durationMinutes).isAfter(workEnd)) {
            LocalDateTime slotStart = date.atTime(cursor);
            LocalDateTime slotEnd   = slotStart.plusMinutes(durationMinutes);

            boolean overlaps = existing.stream().anyMatch(a -> overlaps(a, slotStart, slotEnd));
            if (!overlaps) {
                slots.add(new AvailabilitySlotResponse(slotStart, slotEnd, doctorId, doctor.getFullName()));
            }
            cursor = cursor.plusMinutes(durationMinutes);
        }
        return slots;
    }

    private boolean overlaps(Appointment a, LocalDateTime start, LocalDateTime end) {
        return a.getStartAt().isBefore(end) && a.getEndAt().isAfter(start);
    }
}
