package edu.unimagdalena.reservas.domine.repositories;

import edu.unimagdalena.reservas.domine.entities.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Long> {
    Optional<AppointmentType> findByNameIgnoreCase(String name);
}
