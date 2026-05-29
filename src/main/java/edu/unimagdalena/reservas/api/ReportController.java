package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.response.DoctorProductivityResponse;
import edu.unimagdalena.reservas.api.dto.response.NoShowPatientResponse;
import edu.unimagdalena.reservas.api.dto.response.OfficeOccupancyResponse;
import edu.unimagdalena.reservas.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @GetMapping("/office-occupancy")
    public ResponseEntity<List<OfficeOccupancyResponse>> officeOccupancy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long specialtyId) {
        return ResponseEntity.ok(service.officeOccupancy(from, to, specialtyId));
    }

    @GetMapping("/doctor-productivity")
    public ResponseEntity<List<DoctorProductivityResponse>> doctorProductivity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long specialtyId) {
        return ResponseEntity.ok(service.doctorProductivity(from, to, specialtyId));
    }

    @GetMapping("/no-show-patients")
    public ResponseEntity<List<NoShowPatientResponse>> noShowPatients(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.topNoShowPatients(from, to, limit));
    }
}
