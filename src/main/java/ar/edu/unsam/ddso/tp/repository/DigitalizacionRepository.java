package ar.edu.unsam.ddso.tp.repository;

import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio Spring Data JPA para la entidad Digitalizacion. */
@Repository
public interface DigitalizacionRepository extends JpaRepository<Digitalizacion, Long> {

    List<Digitalizacion> findByPiezaFisicaId(Long piezaFisicaId);

    List<Digitalizacion> findByTagsNombreIgnoreCase(String nombreTag);

    /**
     * Busca digitalizaciones asociadas a un tag especifico cargando su pieza fisica y sus tags.
     */
    @Query(
            "SELECT DISTINCT d FROM Digitalizacion d "
                    + "JOIN FETCH d.piezaFisica p "
                    + "LEFT JOIN FETCH d.tags t "
                    + "WHERE EXISTS ("
                    + "SELECT 1 FROM d.tags tagFiltro "
                    + "WHERE LOWER(tagFiltro.nombre) = LOWER(:nombreTag))")
    List<Digitalizacion> buscarPorTagConDetalle(@Param("nombreTag") String nombreTag);
}

