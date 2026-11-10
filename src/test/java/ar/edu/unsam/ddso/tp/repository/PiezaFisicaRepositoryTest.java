package ar.edu.unsam.ddso.tp.repository;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.Tag;
import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class PiezaFisicaRepositoryTest {

    @Autowired private PiezaFisicaRepository piezaFisicaRepository;

    @Autowired private DigitalizacionRepository digitalizacionRepository;

    @Autowired private TagRepository tagRepository;

    @Test
    void buscarPorNombreTagCruzaPiezaDigitalizacionYTag() {
        // Dado
        Tag tagFotografia = new Tag();
        tagFotografia.setNombre("FOTOGRAFIA");
        tagRepository.save(tagFotografia);

        Tag tagManuscrito = new Tag();
        tagManuscrito.setNombre("MANUSCRITO");
        tagRepository.save(tagManuscrito);

        PiezaFisica acta =
                crearPieza(
                        "INV-2026-0001",
                        "Acta Fundacional",
                        "Documento historico firmado",
                        LocalDate.of(2026, 3, 15));
        acta.agregarDigitalizacion("https://archivo/acta-frente.jpg", "Escaneo frontal");
        acta.getDigitalizaciones().get(0).asociarTag(tagFotografia);
        acta.getDigitalizaciones().get(0).asociarTag(tagManuscrito);
        piezaFisicaRepository.save(acta);

        PiezaFisica afiche =
                crearPieza(
                        "INV-2026-0002",
                        "Afiche Cultural",
                        "Afiche impreso a color",
                        LocalDate.of(2026, 4, 10));
        afiche.agregarDigitalizacion("https://archivo/afiche.jpg", "Foto completa");
        afiche.getDigitalizaciones().get(0).asociarTag(tagFotografia);
        piezaFisicaRepository.save(afiche);

        // Cuando
        List<PiezaFisica> conManuscrito = piezaFisicaRepository.buscarPorNombreTag("manuscrito");
        List<PiezaFisica> conFotografia = piezaFisicaRepository.buscarPorNombreTag("FOTOGRAFIA");

        // Entonces
        assertEquals(1, conManuscrito.size());
        assertEquals("INV-2026-0001", conManuscrito.get(0).getCodigoInventario());
        assertEquals(2, conFotografia.size());
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
                        "Manuscrito sin restaurar",
                        LocalDate.of(2025, 1, 10));
        piezaFisicaRepository.save(piezaFueraDeFecha);

        // Cuando
        List<PiezaFisica> resultado =
                piezaFisicaRepository.buscarAvanzado(
                        "laboratorio",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        "restaurado");

        // Entonces
        assertEquals(1, resultado.size());
        assertEquals("INV-2026-0010", resultado.get(0).getCodigoInventario());
    }

    @Test
    void digitalizacionYTagRepositoriesEjecutanConsultasDerivadasYPersonalizadas() {
        // Dado
        Tag tag = new Tag();
        tag.setNombre("DORSO");
        tagRepository.save(tag);

        PiezaFisica pieza =
                crearPieza(
                        "INV-2026-0050",
                        "Fotografia Original",
                        "Retrato grupal",
                        LocalDate.of(2026, 6, 1));
        pieza.agregarDigitalizacion("https://archivo/dorso.jpg", "Toma de dorso");
        pieza.getDigitalizaciones().get(0).asociarTag(tag);
        PiezaFisica guardada = piezaFisicaRepository.save(pieza);

        // Cuando
        Optional<Tag> tagBuscado = tagRepository.findByNombreIgnoreCase("dorso");
        List<Digitalizacion> porPieza =
                digitalizacionRepository.findByPiezaFisicaId(guardada.getId());
        List<Digitalizacion> porTag = digitalizacionRepository.buscarPorTagConDetalle("dorso");

        // Entonces
        assertTrue(tagBuscado.isPresent());
        assertTrue(tagRepository.existsByNombreIgnoreCase("DORSO"));
        assertEquals(1, porPieza.size());
        assertEquals(1, porTag.size());
        assertEquals("INV-2026-0050", porTag.get(0).getPiezaFisica().getCodigoInventario());
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

