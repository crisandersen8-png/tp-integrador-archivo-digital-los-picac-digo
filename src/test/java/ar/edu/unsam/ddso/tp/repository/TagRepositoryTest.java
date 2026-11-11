package ar.edu.unsam.ddso.tp.repository;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.unsam.ddso.tp.model.Tag;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class TagRepositoryTest {

    @Autowired private TagRepository tagRepository;

    @Test
    void findByNombreIgnoreCaseYExistsByNombreIgnoreCaseIgnoranMayusculasYMinusculas() {
        // Dado
        Tag tag = new Tag();
        tag.setNombre("RESTAURADO");
        tagRepository.save(tag);

        // Cuando
        Optional<Tag> encontrado = tagRepository.findByNombreIgnoreCase("restaurado");
        boolean existe = tagRepository.existsByNombreIgnoreCase("ReStAuRaDo");

        // Entonces
        assertTrue(encontrado.isPresent());
        assertEquals("RESTAURADO", encontrado.get().getNombre());
        assertTrue(existe);
    }
}
