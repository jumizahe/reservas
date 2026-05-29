package edu.unimagdalena.reservas.domine.repositories;

import edu.unimagdalena.reservas.domine.entities.Appointment;
import edu.unimagdalena.reservas.domine.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Buscar citas de un paciente por estado */
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    /** Buscar citas por rango de fecha */
    List<Appointment> findByStartAtBetween(LocalDateTime from, LocalDateTime to);

    /** Validar traslape de citas para un DOCTOR
     *  Una cita traslapa si su startAt < endAt_nueva Y su endAt > startAt_nueva */
    @Query("""
           SELECT COUNT(a) > 0 FROM Appointment a
           WHERE a.doctor.id = :doctorId
             AND a.status NOT IN ('CANCELLED')
             AND a.startAt < :endAt
             AND a.endAt   > :startAt
           """)
    boolean existsDoctorOverlap(
            @Param("doctorId") Long doctorId,
            @Param("startAt")  LocalDateTime startAt,
            @Param("endAt")    LocalDateTime endAt);

    /** Validar traslape excluyendo una cita existente (útil para updates) */
    @Query("""
           SELECT COUNT(a) > 0 FROM Appointment a
           WHERE a.doctor.id = :doctorId
             AND a.id <> :excludeId
             AND a.status NOT IN ('CANCELLED')
             AND a.startAt < :endAt
             AND a.endAt   > :startAt
           """)
    boolean existsDoctorOverlapExcluding(
            @Param("doctorId")  Long doctorId,
            @Param("startAt")   LocalDateTime startAt,
            @Param("endAt")     LocalDateTime endAt,
            @Param("excludeId") Long excludeId);

    /** Validar traslape de citas para un CONSULTORIO */
    @Query("""
           SELECT COUNT(a) > 0 FROM Appointment a
           WHERE a.office.id = :officeId
             AND a.status NOT IN ('CANCELLED')
             AND a.startAt < :endAt
             AND a.endAt   > :startAt
           """)
    boolean existsOfficeOverlap(
            @Param("officeId") Long officeId,
            @Param("startAt")  LocalDateTime startAt,
            @Param("endAt")    LocalDateTime endAt);

    /** Validar traslape de citas para un PACIENTE */
    @Query("""
           SELECT COUNT(a) > 0 FROM Appointment a
           WHERE a.patient.id = :patientId
             AND a.status NOT IN ('CANCELLED')
             AND a.startAt < :endAt
             AND a.endAt   > :startAt
           """)
    boolean existsPatientOverlap(
            @Param("patientId") Long patientId,
            @Param("startAt")   LocalDateTime startAt,
            @Param("endAt")     LocalDateTime endAt);

    /** Citas confirmadas de un doctor en una fecha concreta (para calcular slots) */
    @Query("""
           SELECT a FROM Appointment a
           WHERE a.doctor.id = :doctorId
             AND a.status NOT IN ('CANCELLED')
             AND CAST(a.startAt AS date) = CAST(:date AS date)
           ORDER BY a.startAt
           """)
    List<Appointment> findByDoctorIdAndDate(
            @Param("doctorId") Long doctorId,
            @Param("date")     LocalDateTime date);

    /** Contar citas canceladas y NO_SHOW por especialidad */
    @Query("""
           SELECT d.specialty.name, a.status, COUNT(a)
           FROM Appointment a
           JOIN a.doctor d
           WHERE a.status IN ('CANCELLED', 'NO_SHOW')
             AND a.startAt BETWEEN :from AND :to
           GROUP BY d.specialty.name, a.status
           """)
    List<Object[]> countCancelledAndNoShowBySpecialty(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    /** Ranking de doctores por citas completadas */
    @Query("""
           SELECT d.id, d.fullName, d.specialty.name, COUNT(a) as total
           FROM Appointment a
           JOIN a.doctor d
           WHERE a.status = 'COMPLETED'
             AND a.startAt BETWEEN :from AND :to
           GROUP BY d.id, d.fullName, d.specialty.name
           ORDER BY COUNT(a) DESC
           """)
    List<Object[]> findDoctorProductivity(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    /** Ranking de doctores por citas completadas, filtrado por especialidad */
    @Query("""
           SELECT d.id, d.fullName, d.specialty.name, COUNT(a) as total
           FROM Appointment a
           JOIN a.doctor d
           WHERE a.status = 'COMPLETED'
             AND a.startAt BETWEEN :from AND :to
             AND d.specialty.id = :specialtyId
           GROUP BY d.id, d.fullName, d.specialty.name
           ORDER BY COUNT(a) DESC
           """)
    List<Object[]> findDoctorProductivityBySpecialty(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            @Param("specialtyId") Long specialtyId);

    /** Ocupación de consultorios por rango de fechas */
    @Query("""
           SELECT o.id, o.name, COUNT(a) as total
           FROM Appointment a
           JOIN a.office o
           WHERE a.status NOT IN ('CANCELLED')
             AND a.startAt BETWEEN :from AND :to
           GROUP BY o.id, o.name
           ORDER BY COUNT(a) DESC
           """)
    List<Object[]> findOfficeOccupancy(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    /** Ocupación de consultorios, filtrado por especialidad (vía doctor) */
    @Query("""
           SELECT o.id, o.name, COUNT(a) as total
           FROM Appointment a
           JOIN a.office o
           JOIN a.doctor d
           WHERE a.status NOT IN ('CANCELLED')
             AND a.startAt BETWEEN :from AND :to
             AND d.specialty.id = :specialtyId
           GROUP BY o.id, o.name
           ORDER BY COUNT(a) DESC
           """)
    List<Object[]> findOfficeOccupancyBySpecialty(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            @Param("specialtyId") Long specialtyId);

    /** Pacientes con más NO_SHOW en un período */
    @Query("""
           SELECT p.id, p.fullName, p.documentNumber, COUNT(a) as noShows
           FROM Appointment a
           JOIN a.patient p
           WHERE a.status = 'NO_SHOW'
             AND a.startAt BETWEEN :from AND :to
           GROUP BY p.id, p.fullName, p.documentNumber
           ORDER BY COUNT(a) DESC
           """)
    List<Object[]> findTopNoShowPatients(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to,
            Pageable pageable);
}
