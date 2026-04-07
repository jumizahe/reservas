package edu.unimagdalena.reservas.domine.repositories;

import edu.unimagdalena.reservas.domine.entities.Patient;
import edu.unimagdalena.reservas.domine.enums.AppointmentStatus;
import edu.unimagdalena.reservas.domine.enums.PatientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByDocumentNumber(String documentNumber);

    Page<Patient> findByStatus(PatientStatus status, Pageable pageable);

    /** Pacientes con mayor número de NO_SHOW en un período */
    @Query("""
           SELECT p FROM Patient p
           JOIN p.appointments a
           WHERE a.status = :status
             AND a.startAt BETWEEN :from AND :to
           GROUP BY p
           ORDER BY COUNT(a) DESC
           """)
    List<Patient> findTopByAppointmentStatusInPeriod(
            @Param("status") AppointmentStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
