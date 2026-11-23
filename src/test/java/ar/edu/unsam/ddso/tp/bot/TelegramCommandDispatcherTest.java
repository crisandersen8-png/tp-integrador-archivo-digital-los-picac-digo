package ar.edu.unsam.ddso.tp.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.unsam.ddso.tp.bot.config.TelegramBotConfig;
import ar.edu.unsam.ddso.tp.bot.dispatcher.TelegramCommandDispatcher;
import ar.edu.unsam.ddso.tp.bot.formatter.TelegramMessageFormatter;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import ar.edu.unsam.ddso.tp.service.DisponibilizacionService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TelegramCommandDispatcherTest {

    @Mock
    private DisponibilizacionService disponibilizacionService;

    private TelegramMessageFormatter formatter;
    private TelegramBotConfig botConfig;
    private TelegramCommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        formatter = new TelegramMessageFormatter();
        botConfig = new TelegramBotConfig();
        botConfig.setUsername("archivo_digital_bot");
        dispatcher = new TelegramCommandDispatcher(disponibilizacionService, formatter, botConfig);
    }

    @Test
    void procesarMensaje_mensajeNuloOVacio_retornaNull() {
        assertNull(dispatcher.procesarMensaje(123L, null));
        assertNull(dispatcher.procesarMensaje(123L, "   "));
    }

    @Test
    void procesarMensaje_start_retornaBienvenida() {
        String respuesta = dispatcher.procesarMensaje(123L, "/start");

        assertNotNull(respuesta);
        assertTrue(respuesta.contains("ARCHIVO DIGITAL"));
    }

    @Test
    void procesarMensaje_ayuda_retornaAyuda() {
        String respuesta = dispatcher.procesarMensaje(123L, "/ayuda");

        assertNotNull(respuesta);
        assertTrue(respuesta.contains("/buscar <termino>"));
    }

    @Test
    void procesarMensaje_buscarSinArgumentos_retornaUsoBuscar() {
        String respuesta = dispatcher.procesarMensaje(123L, "/buscar");

        assertNotNull(respuesta);
        assertTrue(respuesta.contains("Debes indicar al menos un termino o etiqueta"));
    }

    @Test
    void procesarMensaje_buscarConTermino_llamaDisponibilizacionYRetornaResultados() {
        // Dado
        PiezaFisicaResponseDTO pieza =
                new PiezaFisicaResponseDTO(
                        1L, "INV-001", "Acta de Fundacion", "Descripcion",
                        LocalDate.now(), "Papel", EstadoPieza.EN_DEPOSITO, 1);
        Page<PiezaFisicaResponseDTO> page =
                new PageImpl<>(List.of(pieza), PageRequest.of(0, 5), 1);

        when(disponibilizacionService.buscarPiezas(eq("acta"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        // Cuando
        String respuesta = dispatcher.procesarMensaje(123L, "/buscar acta");

        // Entonces
        assertNotNull(respuesta);
        assertTrue(respuesta.contains("Acta de Fundacion"));
        assertTrue(respuesta.contains("INV-001"));
        verify(disponibilizacionService)
                .buscarPiezas(eq("acta"), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void procesarMensaje_buscarConTag_llamaDisponibilizacionConTag() {
        // Dado
        Page<PiezaFisicaResponseDTO> page = Page.empty();
        when(disponibilizacionService.buscarPiezas(isNull(), isNull(), isNull(), eq("FOTOGRAFIA"), any(Pageable.class)))
                .thenReturn(page);

        // Cuando
        String respuesta = dispatcher.procesarMensaje(123L, "/buscar tag:FOTOGRAFIA");

        // Entonces
        assertNotNull(respuesta);
        assertTrue(respuesta.contains("No se encontraron piezas fisicas"));
        verify(disponibilizacionService)
                .buscarPiezas(isNull(), isNull(), isNull(), eq("FOTOGRAFIA"), any(Pageable.class));
    }

    @Test
    void procesarMensaje_buscarConPaginacion_calculaPaginaBaseCeroYPreservaFiltros() {
        // Dado
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        Page<PiezaFisicaResponseDTO> page = Page.empty();
        when(disponibilizacionService.buscarPiezas(eq("expediente"), isNull(), isNull(), eq("HISTORICO"), any(Pageable.class)))
                .thenReturn(page);

        // Cuando
        dispatcher.procesarMensaje(123L, "/buscar expediente tag:HISTORICO p:3");

        // Entonces
        verify(disponibilizacionService)
                .buscarPiezas(eq("expediente"), isNull(), isNull(), eq("HISTORICO"), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(2, capturedPageable.getPageNumber()); // Pagina 3 humana = index 2
        assertEquals(5, capturedPageable.getPageSize());
    }

    @Test
    void procesarMensaje_buscarConPaginaInvalidaMenorAUno_retornaMensajeErrorPagina() {
        String respuesta = dispatcher.procesarMensaje(123L, "/buscar acta p:0");

        assertNotNull(respuesta);
        assertTrue(respuesta.contains("mayor o igual a 1"));
    }

    @Test
    void procesarMensaje_comandoGrupoConBotPropio_procesaCorrectamente() {
        String respuesta = dispatcher.procesarMensaje(123L, "/ayuda@archivo_digital_bot");

        assertNotNull(respuesta);
        assertTrue(respuesta.contains("GUIA DE COMANDOS DISPONIBLES"));
    }

    @Test
    void procesarMensaje_comandoGrupoConOtroBot_retornaNullParaIgnorarlo() {
        String respuesta = dispatcher.procesarMensaje(123L, "/buscar@otro_bot acta");

        assertNull(respuesta);
    }

    @Test
    void procesarMensaje_comandoDesconocido_retornaMensajeDeErrorYSugerencia() {
        String respuesta = dispatcher.procesarMensaje(123L, "/invalido");

        assertNotNull(respuesta);
        assertTrue(respuesta.contains("Comando no reconocido"));
        assertTrue(respuesta.contains("/ayuda"));
    }

    @Test
    void procesarMensaje_mensajeSinBarra_retornaSaludoYGuia() {
        String respuesta = dispatcher.procesarMensaje(123L, "hola que tal");

        assertNotNull(respuesta);
        assertTrue(respuesta.contains("Para consultar piezas utiliza el comando /buscar"));
    }

    @Test
    void procesarMensaje_errorEnServicio_capturaExcepcionYRetornaMensajeError() {
        // Dado
        when(disponibilizacionService.buscarPiezas(eq("error"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Fallo de base de datos"));

        // Cuando
        String respuesta = dispatcher.procesarMensaje(123L, "/buscar error");

        // Entonces
        assertNotNull(respuesta);
        assertTrue(respuesta.contains("Fallo de base de datos"));
    }
}
