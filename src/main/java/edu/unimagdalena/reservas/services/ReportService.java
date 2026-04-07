package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.response.DoctorProductivityResponse;
import edu.unimagdalena.reservas.api.dto.response.NoShowPatientResponse;
import edu.unimagdalena.reservas.api.dto.response.OfficeOccupancyResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {
    List<OfficeOccupancyResponse>    officeOccupancy(LocalDateTime from, LocalDateTime to);
    List<DoctorProductivityResponse> doctorProductivity(LocalDateTime from, LocalDateTime to);
    List<NoShowPatientResponse>      topNoShowPatients(LocalDateTime from, LocalDateTime to, int limit);
}
