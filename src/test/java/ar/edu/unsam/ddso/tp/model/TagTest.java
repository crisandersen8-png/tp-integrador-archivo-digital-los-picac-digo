package ar.edu.unsam.ddso.tp.model;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.unsam.ddso.tp.model.exceptions.TagInvalidoException;
import org.junit.jupiter.api.Test;

class TagTest {

    @Test
    void validarTagConNombreValidoNoLanzaExcepcion() {
        // Dado
        Tag tag = new Tag();
        tag.setNombre("FOTOGRAFIA");

        // Cuando / Entonces
        assertDoesNotThrow(tag::validar);
    }

    @Test
    void validarTagConNombreNuloOVacioLanzaTagInvalidoException() {
        // Dado
        Tag tagNulo = new Tag();
        Tag tagEnBlanco = new Tag();
        tagEnBlanco.setNombre("   ");

        // Cuando / Entonces
        assertThrows(TagInvalidoException.class, tagNulo::validar);
        assertThrows(TagInvalidoException.class, tagEnBlanco::validar);
    }
}

