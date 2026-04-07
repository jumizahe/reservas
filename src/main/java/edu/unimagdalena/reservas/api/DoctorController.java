package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.request.CreateDoctorRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdateDoctorRequest;
import edu.unimagdalena.reservas.api.dto.response.DoctorResponse;
import edu.unimagdalena.reservas.services.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService service;

    @PostMapping
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody CreateDoctorRequest req,
                                                 UriComponentsBuilder ucb) {
        var created = service.create(req);
        return ResponseEntity.created(ucb.path("/api/doctors/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<DoctorResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.list(PageRequest.of(page, size, Sort.by("fullName"))));
    }

    @GetMapping("/by-specialty/{specialtyId}")
    public ResponseEntity<List<DoctorResponse>> listBySpecialty(@PathVariable Long specialtyId) {
        return ResponseEntity.ok(service.listBySpecialty(specialtyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateDoctorRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }
}
