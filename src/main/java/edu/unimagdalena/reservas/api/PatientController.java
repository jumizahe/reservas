package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.request.CreatePatientRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdatePatientRequest;
import edu.unimagdalena.reservas.api.dto.response.PatientResponse;
import edu.unimagdalena.reservas.services.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService service;

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest req,
                                                  UriComponentsBuilder ucb) {
        var created = service.create(req);
        var location = ucb.path("/api/patients/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.list(PageRequest.of(page, size, Sort.by("fullName"))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdatePatientRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }
}
