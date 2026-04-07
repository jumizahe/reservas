package edu.unimagdalena.reservas.services;

import edu.unimagdalena.reservas.api.dto.response.AvailabilitySlotResponse;
import java.time.LocalDate;
import java.util.List;

public interface AvailabilityService {
    List<AvailabilitySlotResponse> getAvailableSlots(Long doctorId, LocalDate date, int durationMinutes);
}
