package ar.edu.unsam.ddso.tp.repository;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.Tag;
import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class PiezaFisicaRepositoryTest {

    @Autowired private PiezaFisicaRepository piezaFisicaRepository;

    @Autowired private TagRepository tagRepository;

    @Test
    void buscarAvanzadoConTodosLosParametrosNulosDevuelveTodasLasPiezas() {
        // Dado
        piezaFisicaRepository.save(
                crearPieza(
                        "INV-2026-0001",
                        "Acta Fundacional",
                        "Documento historico firmado",
                        LocalDate.of(2026, 3, 15)));
        piezaFisicaRepository.save(
                crearPieza(
                        "INV-2026-0002",
                        "Afiche Cultural",
                        "Afiche impreso a color",
                        LocalDate.of(2026, 4, 10)));

        // Cuando
        Page<PiezaFisica> pagina =
                piezaFisicaRepository.buscarAvanzado(null, null, null, null, PageRequest.of(0, 10));

        // Entonces
        assertEquals(2, pagina.getTotalElements());
        assertEquals(2, pagina.getContent().size());
    }

    @Test
    void buscarAvanzadoSoloConTagCruzaPiezaDigitalizacionYTagSinDuplicar() {
        // Dado
        Tag tagFotografia = new Tag();
        tagFotografia.setNombre("FOTOGRAFIA");
        tagRepository.save(tagFotografia);

        PiezaFisica actaConDosFotosDelMismoTag =
                crearPieza(
                        "INV-2026-0001",
                        "Acta Fundacional",
                        "Documento historico firmado",
                        LocalDate.of(2026, 3, 15));
        actaConDosFotosDelMismoTag.agregarDigitalizacion(
                "https://archivo/acta-frente.jpg", "Frente");
        actaConDosFotosDelMismoTag.agregarDigitalizacion("https://archivo/acta-dorso.jpg", "Dorso");
        actaConDosFotosDelMismoTag.getDigitalizaciones().get(0).asociarTag(tagFotografia);
        actaConDosFotosDelMismoTag.getDigitalizaciones().get(1).asociarTag(tagFotografia);
        piezaFisicaRepository.save(actaConDosFotosDelMismoTag);

        PiezaFisica piezaSinTag =
                crearPieza(
                        "INV-2026-0002",
                        "Afiche Cultural",
                        "Afiche impreso a color",
                        LocalDate.of(2026, 4, 10));
        piezaFisicaRepository.save(piezaSinTag);

        // Cuando
        Page<PiezaFisica> pagina =
                piezaFisicaRepository.buscarAvanzado(
                        null, null, null, "fotografia", PageRequest.of(0, 10));

        // Entonces
        assertEquals(1, pagina.getTotalElements());
        assertEquals("INV-2026-0001", pagina.getContent().get(0).getCodigoInventario());
    }

    @Test
    void buscarAvanzadoFiltraPorTerminoFechasYTag() {
        // Dado
        Tag tagRestaurado = new Tag();
        tagRestaurado.setNombre("RESTAURADO");
        tagRepository.save(tagRestaurado);

        PiezaFisica piezaCoincidente =
                crearPieza(
                        "INV-2026-0010",
                        "Carta de Fundacion",
                        "Manuscrito restaurado en laboratorio",
                        LocalDate.of(2026, 5, 20));
        piezaCoincidente.agregarDigitalizacion("https://archivo/carta.pdf", "PDF completo");
        piezaCoincidente.getDigitalizaciones().get(0).asociarTag(tagRestaurado);
        piezaFisicaRepository.save(piezaCoincidente);

        PiezaFisica piezaFueraDeFecha =
                crearPieza(
                        "INV-2026-0011",
                        "Carta Antigua",
                        "Manuscrito restaurado en laboratorio",
                        LocalDate.of(2025, 1, 10));
        piezaFueraDeFecha.agregarDigitalizacion("https://archivo/carta2.pdf", "PDF antiguo");
        piezaFueraDeFecha.getDigitalizaciones().get(0).asociarTag(tagRestaurado);
        piezaFisicaRepository.save(piezaFueraDeFecha);

        // Cuando
        Page<PiezaFisica> pagina =
                piezaFisicaRepository.buscarAvanzado(
                        "laboratorio",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        "restaurado",
                        PageRequest.of(0, 10));

        // Entonces
        assertEquals(1, pagina.getTotalElements());
        assertEquals("INV-2026-0010", pagina.getContent().get(0).getCodigoInventario());
    }

    @Test
    void buscarAvanzadoRespetaPaginacion() {
        // Dado
        piezaFisicaRepository.save(
                crearPieza("INV-2026-0101", "Pieza 1", "Detalle 1", LocalDate.of(2026, 1, 10)));
        piezaFisicaRepository.save(
                crearPieza("INV-2026-0102", "Pieza 2", "Detalle 2", LocalDate.of(2026, 2, 10)));
        piezaFisicaRepository.save(
                crearPieza("INV-2026-0103", "Pieza 3", "Detalle 3", LocalDate.of(2026, 3, 10)));

        // Cuando
        Page<PiezaFisica> primeraPagina =
                piezaFisicaRepository.buscarAvanzado(null, null, null, null, PageRequest.of(0, 2));
        Page<PiezaFisica> segundaPagina =
                piezaFisicaRepository.buscarAvanzado(null, null, null, null, PageRequest.of(1, 2));

        // Entonces
        assertEquals(3, primeraPagina.getTotalElements());
        assertEquals(2, primeraPagina.getTotalPages());
        assertEquals(2, primeraPagina.getContent().size());
        assertEquals(1, segundaPagina.getContent().size());
    }

    private PiezaFisica crearPieza(
            String codigoInventario, String nombre, String descripcion, LocalDate fechaIngreso) {
        PiezaFisica pieza = new PiezaFisica();
        pieza.setCodigoInventario(codigoInventario);
        pieza.setNombre(nombre);
        pieza.setDescripcion(descripcion);
        pieza.setFechaIngreso(fechaIngreso);
        pieza.setTipoMaterial("Documento Papel");
        pieza.setEstado(EstadoPieza.EN_DEPOSITO);
        return pieza;
    }
}
