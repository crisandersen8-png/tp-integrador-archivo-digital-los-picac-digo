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

/** Recurso digital (escaneo, foto, etc.) generado a partir de una pieza fisica. */
@Entity
@Table(name = "digitalizaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Digitalizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String urlRecurso;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fechaDigitalizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pieza_fisica_id", nullable = false)
    private PiezaFisica piezaFisica;

    @ManyToMany
    @JoinTable(
            name = "digitalizacion_tag",
            joinColumns = @JoinColumn(name = "digitalizacion_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    /** Asocia un tag a esta digitalizacion manteniendo ambos lados sincronizados. */
    public void asociarTag(Tag tag) {
        tags.add(tag);
        tag.getDigitalizaciones().add(this);
    }

    /** Desasocia un tag de esta digitalizacion manteniendo ambos lados sincronizados. */
    public void desasociarTag(Tag tag) {
        tags.remove(tag);
        tag.getDigitalizaciones().remove(this);
    }
}
