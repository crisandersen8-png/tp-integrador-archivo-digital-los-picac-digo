package ar.edu.unsam.ddso.tp.bot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.unsam.ddso.tp.bot.config.TelegramBotConfig;
import ar.edu.unsam.ddso.tp.bot.dispatcher.TelegramCommandDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@ExtendWith(MockitoExtension.class)
class TelegramBotServiceTest {

    @Mock
    private TelegramCommandDispatcher dispatcher;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private Update update;

    @Mock
    private Message message;

    private TelegramBotConfig botConfig;
    private TelegramBotService botService;

    @BeforeEach
    void setUp() {
        botConfig = new TelegramBotConfig();
        botConfig.setToken("123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11");
        botConfig.setUsername("test_bot");
        botService = new TelegramBotService(botConfig, dispatcher, telegramClient);
    }

    @Test
    void getBotToken_retornaTokenConfigurado() {
        assertEquals("123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11", botService.getBotToken());
    }

    @Test
    void getUpdatesConsumer_retornaMismaInstancia() {
        assertSame(botService, botService.getUpdatesConsumer());
    }

    @Test
    void consume_mensajeConTexto_procesaYEnviaRespuesta() throws Exception {
        // Dado
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getChatId()).thenReturn(98765L);
        when(message.getText()).thenReturn("/buscar acta");
        when(dispatcher.procesarMensaje(98765L, "/buscar acta")).thenReturn("Resultados encontrados");

        // Cuando
        botService.consume(update);

        // Entonces
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());
        SendMessage enviado = captor.getValue();
        assertEquals("98765", enviado.getChatId());
        assertEquals("Resultados encontrados", enviado.getText());
    }

    @Test
    void consume_mensajeIgnoradoPorDispatcher_noEnviaMensaje() throws Exception {
        // Dado
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getChatId()).thenReturn(98765L);
        when(message.getText()).thenReturn("/buscar@otro_bot acta");
        when(dispatcher.procesarMensaje(98765L, "/buscar@otro_bot acta")).thenReturn(null);

        // Cuando
        botService.consume(update);

        // Entonces
        verify(telegramClient, never()).execute(any(SendMessage.class));
    }

    @Test
    void consume_updateNuloOSinMensaje_noEnviaMensaje() throws Exception {
        // Dado
        when(update.hasMessage()).thenReturn(false);

        // Cuando
        botService.consume((Update) null);
        botService.consume(update);

        // Entonces
        verify(telegramClient, never()).execute(any(SendMessage.class));
    }

    @Test
    void consume_excepcionAlEnviar_capturaYNoLanzaExcepcion() throws Exception {
        // Dado
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getChatId()).thenReturn(98765L);
        when(message.getText()).thenReturn("/start");
        when(dispatcher.procesarMensaje(98765L, "/start")).thenReturn("Bienvenido");
        doThrow(new TelegramApiException("Fallo de red al enviar mensaje"))
                .when(telegramClient).execute(any(SendMessage.class));

        // Cuando / Entonces: no debe propagar la excepcion ni romper la ejecucion
        assertDoesNotThrow(() -> botService.consume(update));
    }
}
