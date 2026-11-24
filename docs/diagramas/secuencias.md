# Diagramas de Secuencia

Este documento ilustra las interacciones dinámicas y el flujo de llamadas entre capas para los casos de uso principales del sistema **Archivo Digital** (TP Integrador - Diseño de Software UNSAM).

---

## 1. Caso de Uso: Búsqueda Avanzada desde el Bot de Telegram (Patrón Adapter)

Muestra el flujo conversacional donde el usuario busca piezas desde Telegram (`/buscar San Martin tag:HISTORICO`). El bot procesa el mensaje como un adaptador de presentación e invoca a `DisponibilizacionController`:

```mermaid
sequenceDiagram
    autonumber
    actor Usuario as Usuario Telegram
    participant Telegram as Telegram Bot API
    participant BotService as TelegramBotService
    participant Dispatcher as TelegramCommandDispatcher
    participant Controller as DisponibilizacionController
    participant Service as DisponibilizacionService
    participant Spec as PiezaFisicaSpecifications
    participant Repo as PiezaFisicaRepository
    participant Formatter as TelegramMessageFormatter

    Usuario->>Telegram: /buscar San Martin tag:HISTORICO
    Telegram-->>BotService: Long Polling (Update)
    activate BotService
    BotService->>Dispatcher: despachar("/buscar San Martin tag:HISTORICO")
    activate Dispatcher

    Dispatcher->>Dispatcher: parsear comando (termino="San Martin", tag="HISTORICO", p=0)
    Dispatcher->>Controller: buscarPiezas("San Martin", null, null, "HISTORICO", PageRequest(0, 5))
    activate Controller

    Controller->>Service: buscarPiezas("San Martin", null, null, "HISTORICO", pageable)
    activate Service

    Service->>Spec: conFiltros("San Martin", null, null, "HISTORICO")
    activate Spec
    Spec-->>Service: Specification<PiezaFisica> (Criteria Predicates)
    deactivate Spec

    Service->>Repo: findAll(specification, pageable)
    activate Repo
    Repo-->>Service: Page<PiezaFisica>
    deactivate Repo

    Service->>Service: mapearAPiezaFisicaResponseDTO(page)
    Service-->>Controller: Page<PiezaFisicaResponseDTO>
    deactivate Service

    Controller-->>Dispatcher: ResponseEntity.ok(pageDTO)
    deactivate Controller

    Dispatcher->>Formatter: formatearResultadosBusqueda("San Martin", "HISTORICO", pageDTO)
    activate Formatter
    Formatter-->>Dispatcher: String (Texto plano con links y paginación)
    deactivate Formatter

    Dispatcher-->>BotService: Mensaje de respuesta
    deactivate Dispatcher

    BotService->>Telegram: execute(SendMessage)
    deactivate BotService
    Telegram-->>Usuario: Muestra resultados en el chat
```

---

## 2. Caso de Uso: Eliminación Segura de un Tag del Catálogo

Ilustra la administración del catálogo de tags en tiempo de ejecución. Para no violar la clave foránea en la tabla intermedia `digitalizacion_tag`, el servicio desasocia primero las digitalizaciones antes de eliminar el tag:

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador
    participant Controller as CatalogacionController
    participant Service as CatalogacionService
    participant TagRepo as TagRepository
    participant EntidadTag as Tag (Entidad)
    participant EntidadDig as Digitalizacion (Entidad)

    Admin->>Controller: DELETE /api/v1/tags/{id}
    activate Controller
    Controller->>Service: eliminarTag(id)
    activate Service

    Service->>TagRepo: findById(id)
    activate TagRepo
    TagRepo-->>Service: Optional<Tag>
    deactivate TagRepo

    loop Para cada digitalización asociada al tag
        Service->>EntidadDig: desasociarTag(tag)
        activate EntidadDig
        EntidadDig->>EntidadDig: tags.remove(tag)
        EntidadDig->>EntidadTag: digitalizaciones.remove(digitalizacion)
        deactivate EntidadDig
    end

    Service->>TagRepo: delete(tag)
    activate TagRepo
    TagRepo-->>Service: void (DELETE FROM tags WHERE id = ?)
    deactivate TagRepo

    Service-->>Controller: void
    deactivate Service

    Controller-->>Admin: 204 No Content
    deactivate Controller
```

---

## 3. Caso de Uso: Consulta de Pieza Física por ID con Digitalizaciones (Flujo Exitoso)

Muestra la propagación de una solicitud GET desde el cliente HTTP hasta la base de datos y la transformación a DTO enriquecido:

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente HTTP / Swagger
    participant Controller as PiezaFisicaController
    participant Service as PiezaFisicaService
    participant Repo as PiezaFisicaRepository
    participant DB as Base de Datos (JPA)

    Cliente->>Controller: GET /api/v1/piezas-fisicas/{id}
    activate Controller
    Controller->>Service: obtenerPorId(id)
    activate Service
    Service->>Repo: findById(id)
    activate Repo
    Repo->>DB: SELECT * FROM piezas_fisicas WHERE id = ?
    activate DB
    DB-->>Repo: Registro de la pieza
    deactivate DB
    Repo-->>Service: Optional<PiezaFisica>
    deactivate Repo

    Service->>Service: mapearADetalleDTO(piezaFisica)
    Service-->>Controller: PiezaFisicaDetalleResponseDTO
    deactivate Service

    Controller-->>Cliente: 200 OK + PiezaFisicaDetalleResponseDTO (JSON)
    deactivate Controller
```

---

## 4. Caso de Uso: Manejo Global de Excepción (Recurso No Encontrado)

Muestra la captura homogénea mediante `@RestControllerAdvice` (`GlobalExceptionHandler`):

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente HTTP
    participant Controller as PiezaFisicaController
    participant Service as PiezaFisicaService
    participant Handler as GlobalExceptionHandler

    Cliente->>Controller: GET /api/v1/piezas-fisicas/999
    activate Controller
    Controller->>Service: obtenerPorId(999)
    activate Service

    Service-->>Controller: throw PiezaFisicaNoEncontradaException("Pieza física no encontrada con ID: 999")
    deactivate Service

    Controller-->>Handler: Intercepta PiezaFisicaNoEncontradaException
    deactivate Controller
    activate Handler
    Handler->>Handler: armarErrorResponse(status 404, mensaje)
    Handler-->>Cliente: 404 Not Found + ErrorResponse (JSON)
    deactivate Handler
```
