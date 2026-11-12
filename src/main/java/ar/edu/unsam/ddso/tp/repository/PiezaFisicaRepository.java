package ar.edu.unsam.ddso.tp.repository;

import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Repositorio Spring Data JPA para la entidad PiezaFisica. */
public interface PiezaFisicaRepository
        extends JpaRepository<PiezaFisica, Long>, JpaSpecificationExecutor<PiezaFisica> {

    Optional<PiezaFisica> findByCodigoInventario(String codigoInventario);

    boolean existsByCodigoInventarioIgnoreCase(String codigoInventario);

    /**
     * Busqueda avanzada paginada cruzando PiezaFisica, Digitalizacion y Tag con filtros opcionales
     * por termino (nombre, descripcion o codigo de inventario), rango de fecha de ingreso y tag.
     */
    default Page<PiezaFisica> buscarAvanzado(
            String termino,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String nombreTag,
            Pageable pageable) {
        return findAll(
                PiezaFisicaSpecifications.conFiltros(termino, fechaDesde, fechaHasta, nombreTag),
                pageable);
    }
}
