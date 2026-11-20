package ar.edu.unsam.ddso.tp.bot.dispatcher;

import ar.edu.unsam.ddso.tp.bot.config.TelegramBotConfig;
import ar.edu.unsam.ddso.tp.bot.formatter.TelegramMessageFormatter;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.service.DisponibilizacionService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/** Despachador de comandos y consultas para el bot de Telegram. */
@Component
public class TelegramCommandDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramCommandDispatcher.class);

    private static final int TAMANIO_PAGINA_DEFECTO = 5;
    private static final Pattern PATRON_PAGINA = Pattern.compile("(?i)(?:^|\\s)p:(\\d+)(?:\\s|$)");
    private static final Pattern PATRON_TAG = Pattern.compile("(?i)(?:^|\\s)tag:([^\\s]+)(?:\\s|$)");

    private final DisponibilizacionService disponibilizacionService;
    private final TelegramMessageFormatter formatter;
    private final TelegramBotConfig botConfig;

    public TelegramCommandDispatcher(
            DisponibilizacionService disponibilizacionService,
            TelegramMessageFormatter formatter,
            TelegramBotConfig botConfig) {
        this.disponibilizacionService = disponibilizacionService;
        this.formatter = formatter;
        this.botConfig = botConfig;
    }

    /**
     * Procesa un mensaje de texto entrante y genera la respuesta para el usuario.
     *
     * @param chatId identificador unico del chat de Telegram
     * @param textoMensaje contenido textual del mensaje
     * @return texto de respuesta, o null si el mensaje debe ser ignorado (ej. comando para otro bot)
     */
    public String procesarMensaje(Long chatId, String textoMensaje) {
        if (textoMensaje == null || textoMensaje.isBlank()) {
            return null;
        }

        String texto = textoMensaje.trim();
        String[] partes = texto.split("\\s+", 2);
        String primerToken = partes[0];
        String argumentos = partes.length > 1 ? partes[1].trim() : "";

        String comando = primerToken;
        if (primerToken.contains("@")) {
            String[] partesMencion = primerToken.split("@", 2);
            comando = partesMencion[0];
            String botMencionado = partesMencion[1];
            String usernameConfigurado = botConfig.getUsername();

            if (usernameConfigurado != null && !usernameConfigurado.isBlank()) {
                String expected = usernameConfigurado.startsWith("@")
                        ? usernameConfigurado.substring(1)
                        : usernameConfigurado;
                if (!botMencionado.equalsIgnoreCase(expected)) {
                    LOG.debug("Mensaje ignorado en chat {}: dirigido a @{}", chatId, botMencionado);
                    return null;
                }
            }
        }

        LOG.info("Comando recibido en chat {}: {}", chatId, comando);

        return switch (comando.toLowerCase()) {
            case "/start" -> formatter.formatearBienvenida();
            case "/ayuda" -> formatter.formatearAyuda();
            case "/buscar" -> procesarBuscar(argumentos);
            default -> {
                if (comando.startsWith("/")) {
                    yield "⚠️ Comando no reconocido: \"" + comando + "\". Escribi /ayuda para ver la guia.";
                }
                yield "Hola. Para consultar piezas utiliza el comando /buscar <termino> o escribi /ayuda.";
            }
        };
    }

    private String procesarBuscar(String argumentos) {
        if (argumentos == null || argumentos.isBlank()) {
            return formatter.formatearUsoBuscar();
        }

        int paginaHumana = 1;
        String resto = argumentos;

        Matcher matcherPagina = PATRON_PAGINA.matcher(resto);
        if (matcherPagina.find()) {
            try {
                paginaHumana = Integer.parseInt(matcherPagina.group(1));
            } catch (NumberFormatException e) {
                paginaHumana = 1;
            }
            resto = matcherPagina.replaceFirst(" ");
        }

        if (paginaHumana < 1) {
            return "⚠️ El numero de pagina debe ser mayor o igual a 1.";
        }

        String tag = null;
        Matcher matcherTag = PATRON_TAG.matcher(resto);
        if (matcherTag.find()) {
            tag = matcherTag.group(1).trim();
            resto = matcherTag.replaceFirst(" ");
        }

        String termino = resto.trim().replaceAll("\\s+", " ");
        if (termino.isBlank()) {
            termino = null;
        }

        if (termino == null && tag == null) {
            return formatter.formatearUsoBuscar();
        }

        String queryOriginal;
        if (termino != null && tag != null) {
            queryOriginal = termino + " tag:" + tag;
        } else if (termino != null) {
            queryOriginal = termino;
        } else {
            queryOriginal = "tag:" + tag;
        }

        int paginaBaseCero = paginaHumana - 1;
        Pageable pageable = PageRequest.of(
                paginaBaseCero,
                TAMANIO_PAGINA_DEFECTO,
                Sort.by("fechaIngreso").descending());

        try {
            Page<PiezaFisicaResponseDTO> resultado =
                    disponibilizacionService.buscarPiezas(termino, null, null, tag, pageable);
            return formatter.formatearResultados(resultado, queryOriginal, paginaHumana);
        } catch (Exception ex) {
            LOG.error("Error al ejecutar busqueda en Telegram con query '{}': {}", queryOriginal, ex.getMessage(), ex);
            return formatter.formatearError(ex.getMessage());
        }
    }
}
