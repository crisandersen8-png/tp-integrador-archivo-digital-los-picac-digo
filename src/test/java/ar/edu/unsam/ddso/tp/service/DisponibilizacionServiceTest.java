package ar.edu.unsam.ddso.tp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.unsam.ddso.tp.dto.PaginaResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.exception.RangoFechasInvalidoException;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import ar.edu.unsam.ddso.tp.repository.PiezaFisicaRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DisponibilizacionServiceTest {

    @Mock private PiezaFisicaRepository piezaFisicaRepository;

    private DisponibilizacionService disponibilizacionService;

    @BeforeEach
    void setUp() {
        disponibilizacionService = new DisponibilizacionService(piezaFisicaRepository);
    }

    @Test
    void buscarPiezasConFiltrosValidosDevuelvePaginaResponseDTO() {
        // Dado
        PiezaFisica pieza = new PiezaFisica();
        pieza.setId(1L);
        pieza.setCodigoInventario("INV-2026-0042");
        pieza.setNombre("Acta Fundacional");
        pieza.setDescripcion("Manuscrito historico");
        pieza.setFechaIngreso(LocalDate.of(2026, 3, 15));
        pieza.setTipoMaterial("Documento Papel");
        pieza.setEstado(EstadoPieza.EN_DEPOSITO);

        Pageable pageable = PageRequest.of(0, 10);
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 12, 31);

        when(piezaFisicaRepository.buscarAvanzado("acta", desde, hasta, "FOTOGRAFIA", pageable))
                .thenReturn(new PageImpl<>(List.of(pieza), pageable, 1));

        // Cuando
        PaginaResponseDTO<PiezaFisicaResponseDTO> resultado =
                disponibilizacionService.buscarPiezas(
                        "acta", desde, hasta, "FOTOGRAFIA", pageable);

        // Entonces
        assertEquals(1, resultado.totalElements());
        assertEquals(1, resultado.totalPages());
        assertEquals(0, resultado.pageNumber());
        assertEquals(10, resultado.pageSize());
        assertTrue(resultado.last());
        assertEquals("INV-2026-0042", resultado.content().get(0).codigoInventario());
        assertEquals("Acta Fundacional", resultado.content().get(0).nombre());
    }

    @Test
    void buscarPiezasConFechaDesdePosteriorAFechaHastaLanzaRangoFechasInvalidoException() {
        // Dado
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate desde = LocalDate.of(2026, 12, 31);
        LocalDate hasta = LocalDate.of(2026, 1, 1);

        // Cuando / Entonces
        assertThrows(
                RangoFechasInvalidoException.class,
                () -> disponibilizacionService.buscarPiezas(null, desde, hasta, null, pageable));
        verify(piezaFisicaRepository, never()).buscarAvanzado(null, desde, hasta, null, pageable);
    }
}
