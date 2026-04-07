package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.request.CancelAppointmentRequest;
import edu.unimagdalena.reservas.api.dto.request.CreateAppointmentRequest;
import edu.unimagdalena.reservas.api.dto.response.AppointmentResponse;
import edu.unimagdalena.reservas.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest req,
                                                      UriComponentsBuilder ucb) {
        var created = service.create(req);
        var location = ucb.path("/api/appointments/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.list(PageRequest.of(page, size, Sort.by("startAt").descending())));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirm(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id,
                                                      @Valid @RequestBody CancelAppointmentRequest req) {
        return ResponseEntity.ok(service.cancel(id, req));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable Long id,
                                                        @RequestParam(required = false) String observations) {
        return ResponseEntity.ok(service.complete(id, observations));
    }

    @PutMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> noShow(@PathVariable Long id) {
        return ResponseEntity.ok(service.markNoShow(id));
    }
}
