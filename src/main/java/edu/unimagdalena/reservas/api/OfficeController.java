package edu.unimagdalena.reservas.api;

import edu.unimagdalena.reservas.api.dto.request.CreateOfficeRequest;
import edu.unimagdalena.reservas.api.dto.request.UpdateOfficeRequest;
import edu.unimagdalena.reservas.api.dto.response.OfficeResponse;
import edu.unimagdalena.reservas.services.OfficeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/offices")
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeService service;

    @PostMapping
    public ResponseEntity<OfficeResponse> create(@Valid @RequestBody CreateOfficeRequest req,
                                                 UriComponentsBuilder ucb) {
        var created = service.create(req);
        return ResponseEntity.created(ucb.path("/api/offices/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<OfficeResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfficeResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateOfficeRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }
}
