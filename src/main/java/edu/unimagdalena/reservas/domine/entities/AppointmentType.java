package edu.unimagdalena.reservas.domine.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appointment_types")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class AppointmentType {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** Duración oficial en minutos */
    @Column(nullable = false)
    private Integer durationMinutes;

    @OneToMany(mappedBy = "appointmentType")
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}
