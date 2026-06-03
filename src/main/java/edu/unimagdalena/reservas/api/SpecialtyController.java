package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.request.CreateSpecialtyRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdateSpecialtyRequest;
import edu.unimagdalena.reservas.api.dto.response.SpecialtyResponse;
import edu.unimagdalena.reservas.services.SpecialtyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService service;

    @PostMapping
    public ResponseEntity<SpecialtyResponse> create(@Valid @RequestBody CreateSpecialtyRequest req,
                                                    UriComponentsBuilder ucb) {
        var created = service.create(req);
        return ResponseEntity.created(ucb.path("/api/specialties/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<SpecialtyResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpecialtyResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateSpecialtyRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }
}
