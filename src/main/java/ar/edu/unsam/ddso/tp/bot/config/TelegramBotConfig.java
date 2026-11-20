package ar.edu.unsam.ddso.tp.bot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuracion de credenciales y propiedades del bot de Telegram. */
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
@Getter
@Setter
public class TelegramBotConfig {

    private boolean enabled;
    private String token = "";
    private String username = "archivo_digital_bot";
}
