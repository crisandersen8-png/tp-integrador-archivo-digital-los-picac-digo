package ar.edu.unsam.ddso.tp.repository;

import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data JPA para la entidad Digitalizacion. */
public interface DigitalizacionRepository extends JpaRepository<Digitalizacion, Long> {

    List<Digitalizacion> findByPiezaFisicaId(Long piezaFisicaId);
}
