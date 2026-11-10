package ar.edu.unsam.ddso.tp.repository;

import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio Spring Data JPA para la entidad PiezaFisica. */
@Repository
public interface PiezaFisicaRepository
        extends JpaRepository<PiezaFisica, Long>, JpaSpecificationExecutor<PiezaFisica> {

    Optional<PiezaFisica> findByCodigoInventario(String codigoInventario);

    boolean existsByCodigoInventario(String codigoInventario);

    List<PiezaFisica> findByEstado(EstadoPieza estado);

    List<PiezaFisica> findByNombreContainingIgnoreCaseOrCodigoInventarioContainingIgnoreCase(
            String nombre, String codigoInventario);

    /** Busca piezas fisicas que tengan al menos una digitalizacion asociada al tag indicado. */
    @Query(
            "SELECT DISTINCT p FROM PiezaFisica p "
                    + "JOIN p.digitalizaciones d "
                    + "JOIN d.tags t "
                    + "WHERE LOWER(t.nombre) = LOWER(:nombreTag)")
    List<PiezaFisica> buscarPorNombreTag(@Param("nombreTag") String nombreTag);

    /**
     * Busqueda avanzada cruzando PiezaFisica, Digitalizacion y Tag con filtros opcionales por
     * termino (nombre, descripcion o codigo de inventario), rango de fecha de ingreso y tag.
     */
    @Query(
            "SELECT DISTINCT p FROM PiezaFisica p "
                    + "LEFT JOIN p.digitalizaciones d "
                    + "LEFT JOIN d.tags t "
                    + "WHERE (:termino IS NULL "
                    + "OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) "
                    + "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')) "
                    + "OR LOWER(p.codigoInventario) LIKE LOWER(CONCAT('%', :termino, '%'))) "
                    + "AND (:fechaDesde IS NULL OR p.fechaIngreso >= :fechaDesde) "
                    + "AND (:fechaHasta IS NULL OR p.fechaIngreso <= :fechaHasta) "
                    + "AND (:nombreTag IS NULL OR LOWER(t.nombre) = LOWER(:nombreTag))")
    List<PiezaFisica> buscarAvanzado(
            @Param("termino") String termino,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("nombreTag") String nombreTag);
}

