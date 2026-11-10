package ar.edu.unsam.ddso.tp.repository;

import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Especificaciones JPA dinamicas para filtrar instancias de PiezaFisica. */
public final class PiezaFisicaSpecifications {

    private PiezaFisicaSpecifications() {}

    /**
     * Construye una especificacion combinando filtros opcionales por termino libre (nombre,
     * descripcion o codigo de inventario), rango de fecha de ingreso y nombre de tag asociado.
     */
    public static Specification<PiezaFisica> conFiltros(
            String termino, LocalDate fechaDesde, LocalDate fechaHasta, String nombreTag) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> predicados = new ArrayList<>();

            if (termino != null && !termino.isBlank()) {
                String patron = "%" + termino.trim().toLowerCase(Locale.ROOT) + "%";
                predicados.add(
                        cb.or(
                                cb.like(cb.lower(root.get("nombre")), patron),
                                cb.like(cb.lower(root.get("descripcion")), patron),
                                cb.like(cb.lower(root.get("codigoInventario")), patron)));
            }

            if (fechaDesde != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("fechaIngreso"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("fechaIngreso"), fechaHasta));
            }

            if (nombreTag != null && !nombreTag.isBlank()) {
                Join<PiezaFisica, Digitalizacion> digitalizacionesJoin =
                        root.join("digitalizaciones", JoinType.INNER);
                Join<Digitalizacion, Tag> tagsJoin =
                        digitalizacionesJoin.join("tags", JoinType.INNER);
                predicados.add(
                        cb.equal(
                                cb.lower(tagsJoin.get("nombre")),
                                nombreTag.trim().toLowerCase(Locale.ROOT)));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
