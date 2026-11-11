package ar.edu.unsam.ddso.tp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.unsam.ddso.tp.dto.DigitalizacionCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitalizacionResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaDetalleResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.exception.CodigoInventarioDuplicadoException;
import ar.edu.unsam.ddso.tp.exception.PiezaFisicaNoEncontradaException;
import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import ar.edu.unsam.ddso.tp.model.exceptions.DigitalizacionInvalidaException;
import ar.edu.unsam.ddso.tp.model.exceptions.PiezaFisicaInvalidaException;
import ar.edu.unsam.ddso.tp.repository.DigitalizacionRepository;
import ar.edu.unsam.ddso.tp.repository.PiezaFisicaRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PiezaFisicaServiceTest {

    @Mock private PiezaFisicaRepository piezaFisicaRepository;

    @Mock private DigitalizacionRepository digitalizacionRepository;

    private PiezaFisicaService piezaFisicaService;

    @BeforeEach
    void setUp() {
        piezaFisicaService =
                new PiezaFisicaService(piezaFisicaRepository, digitalizacionRepository);
    }

    @Test
    void crearPiezaDatosValidosGuardaYDevuelveDTO() {
        // Dado
        PiezaFisicaCreateRequestDTO request =
                new PiezaFisicaCreateRequestDTO(
                        "INV-2026-0042",
                        "Acta Fundacional del Instituto",
                        "Manuscrito en papel de lino",
                        LocalDate.of(2026, 3, 15),
                        "Documento Papel",
                        EstadoPieza.EN_DEPOSITO);
        when(piezaFisicaRepository.existsByCodigoInventario("INV-2026-0042")).thenReturn(false);
        when(piezaFisicaRepository.save(any(PiezaFisica.class)))
                .thenAnswer(
                        invocacion -> {
                            PiezaFisica guardada = invocacion.getArgument(0);
                            guardada.setId(1L);
                            return guardada;
                        });

        // Cuando
        PiezaFisicaResponseDTO respuesta = piezaFisicaService.crearPieza(request);

        // Entonces
        assertEquals(1L, respuesta.id());
        assertEquals("INV-2026-0042", respuesta.codigoInventario());
        assertEquals("Acta Fundacional del Instituto", respuesta.nombre());
        assertEquals(EstadoPieza.EN_DEPOSITO, respuesta.estado());
        assertEquals(0, respuesta.cantidadDigitalizaciones());
        verify(piezaFisicaRepository).save(any(PiezaFisica.class));
    }

    @Test
    void crearPiezaCodigoInventarioDuplicadoLanzaExcepcion() {
        // Dado
        PiezaFisicaCreateRequestDTO request =
                new PiezaFisicaCreateRequestDTO(
                        "INV-2026-0042",
                        "Acta Fundacional del Instituto",
                        "Manuscrito en papel de lino",
                        LocalDate.of(2026, 3, 15),
                        "Documento Papel",
                        null);
        when(piezaFisicaRepository.existsByCodigoInventario("INV-2026-0042")).thenReturn(true);

        // Cuando / Entonces
        assertThrows(
                CodigoInventarioDuplicadoException.class,
                () -> piezaFisicaService.crearPieza(request));
        verify(piezaFisicaRepository, never()).save(any(PiezaFisica.class));
    }

    @Test
    void crearPiezaSinNombreLanzaPiezaFisicaInvalidaException() {
        // Dado
        PiezaFisicaCreateRequestDTO requestInvalido =
                new PiezaFisicaCreateRequestDTO(
                        "INV-2026-0042",
                        "   ",
                        "Sin nombre",
                        LocalDate.of(2026, 3, 15),
                        "Documento Papel",
                        EstadoPieza.EN_DEPOSITO);

        // Cuando / Entonces
        assertThrows(
                PiezaFisicaInvalidaException.class,
                () -> piezaFisicaService.crearPieza(requestInvalido));
        verify(piezaFisicaRepository, never()).save(any(PiezaFisica.class));
    }

    @Test
    void obtenerPorIdExistenteDevuelveDetalleConDigitalizaciones() {
        // Dado
        PiezaFisica pieza = crearPiezaExistente(1L);
        pieza.agregarDigitalizacion(
                "https://storage.archivo-digital.org/fondos/inv-0042.jpg", "Escaneo frontal");
        when(piezaFisicaRepository.findById(1L)).thenReturn(Optional.of(pieza));

        // Cuando
        PiezaFisicaDetalleResponseDTO detalle = piezaFisicaService.obtenerPorId(1L);

        // Entonces
        assertEquals(1L, detalle.id());
        assertEquals("INV-2026-0042", detalle.codigoInventario());
        assertEquals(1, detalle.digitalizaciones().size());
        assertEquals(
                "https://storage.archivo-digital.org/fondos/inv-0042.jpg",
                detalle.digitalizaciones().get(0).urlRecurso());
    }

    @Test
    void obtenerPorIdInexistenteLanzaPiezaFisicaNoEncontradaException() {
        // Dado
        when(piezaFisicaRepository.findById(99L)).thenReturn(Optional.empty());

        // Cuando / Entonces
        assertThrows(
                PiezaFisicaNoEncontradaException.class, () -> piezaFisicaService.obtenerPorId(99L));
    }

    @Test
    void agregarDigitalizacionPiezaExistenteYUrlValidaGuardaYDevuelveDTO() {
        // Dado
        PiezaFisica pieza = crearPiezaExistente(1L);
        DigitalizacionCreateRequestDTO request =
                new DigitalizacionCreateRequestDTO(
                        "https://storage.archivo-digital.org/fondos/inv-0042.jpg",
                        "Escaneo alta resolucion");
        when(piezaFisicaRepository.findById(1L)).thenReturn(Optional.of(pieza));
        when(digitalizacionRepository.save(any(Digitalizacion.class)))
                .thenAnswer(
                        invocacion -> {
                            Digitalizacion guardada = invocacion.getArgument(0);
                            guardada.setId(10L);
                            return guardada;
                        });

        // Cuando
        DigitalizacionResponseDTO respuesta =
                piezaFisicaService.agregarDigitalizacion(1L, request);

        // Entonces
        assertEquals(10L, respuesta.id());
        assertEquals(1L, respuesta.piezaFisicaId());
        assertEquals(
                "https://storage.archivo-digital.org/fondos/inv-0042.jpg", respuesta.urlRecurso());
        assertEquals("Escaneo alta resolucion", respuesta.descripcion());
        assertNotNull(respuesta.fechaDigitalizacion());
        assertEquals(1, pieza.getDigitalizaciones().size());
        verify(digitalizacionRepository).save(any(Digitalizacion.class));
    }

    @Test
    void agregarDigitalizacionPiezaInexistenteLanzaPiezaFisicaNoEncontradaException() {
        // Dado
        DigitalizacionCreateRequestDTO request =
                new DigitalizacionCreateRequestDTO(
                        "https://storage.archivo-digital.org/fondos/inv-0042.jpg",
                        "Escaneo alta resolucion");
        when(piezaFisicaRepository.findById(99L)).thenReturn(Optional.empty());

        // Cuando / Entonces
        assertThrows(
                PiezaFisicaNoEncontradaException.class,
                () -> piezaFisicaService.agregarDigitalizacion(99L, request));
        verify(digitalizacionRepository, never()).save(any(Digitalizacion.class));
    }

    @Test
    void agregarDigitalizacionUrlInvalidaLanzaDigitalizacionInvalidaException() {
        // Dado
        PiezaFisica pieza = crearPiezaExistente(1L);
        DigitalizacionCreateRequestDTO requestInvalido =
                new DigitalizacionCreateRequestDTO("url-sin-formato", "Escaneo fallido");
        when(piezaFisicaRepository.findById(1L)).thenReturn(Optional.of(pieza));

        // Cuando / Entonces
        assertThrows(
                DigitalizacionInvalidaException.class,
                () -> piezaFisicaService.agregarDigitalizacion(1L, requestInvalido));
        verify(digitalizacionRepository, never()).save(any(Digitalizacion.class));
    }

    private PiezaFisica crearPiezaExistente(Long id) {
        PiezaFisica pieza = new PiezaFisica();
        pieza.setId(id);
        pieza.setCodigoInventario("INV-2026-0042");
        pieza.setNombre("Acta Fundacional del Instituto");
        pieza.setDescripcion("Manuscrito en papel de lino");
        pieza.setFechaIngreso(LocalDate.of(2026, 3, 15));
        pieza.setTipoMaterial("Documento Papel");
        pieza.setEstado(EstadoPieza.EN_DEPOSITO);
        return pieza;
    }
}
