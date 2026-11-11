package ar.edu.unsam.ddso.tp.model;

import ar.edu.unsam.ddso.tp.model.exceptions.DigitalizacionInvalidaException;
import ar.edu.unsam.ddso.tp.model.exceptions.TagInvalidoException;
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
import java.net.URI;
import java.net.URISyntaxException;
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

    /** Valida que el recurso digitalizado tenga una URL HTTP o HTTPS con formato valido. */
    public void validar() {
        if (urlRecurso == null || urlRecurso.isBlank()) {
            throw new DigitalizacionInvalidaException(
                    "La URL del recurso digitalizado es obligatoria");
        }
        try {
            URI uri = new URI(urlRecurso.trim());
            String esquema = uri.getScheme();
            String host = uri.getHost();
            if (esquema == null
                    || (!"http".equalsIgnoreCase(esquema) && !"https".equalsIgnoreCase(esquema))
                    || host == null
                    || host.isBlank()) {
                throw new DigitalizacionInvalidaException(
                        "La URL del recurso digitalizado debe ser una direccion HTTP o HTTPS"
                                + " valida");
            }
        } catch (URISyntaxException ex) {
            throw new DigitalizacionInvalidaException(
                    "La URL del recurso digitalizado tiene un formato invalido", ex);
        }
    }

    /** Asocia un tag a esta digitalizacion manteniendo ambos lados sincronizados. */
    public void asociarTag(Tag tag) {
        if (tag == null) {
            throw new TagInvalidoException("El tag a asociar no puede ser nulo");
        }
        tags.add(tag);
        tag.getDigitalizaciones().add(this);
    }

    /** Desasocia un tag de esta digitalizacion manteniendo ambos lados sincronizados. */
    public void desasociarTag(Tag tag) {
        tags.remove(tag);
        tag.getDigitalizaciones().remove(this);
    }
}
