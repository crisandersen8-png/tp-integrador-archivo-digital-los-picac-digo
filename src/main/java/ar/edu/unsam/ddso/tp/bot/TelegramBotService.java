package ar.edu.unsam.ddso.tp.bot;

import ar.edu.unsam.ddso.tp.bot.config.TelegramBotConfig;
import ar.edu.unsam.ddso.tp.bot.dispatcher.TelegramCommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Servicio de integracion con Telegram Bot mediante Long Polling.
 * Activo solo cuando la propiedad {@code telegram.bot.enabled=true}.
 */
@Component
@ConditionalOnProperty(prefix = "telegram.bot", name = "enabled", havingValue = "true", matchIfMissing = false)
public class TelegramBotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramBotService.class);

    private final TelegramBotConfig botConfig;
    private final TelegramCommandDispatcher dispatcher;
    private final TelegramClient telegramClient;

    public TelegramBotService(TelegramBotConfig botConfig, TelegramCommandDispatcher dispatcher) {
        this(botConfig, dispatcher, new OkHttpTelegramClient(botConfig.getToken()));
    }

    public TelegramBotService(
            TelegramBotConfig botConfig,
            TelegramCommandDispatcher dispatcher,
            TelegramClient telegramClient) {
        this.botConfig = botConfig;
        this.dispatcher = dispatcher;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        try {
            if (update != null && update.hasMessage() && update.getMessage().hasText()) {
                Long chatId = update.getMessage().getChatId();
                String texto = update.getMessage().getText();

                String respuesta = dispatcher.procesarMensaje(chatId, texto);
                if (respuesta != null && !respuesta.isBlank()) {
                    SendMessage mensaje = new SendMessage(chatId.toString(), respuesta);
                    telegramClient.execute(mensaje);
                }
            }
        } catch (Exception e) {
            LOG.error("Error inesperado al procesar actualizacion de Telegram: {}", e.getMessage(), e);
        }
    }
}
