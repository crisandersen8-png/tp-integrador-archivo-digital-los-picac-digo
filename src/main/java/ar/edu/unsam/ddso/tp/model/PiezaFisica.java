package ar.edu.unsam.ddso.tp.model;

import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import ar.edu.unsam.ddso.tp.model.exceptions.PiezaFisicaInvalidaException;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Objeto fisico del acervo del archivo (fotografia, documento, etc.). */
@Entity
@Table(name = "piezas_fisicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PiezaFisica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String codigoInventario;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fechaIngreso;

    @Column(nullable = false, length = 50)
    private String tipoMaterial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPieza estado = EstadoPieza.EN_DEPOSITO;

    @OneToMany(mappedBy = "piezaFisica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Digitalizacion> digitalizaciones = new ArrayList<>();

    /** Valida las invariantes de negocio obligatorias de la pieza fisica sin mutar su estado. */
    public void validar() {
        if (codigoInventario == null || codigoInventario.isBlank()) {
            throw new PiezaFisicaInvalidaException("El codigo de inventario es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new PiezaFisicaInvalidaException("El nombre de la pieza fisica es obligatorio");
        }
        if (fechaIngreso == null) {
            throw new PiezaFisicaInvalidaException(
                    "La fecha de ingreso de la pieza fisica es obligatoria");
        }
        if (tipoMaterial == null || tipoMaterial.isBlank()) {
            throw new PiezaFisicaInvalidaException(
                    "El tipo de material de la pieza fisica es obligatorio");
        }
        if (estado == null) {
            throw new PiezaFisicaInvalidaException(
                    "El estado de la pieza fisica es obligatorio");
        }
    }

    /** Crea una digitalizacion de esta pieza y la vincula manteniendo ambos lados sincronizados. */
    public Digitalizacion agregarDigitalizacion(String url, String descripcionDigitalizacion) {
        Digitalizacion digitalizacion = new Digitalizacion();
        digitalizacion.setUrlRecurso(url != null ? url.trim() : null);
        digitalizacion.setDescripcion(descripcionDigitalizacion);
        digitalizacion.setFechaDigitalizacion(LocalDateTime.now());
        digitalizacion.validar();
        digitalizacion.setPiezaFisica(this);
        digitalizaciones.add(digitalizacion);
        return digitalizacion;
    }

    /** Quita una digitalizacion de esta pieza manteniendo ambos lados sincronizados. */
    public void quitarDigitalizacion(Digitalizacion digitalizacion) {
        if (digitalizaciones.remove(digitalizacion)) {
            digitalizacion.setPiezaFisica(null);
        }
    }
}
