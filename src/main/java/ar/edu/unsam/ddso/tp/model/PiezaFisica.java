package ar.edu.unsam.ddso.tp.model;

import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
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
    private EstadoPieza estado;

    @OneToMany(mappedBy = "piezaFisica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Digitalizacion> digitalizaciones = new ArrayList<>();

    /** Crea una digitalizacion de esta pieza y la vincula manteniendo ambos lados sincronizados. */
    public void agregarDigitalizacion(String url, String descripcionDigitalizacion) {
        Digitalizacion digitalizacion = new Digitalizacion();
        digitalizacion.setUrlRecurso(url);
        digitalizacion.setDescripcion(descripcionDigitalizacion);
        digitalizacion.setFechaDigitalizacion(LocalDateTime.now());
        digitalizacion.setPiezaFisica(this);
        digitalizaciones.add(digitalizacion);
    }

    /** Quita una digitalizacion de esta pieza manteniendo ambos lados sincronizados. */
    public void quitarDigitalizacion(Digitalizacion digitalizacion) {
        if (digitalizaciones.remove(digitalizacion)) {
            digitalizacion.setPiezaFisica(null);
        }
    }
}
