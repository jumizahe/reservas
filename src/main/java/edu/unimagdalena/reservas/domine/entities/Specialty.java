package edu.unimagdalena.reservas.domine.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "specialties")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class Specialty {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    @OneToMany(mappedBy = "specialty")
    @Builder.Default
    private List<Doctor> doctors = new ArrayList<>();
}
