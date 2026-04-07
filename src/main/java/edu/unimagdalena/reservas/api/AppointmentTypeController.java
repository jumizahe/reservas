package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.request.CreateAppointmentTypeRequest;
import edu.unimagdalena.reservas.api.dto.response.AppointmentTypeResponse;
import edu.unimagdalena.reservas.services.AppointmentTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/appointment-types")
@RequiredArgsConstructor
public class AppointmentTypeController {

    private final AppointmentTypeService service;

    @PostMapping
    public ResponseEntity<AppointmentTypeResponse> create(@Valid @RequestBody CreateAppointmentTypeRequest req,
                                                          UriComponentsBuilder ucb) {
        var created = service.create(req);
        return ResponseEntity.created(ucb.path("/api/appointment-types/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentTypeResponse>> list() {
        return ResponseEntity.ok(service.list());
    }
}
