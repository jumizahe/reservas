package edu.unimagdalena.reservas.domine.repositories;

import edu.unimagdalena.reservas.domine.entities.Office;
import edu.unimagdalena.reservas.domine.enums.OfficeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OfficeRepository extends JpaRepository<Office, Long> {

    List<Office> findByStatus(OfficeStatus status);

    /** Calcular ocupación diaria o por rango de consultorios */
    @Query("""
           SELECT o.id, o.name, COUNT(a) as totalAppointments
           FROM Office o
           LEFT JOIN o.appointments a
           WHERE a.startAt BETWEEN :from AND :to
             AND a.status NOT IN ('CANCELLED')
           GROUP BY o.id, o.name
           ORDER BY COUNT(a) DESC
           """)
    List<Object[]> findOccupancyByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
