package edu.unimagdalena.reservas.domine.repositories;

import edu.unimagdalena.reservas.domine.entities.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByEmail(String email);

    /** Buscar doctores activos por especialidad */
    List<Doctor> findBySpecialtyIdAndActiveTrue(Long specialtyId);

    Page<Doctor> findByActiveTrue(Pageable pageable);

    /** Ranking de profesionales por citas completadas */
    @Query("""
           SELECT d FROM Doctor d
           JOIN d.appointments a
           WHERE a.status = 'COMPLETED'
           GROUP BY d
           ORDER BY COUNT(a) DESC
           """)
    List<Doctor> findRankedByCompletedAppointments(Pageable pageable);
}
