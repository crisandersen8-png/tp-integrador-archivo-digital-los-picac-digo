package ar.edu.unsam.ddso.tp.model;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.unsam.ddso.tp.model.exceptions.DigitalizacionInvalidaException;
import org.junit.jupiter.api.Test;

class DigitalizacionTest {

    @Test
    void validarDigitalizacionConUrlHttpsValidaNoLanzaExcepcion() {
        // Dado
        Digitalizacion digitalizacion = new Digitalizacion();
        digitalizacion.setUrlRecurso("https://storage.archivo-digital.org/fondos/inv-0042.jpg");

        // Cuando / Entonces
        assertDoesNotThrow(digitalizacion::validar);
    }

    @Test
    void validarDigitalizacionConUrlVaciaOMalFormadaLanzaExcepcion() {
        // Dado
        Digitalizacion vacia = new Digitalizacion();
        vacia.setUrlRecurso("   ");

        Digitalizacion malFormada = new Digitalizacion();
        malFormada.setUrlRecurso("no-es-una-url");

        // Cuando / Entonces
        assertThrows(DigitalizacionInvalidaException.class, vacia::validar);
        assertThrows(DigitalizacionInvalidaException.class, malFormada::validar);
    }

    @Test
    void asociarTagVinculaAmbosLados() {
        // Dado
        Digitalizacion digitalizacion = new Digitalizacion();
        Tag tag = new Tag();

        // Cuando
        digitalizacion.asociarTag(tag);

        // Entonces
        assertTrue(digitalizacion.getTags().contains(tag));
        assertTrue(tag.getDigitalizaciones().contains(digitalizacion));
    }

    @Test
    void asociarMismoTagDosVecesMantieneUnaSolaAsociacion() {
        // Dado
        Digitalizacion digitalizacion = new Digitalizacion();
        Tag tag = new Tag();

        // Cuando
        digitalizacion.asociarTag(tag);
        digitalizacion.asociarTag(tag);

        // Entonces
        assertEquals(1, digitalizacion.getTags().size());
        assertEquals(1, tag.getDigitalizaciones().size());
    }

    @Test
    void desasociarTagDesvinculaAmbosLados() {
        // Dado
        Digitalizacion digitalizacion = new Digitalizacion();
        Tag tag = new Tag();
        digitalizacion.asociarTag(tag);

        // Cuando
        digitalizacion.desasociarTag(tag);

        // Entonces
        assertTrue(digitalizacion.getTags().isEmpty());
        assertTrue(tag.getDigitalizaciones().isEmpty());
    }
}
