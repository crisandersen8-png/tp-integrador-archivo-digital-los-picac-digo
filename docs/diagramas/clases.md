# Diagramas de Clases

Este documento contiene los **Diagramas de Clases** para el sistema **Archivo Digital** (TP Integrador - Diseño de Software UNSAM), cubriendo tanto el Modelo de Dominio como la Capa de Presentación del Bot de Telegram (Patrón Adapter).

---

## 1. Modelo de Dominio (Entidades de Negocio)

El dominio del sistema se compone de tres entidades principales y un enum que modelan el acervo cultural, sus archivos digitales y su catalogación mediante etiquetas dinámicas. Las entidades implementan un **Modelo de Dominio Rico**, protegiendo sus propias invariantes mediante validaciones y métodos de sincronización bidireccional.

```mermaid
classDiagram
    class EstadoPieza {
        <<enumeration>>
        EN_DEPOSITO
        EN_EXHIBICION
        EN_PRESTAMO
        EN_RESTAURACION
    }

    class PiezaFisica {
        -Long id
        -String codigoInventario
        -String nombre
        -String descripcion
        -LocalDate fechaIngreso
        -String tipoMaterial
        -EstadoPieza estado
        -List~Digitalizacion~ digitalizaciones
        +validar() void
        +agregarDigitalizacion(Digitalizacion digitalizacion) void
        +quitarDigitalizacion(Digitalizacion digitalizacion) void
    }

    class Digitalizacion {
        -Long id
        -String urlRecurso
        -String descripcion
        -LocalDateTime fechaDigitalizacion
        -PiezaFisica piezaFisica
        -Set~Tag~ tags
        +validar() void
        +asociarTag(Tag tag) void
        +desasociarTag(Tag tag) void
    }

    class Tag {
        -Long id
        -String nombre
        -Set~Digitalizacion~ digitalizaciones
        +validar() void
    }

    PiezaFisica --> EstadoPieza : tiene estado
    PiezaFisica "1" *-- "0..*" Digitalizacion : contiene (CascadeType.ALL, orphanRemoval)
    Digitalizacion "*" o-- "*" Tag : etiquetada con (@ManyToMany)
```

---

## 2. Integración del Bot de Telegram (Patrón Adapter)

Para desacoplar el canal de mensajería del núcleo del negocio e intercambiar en el futuro Telegram por WhatsApp o interfaces web sin impacto, el módulo de Telegram se implementa como un **Adaptador de Presentación** que invoca directamente a la frontera del sistema (`DisponibilizacionController`), respetando la jerarquía de capas de Martin Fowler:

```mermaid
classDiagram
    class SpringLongPollingBot {
        <<interface>>
    }

    class LongPollingSingleThreadUpdateConsumer {
        <<interface>>
        +consume(Update update) void
    }

    class TelegramBotConfig {
        -boolean enabled
        -String token
        -String username
    }

    class TelegramBotService {
        -TelegramBotConfig config
        -TelegramCommandDispatcher dispatcher
        -TelegramClient telegramClient
        +consume(Update update) void
    }

    class TelegramCommandDispatcher {
        -DisponibilizacionController disponibilizacionController
        -TelegramMessageFormatter formatter
        -String botUsername
        +despachar(String textoComando) String
    }

    class TelegramMessageFormatter {
        +formatearResultadosBusqueda(String termino, String tag, Page~PiezaFisicaResponseDTO~ pagina) String
        +formatearBienvenida(String botUsername) String
        +formatearAyuda() String
        +formatearComandoDesconocido() String
        +formatearErrorGenerico() String
    }

    class DisponibilizacionController {
        -DisponibilizacionService disponibilizacionService
        +buscarPiezas(String termino, LocalDate fechaDesde, LocalDate fechaHasta, String tag, Pageable pageable) ResponseEntity~Page~
    }

    class DisponibilizacionService {
        -PiezaFisicaRepository piezaFisicaRepository
        +buscarPiezas(String termino, LocalDate fechaDesde, LocalDate fechaHasta, String tag, Pageable pageable) Page~PiezaFisicaResponseDTO~
    }

    SpringLongPollingBot <|.. TelegramBotService
    LongPollingSingleThreadUpdateConsumer <|.. TelegramBotService
    TelegramBotService --> TelegramCommandDispatcher : delega comando
    TelegramBotService --> TelegramBotConfig : configuracion
    TelegramCommandDispatcher --> TelegramMessageFormatter : formatea respuestas
    TelegramCommandDispatcher --> DisponibilizacionController : invoca busqueda (Adapter)
    DisponibilizacionController --> DisponibilizacionService : logica de negocio
```
