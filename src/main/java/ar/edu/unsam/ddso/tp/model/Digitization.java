package ar.edu.unsam.ddso.tp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Digital resource (scan, photo, etc.) generated from a physical piece. */
@Entity
@Table(name = "digitizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Digitization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String resourceUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime digitizationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "physical_piece_id", nullable = false)
    private PhysicalPiece physicalPiece;

    @ManyToMany
    @JoinTable(
            name = "digitization_tags",
            joinColumns = @JoinColumn(name = "digitization_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    /** Associates a tag with this digitization, keeping both sides of the relation in sync. */
    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getDigitizations().add(this);
    }

    /** Dissociates a tag from this digitization, keeping both sides of the relation in sync. */
    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getDigitizations().remove(this);
    }
}
