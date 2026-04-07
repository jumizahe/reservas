package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.request.CreateDoctorScheduleRequest;
import edu.unimagdalena.reservas.api.dto.response.DoctorScheduleResponse;
import edu.unimagdalena.reservas.services.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/doctors/{doctorId}/schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService service;

    @PostMapping
    public ResponseEntity<DoctorScheduleResponse> create(@PathVariable Long doctorId,
                                                         @Valid @RequestBody CreateDoctorScheduleRequest req,
                                                         UriComponentsBuilder ucb) {
        var created = service.create(doctorId, req);
        var location = ucb.path("/api/doctors/{doctorId}/schedules/{id}")
                .buildAndExpand(doctorId, created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<DoctorScheduleResponse>> list(@PathVariable Long doctorId) {
        return ResponseEntity.ok(service.listByDoctor(doctorId));
    }
}
