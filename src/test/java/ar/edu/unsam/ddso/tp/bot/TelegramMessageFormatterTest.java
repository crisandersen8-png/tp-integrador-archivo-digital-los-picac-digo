package ar.edu.unsam.ddso.tp.bot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ar.edu.unsam.ddso.tp.bot.formatter.TelegramMessageFormatter;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class TelegramMessageFormatterTest {

    private TelegramMessageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new TelegramMessageFormatter();
    }

    @Test
    void formatearBienvenida_retornaTextoInstitucional() {
        // Cuando
        String mensaje = formatter.formatearBienvenida();

        // Entonces
        assertNotNull(mensaje);
        assertTrue(mensaje.contains("ARCHIVO DIGITAL"));
        assertTrue(mensaje.contains("/ayuda"));
    }

    @Test
    void formatearAyuda_retornaGuiaDeComandos() {
        // Cuando
        String mensaje = formatter.formatearAyuda();

        // Entonces
        assertNotNull(mensaje);
        assertTrue(mensaje.contains("/buscar <termino>"));
        assertTrue(mensaje.contains("tag:<etiqueta>"));
        assertTrue(mensaje.contains("p:<numero>"));
    }

    @Test
    void formatearUsoBuscar_retornaIndicaciones() {
        // Cuando
        String mensaje = formatter.formatearUsoBuscar();

        // Entonces
        assertNotNull(mensaje);
        assertTrue(mensaje.contains("Debes indicar al menos un termino o etiqueta"));
    }

    @Test
    void formatearResultados_conPaginaVacia_retornaAvisoSinResultados() {
        // Dado
        Page<PiezaFisicaResponseDTO> paginaVacia = Page.empty();

        // Cuando
        String mensaje = formatter.formatearResultados(paginaVacia, "inmueble", 1);

        // Entonces
        assertNotNull(mensaje);
        assertTrue(mensaje.contains("No se encontraron piezas fisicas"));
        assertTrue(mensaje.contains("inmueble"));
    }

    @Test
    void formatearResultados_conResultados_formateaDatosYMetadatos() {
        // Dado
        PiezaFisicaResponseDTO pieza =
                new PiezaFisicaResponseDTO(
                        1L,
                        "INV-001",
                        "Acta Fundacional",
                        "Manuscrito en papel",
                        LocalDate.of(2026, 3, 15),
                        "Papel",
                        EstadoPieza.EN_DEPOSITO,
                        2);
        Page<PiezaFisicaResponseDTO> pagina =
                new PageImpl<>(List.of(pieza), PageRequest.of(0, 5), 1);

        // Cuando
        String mensaje = formatter.formatearResultados(pagina, "acta", 1);

        // Entonces
        assertNotNull(mensaje);
        assertTrue(mensaje.contains("INV-001"));
        assertTrue(mensaje.contains("Acta Fundacional"));
        assertTrue(mensaje.contains("Papel"));
        assertTrue(mensaje.contains("EN_DEPOSITO"));
        assertTrue(mensaje.contains("Digitalizaciones: 2"));
        assertTrue(mensaje.contains("Manuscrito en papel"));
    }

    @Test
    void formatearResultados_conPaginaSiguiente_incluyeComandoDeNavegacion() {
        // Dado
        PiezaFisicaResponseDTO pieza =
                new PiezaFisicaResponseDTO(
                        1L, "INV-001", "Pieza 1", null, LocalDate.now(), "Metal", EstadoPieza.EN_EXHIBICION, 0);
        // Simulamos que hay 10 elementos en total con size 5, estamos en pagina 0 (hay pagina siguiente)
        Page<PiezaFisicaResponseDTO> pagina =
                new PageImpl<>(List.of(pieza), PageRequest.of(0, 5), 10);

        // Cuando
        String mensaje = formatter.formatearResultados(pagina, "oleo", 1);

        // Entonces
        assertTrue(mensaje.contains("Pagina 1 de 2"));
        assertTrue(mensaje.contains("➡️ Pagina siguiente: /buscar oleo p:2"));
        assertFalse(mensaje.contains("⬅️ Pagina anterior"));
    }

    @Test
    void formatearResultados_conPaginaIntermedia_incluyeAmbasNavegaciones() {
        // Dado
        PiezaFisicaResponseDTO pieza =
                new PiezaFisicaResponseDTO(
                        2L, "INV-002", "Pieza 2", null, LocalDate.now(), "Madera", EstadoPieza.EN_PRESTAMO, 1);
        // Estamos en pagina 1 (base 0) de un total de 3 paginas
        Page<PiezaFisicaResponseDTO> pagina =
                new PageImpl<>(List.of(pieza), PageRequest.of(1, 5), 15);

        // Cuando
        String mensaje = formatter.formatearResultados(pagina, "escultura", 2);

        // Entonces
        assertTrue(mensaje.contains("Pagina 2 de 3"));
        assertTrue(mensaje.contains("⬅️ Pagina anterior: /buscar escultura p:1"));
        assertTrue(mensaje.contains("➡️ Pagina siguiente: /buscar escultura p:3"));
    }

    @Test
    void formatearResultados_conDescripcionLarga_truncaConPuntosSuspensivos() {
        // Dado
        String descripcionLarga = "A".repeat(150);
        PiezaFisicaResponseDTO pieza =
                new PiezaFisicaResponseDTO(
                        1L, "INV-001", "Pieza", descripcionLarga, LocalDate.now(), "Papel", EstadoPieza.EN_DEPOSITO, 0);
        Page<PiezaFisicaResponseDTO> pagina =
                new PageImpl<>(List.of(pieza), PageRequest.of(0, 5), 1);

        // Cuando
        String mensaje = formatter.formatearResultados(pagina, "termino", 1);

        // Entonces
        assertTrue(mensaje.contains("A".repeat(100) + "..."));
        assertFalse(mensaje.contains("A".repeat(150)));
    }

    @Test
    void formatearResultados_conPaginaSolicitadaFueraDeRango_retornaMensajeDeError() {
        // Dado
        PiezaFisicaResponseDTO pieza =
                new PiezaFisicaResponseDTO(
                        1L, "INV-001", "Pieza", null, LocalDate.now(), "Papel", EstadoPieza.EN_DEPOSITO, 0);
        Page<PiezaFisicaResponseDTO> pagina =
                new PageImpl<>(List.of(pieza), PageRequest.of(0, 5), 1);

        // Cuando el usuario pidio pagina 5 pero solo hay 1 pagina
        String mensaje = formatter.formatearResultados(pagina, "termino", 5);

        // Entonces
        assertTrue(mensaje.contains("La pagina solicitada (5) no existe"));
        assertTrue(mensaje.contains("Total de paginas disponibles: 1"));
    }

    @Test
    void formatearResultados_mensajeMuyExtenso_respetaLimiteTelegram() {
        // Dado muchas piezas con texto
        List<PiezaFisicaResponseDTO> piezas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            piezas.add(
                    new PiezaFisicaResponseDTO(
                            (long) i,
                            "INV-" + i,
                            "Pieza " + i,
                            "Descripcion bastante extensa que suma muchos caracteres " + i,
                            LocalDate.now(),
                            "Material " + i,
                            EstadoPieza.EN_DEPOSITO,
                            i));
        }
        Page<PiezaFisicaResponseDTO> paginaGrande =
                new PageImpl<>(piezas, PageRequest.of(0, 50), 50);

        // Cuando
        String mensaje = formatter.formatearResultados(paginaGrande, "test", 1);

        // Entonces
        assertTrue(mensaje.length() <= 4096);
    }
}
