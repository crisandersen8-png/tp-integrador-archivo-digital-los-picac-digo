package ar.edu.unsam.ddso.tp.repository;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class DigitalizacionRepositoryTest {

    @Autowired private DigitalizacionRepository digitalizacionRepository;

    @Autowired private PiezaFisicaRepository piezaFisicaRepository;

    @Test
    void findByPiezaFisicaIdDevuelveLasDigitalizacionesDeLaPieza() {
        // Dado
        PiezaFisica pieza = new PiezaFisica();
        pieza.setCodigoInventario("INV-2026-0050");
        pieza.setNombre("Fotografia Original");
        pieza.setDescripcion("Retrato grupal");
        pieza.setFechaIngreso(LocalDate.of(2026, 6, 1));
        pieza.setTipoMaterial("Fotografia");
        pieza.setEstado(EstadoPieza.EN_DEPOSITO);
        pieza.agregarDigitalizacion("https://archivo/dorso.jpg", "Toma de dorso");
        PiezaFisica guardada = piezaFisicaRepository.save(pieza);

        // Cuando
        List<Digitalizacion> digitalizaciones =
                digitalizacionRepository.findByPiezaFisicaId(guardada.getId());

        // Entonces
        assertEquals(1, digitalizaciones.size());
        assertEquals("https://archivo/dorso.jpg", digitalizaciones.get(0).getUrlRecurso());
    }
}
