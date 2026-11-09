package ar.edu.unsam.ddso.tp.model;

import ar.edu.unsam.ddso.tp.model.enums.PieceStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Physical object of the archive's collection (photo, document, etc.). */
@Entity
@Table(name = "physical_pieces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalPiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String inventoryCode;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, length = 50)
    private String materialType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PieceStatus status;

    @OneToMany(mappedBy = "physicalPiece", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Digitization> digitizations = new ArrayList<>();

    /** Adds a digitization to this piece, keeping both sides of the relation in sync. */
    public void addDigitization(Digitization digitization) {
        digitizations.add(digitization);
        digitization.setPhysicalPiece(this);
    }

    /** Removes a digitization from this piece, keeping both sides of the relation in sync. */
    public void removeDigitization(Digitization digitization) {
        if (digitizations.remove(digitization)) {
            digitization.setPhysicalPiece(null);
        }
    }
}
