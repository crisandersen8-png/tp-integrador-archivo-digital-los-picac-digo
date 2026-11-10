package ar.edu.unsam.ddso.tp.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PiezaFisicaTest {

    @Test
    void agregarDigitalizacionCreaYVinculaAmbosLados() {
        // Dado
        PiezaFisica pieza = new PiezaFisica();

        // Cuando
        pieza.agregarDigitalizacion("https://archivo/scan-001.jpg", "Escaneo frontal");

        // Entonces
        assertEquals(1, pieza.getDigitalizaciones().size());
        Digitalizacion digitalizacion = pieza.getDigitalizaciones().get(0);
        assertEquals("https://archivo/scan-001.jpg", digitalizacion.getUrlRecurso());
        assertEquals("Escaneo frontal", digitalizacion.getDescripcion());
        assertNotNull(digitalizacion.getFechaDigitalizacion());
        assertSame(pieza, digitalizacion.getPiezaFisica());
    }

    @Test
    void quitarDigitalizacionDesvinculaAmbosLados() {
        // Dado
        PiezaFisica pieza = new PiezaFisica();
        pieza.agregarDigitalizacion("https://archivo/scan-001.jpg", "Escaneo frontal");
        Digitalizacion digitalizacion = pieza.getDigitalizaciones().get(0);

        // Cuando
        pieza.quitarDigitalizacion(digitalizacion);

        // Entonces
        assertTrue(pieza.getDigitalizaciones().isEmpty());
        assertNull(digitalizacion.getPiezaFisica());
    }

    @Test
    void quitarDigitalizacionDeOtraPiezaNoLaDesvincula() {
        // Dado
        PiezaFisica pieza = new PiezaFisica();
        PiezaFisica otraPieza = new PiezaFisica();
        otraPieza.agregarDigitalizacion("https://archivo/scan-002.jpg", "Reverso");
        Digitalizacion digitalizacion = otraPieza.getDigitalizaciones().get(0);

        // Cuando
        pieza.quitarDigitalizacion(digitalizacion);

        // Entonces
        assertSame(otraPieza, digitalizacion.getPiezaFisica());
        assertEquals(1, otraPieza.getDigitalizaciones().size());
    }
}
