package ar.edu.unsam.ddso.tp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.unsam.ddso.tp.dto.AsociarTagRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitalizacionResponseDTO;
import ar.edu.unsam.ddso.tp.dto.TagCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.TagResponseDTO;
import ar.edu.unsam.ddso.tp.exception.DigitalizacionNoEncontradaException;
import ar.edu.unsam.ddso.tp.exception.TagDuplicadoException;
import ar.edu.unsam.ddso.tp.exception.TagNoEncontradoException;
import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.Tag;
import ar.edu.unsam.ddso.tp.model.exceptions.TagInvalidoException;
import ar.edu.unsam.ddso.tp.repository.DigitalizacionRepository;
import ar.edu.unsam.ddso.tp.repository.TagRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogacionServiceTest {

    @Mock private TagRepository tagRepository;

    @Mock private DigitalizacionRepository digitalizacionRepository;

    private CatalogacionService catalogacionService;

    @BeforeEach
    void setUp() {
        catalogacionService = new CatalogacionService(tagRepository, digitalizacionRepository);
    }

    @Test
    void listarTagsDevuelveTodosLosTagsMapeadosADTO() {
        // Dado
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setNombre("FOTOGRAFIA");
        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setNombre("RESTAURADO");
        when(tagRepository.findAll()).thenReturn(List.of(tag1, tag2));

        // Cuando
        List<TagResponseDTO> respuesta = catalogacionService.listarTags();

        // Entonces
        assertEquals(2, respuesta.size());
        assertEquals("FOTOGRAFIA", respuesta.get(0).nombre());
        assertEquals("RESTAURADO", respuesta.get(1).nombre());
    }

    @Test
    void crearTagNombreValidoYNoDuplicadoGuardaYDevuelveDTO() {
        // Dado
        TagCreateRequestDTO request = new TagCreateRequestDTO("RESTAURADO");
        when(tagRepository.existsByNombreIgnoreCase("RESTAURADO")).thenReturn(false);
        when(tagRepository.save(any(Tag.class)))
                .thenAnswer(
                        invocacion -> {
                            Tag guardado = invocacion.getArgument(0);
                            guardado.setId(7L);
                            return guardado;
                        });

        // Cuando
        TagResponseDTO respuesta = catalogacionService.crearTag(request);

        // Entonces
        assertEquals(7L, respuesta.id());
        assertEquals("RESTAURADO", respuesta.nombre());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void crearTagNombreDuplicadoLanzaTagDuplicadoException() {
        // Dado
        TagCreateRequestDTO request = new TagCreateRequestDTO("RESTAURADO");
        when(tagRepository.existsByNombreIgnoreCase("RESTAURADO")).thenReturn(true);

        // Cuando / Entonces
        assertThrows(TagDuplicadoException.class, () -> catalogacionService.crearTag(request));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void crearTagNombreEnBlancoLanzaTagInvalidoException() {
        // Dado
        TagCreateRequestDTO requestInvalido = new TagCreateRequestDTO("   ");

        // Cuando / Entonces
        assertThrows(
                TagInvalidoException.class, () -> catalogacionService.crearTag(requestInvalido));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void asociarTagADigitalizacionExistenteVinculaYDevuelveDTO() {
        // Dado
        PiezaFisica pieza = new PiezaFisica();
        pieza.setId(1L);

        Digitalizacion digitalizacion = new Digitalizacion();
        digitalizacion.setId(11L);
        digitalizacion.setPiezaFisica(pieza);
        digitalizacion.setUrlRecurso("https://storage.archivo-digital.org/fondos/inv-0042.jpg");
        digitalizacion.setDescripcion("Dorso del documento");
        digitalizacion.setFechaDigitalizacion(LocalDateTime.of(2026, 4, 2, 10, 15));

        Tag tag = new Tag();
        tag.setId(7L);
        tag.setNombre("RESTAURADO");

        when(digitalizacionRepository.findById(11L)).thenReturn(Optional.of(digitalizacion));
        when(tagRepository.findById(7L)).thenReturn(Optional.of(tag));
        when(digitalizacionRepository.save(any(Digitalizacion.class))).thenReturn(digitalizacion);

        // Cuando
        DigitalizacionResponseDTO respuesta =
                catalogacionService.asociarTagADigitalizacion(11L, new AsociarTagRequestDTO(7L));

        // Entonces
        assertEquals(11L, respuesta.id());
        assertEquals(1L, respuesta.piezaFisicaId());
        assertEquals(1, respuesta.tags().size());
        assertEquals("RESTAURADO", respuesta.tags().get(0).nombre());
        verify(digitalizacionRepository).save(digitalizacion);
    }

    @Test
    void asociarTagADigitalizacionInexistenteLanzaDigitalizacionNoEncontradaException() {
        // Dado
        when(digitalizacionRepository.findById(99L)).thenReturn(Optional.empty());

        // Cuando / Entonces
        assertThrows(
                DigitalizacionNoEncontradaException.class,
                () ->
                        catalogacionService.asociarTagADigitalizacion(
                                99L, new AsociarTagRequestDTO(7L)));
        verify(digitalizacionRepository, never()).save(any(Digitalizacion.class));
    }

    @Test
    void asociarTagInexistenteADigitalizacionLanzaTagNoEncontradoException() {
        // Dado
        Digitalizacion digitalizacion = new Digitalizacion();
        digitalizacion.setId(11L);
        when(digitalizacionRepository.findById(11L)).thenReturn(Optional.of(digitalizacion));
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        // Cuando / Entonces
        assertThrows(
                TagNoEncontradoException.class,
                () ->
                        catalogacionService.asociarTagADigitalizacion(
                                11L, new AsociarTagRequestDTO(99L)));
        verify(digitalizacionRepository, never()).save(any(Digitalizacion.class));
    }
}

