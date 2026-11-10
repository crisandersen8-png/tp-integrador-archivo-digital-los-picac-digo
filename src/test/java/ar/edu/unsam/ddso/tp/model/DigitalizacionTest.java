package ar.edu.unsam.ddso.tp.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DigitalizacionTest {

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
