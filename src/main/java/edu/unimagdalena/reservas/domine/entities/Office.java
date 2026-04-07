package edu.unimagdalena.reservas.domine.entities;

import edu.unimagdalena.reservas.domine.enums.OfficeStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offices")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class Office {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OfficeStatus status = OfficeStatus.AVAILABLE;

    @OneToMany(mappedBy = "office")
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}
