package edu.unimagdalena.reservas.services.impl;

import edu.unimagdalena.reservas.api.dto.response.DoctorProductivityResponse;
import edu.unimagdalena.reservas.api.dto.response.NoShowPatientResponse;
import edu.unimagdalena.reservas.api.dto.response.OfficeOccupancyResponse;
import edu.unimagdalena.reservas.domine.repositories.AppointmentRepository;
import edu.unimagdalena.reservas.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final AppointmentRepository appointmentRepo;

    @Override
    public List<OfficeOccupancyResponse> officeOccupancy(LocalDateTime from, LocalDateTime to, Long specialtyId) {
        List<Object[]> rows = (specialtyId != null)
                ? appointmentRepo.findOfficeOccupancyBySpecialty(from, to, specialtyId)
                : appointmentRepo.findOfficeOccupancy(from, to);
        return rows.stream()
                .map(row -> new OfficeOccupancyResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .toList();
    }

    @Override
    public List<DoctorProductivityResponse> doctorProductivity(LocalDateTime from, LocalDateTime to, Long specialtyId) {
        List<Object[]> rows = (specialtyId != null)
                ? appointmentRepo.findDoctorProductivityBySpecialty(from, to, specialtyId)
                : appointmentRepo.findDoctorProductivity(from, to);
        return rows.stream()
                .map(row -> new DoctorProductivityResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue()))
                .toList();
    }

    @Override
    public List<NoShowPatientResponse> topNoShowPatients(LocalDateTime from, LocalDateTime to, int limit) {
        return appointmentRepo.findTopNoShowPatients(from, to, PageRequest.of(0, limit))
                .stream()
                .map(row -> new NoShowPatientResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue()))
                .toList();
    }
}
