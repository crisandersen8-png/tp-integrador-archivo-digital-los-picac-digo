# Diagramas de Secuencia

Este documento ilustra las interacciones dinámicas y el flujo de llamadas entre capas para los casos de uso principales del sistema **Archivo Digital**.

---

## 1. Caso de Uso: Consulta de Recurso por ID (Flujo Exitoso)

Muestra la propagación de una solicitud GET desde el cliente HTTP hasta la base de datos y la transformación a DTO.

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente HTTP
    participant Controller as DocumentoController
    participant Service as DocumentoService
    participant Repo as DocumentoRepository
    participant DB as Base de Datos (JPA)

    Cliente->>Controller: GET /api/v1/documentos/{id}
    activate Controller
    Controller->>Service: buscarPorId(id)
    activate Service
    Service->>Repo: findById(id)
    activate Repo
    Repo->>DB: SELECT * FROM documentos WHERE id = ?
    activate DB
    DB-->>Repo: Registro del documento
    deactivate DB
    Repo-->>Service: Optional<Documento>
    deactivate Repo

    Service->>Service: mapearADTO(documento)
    Service-->>Controller: DocumentoResponseDTO
    deactivate Service

    Controller-->>Cliente: 200 OK + DocumentoResponseDTO (JSON)
    deactivate Controller
```

---

## 2. Caso de Uso: Transición de Estado con Validación de Invariante de Dominio

Ilustra la invocación de un método de negocio sobre la entidad rica, asegurando que las reglas del negocio vivan en el modelo y no en el servicio.

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Operador / Cliente HTTP
    participant Controller as DocumentoController
    participant Service as DocumentoService
    participant Repo as DocumentoRepository
    participant Entidad as Documento (Entidad)
    participant DB as Base de Datos

    Cliente->>Controller: POST /api/v1/documentos/{id}/publicar
    activate Controller
    Controller->>Service: publicarDocumento(id)
    activate Service

    Service->>Repo: findById(id)
    activate Repo
    Repo-->>Service: Optional<Documento>
    deactivate Repo

    Note over Service,Entidad: El service delega la regla de negocio a la entidad
    Service->>Entidad: publicar()
    activate Entidad

    alt Documento en estado inválido (ej. ARCHIVADO)
        Entidad-->>Service: throw InvalidStateException("No se puede publicar un documento archivado")
        Service-->>Controller: Propaga InvalidStateException
        Controller-->>Cliente: 409 Conflict + ErrorResponse (JSON)
    else Estado válido (ej. BORRADOR)
        Entidad->>Entidad: setEstado(PUBLICADO)
        Entidad-->>Service: void
        deactivate Entidad

        Service->>Repo: save(documento)
        activate Repo
        Repo->>DB: UPDATE documentos SET estado = 'PUBLICADO' WHERE id = ?
        Repo-->>Service: Documento actualizado
        deactivate Repo

        Service->>Service: mapearADTO(documento)
        Service-->>Controller: DocumentoResponseDTO
        Controller-->>Cliente: 200 OK + DocumentoResponseDTO
    end
    deactivate Service
    deactivate Controller
```

---

## 3. Caso de Uso: Manejo de Excepción (Recurso No Encontrado)

Muestra la captura homogénea mediante `@RestControllerAdvice` (`GlobalExceptionHandler`).

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente HTTP
    participant Controller as DocumentoController
    participant Service as DocumentoService
    participant Handler as GlobalExceptionHandler

    Cliente->>Controller: GET /api/v1/documentos/999
    activate Controller
    Controller->>Service: buscarPorId(999)
    activate Service

    Service-->>Controller: throw DocumentoNotFoundException("Documento 999 no encontrado")
    deactivate Service

    Controller-->>Handler: Intercepta DocumentoNotFoundException
    deactivate Controller
    activate Handler
    Handler->>Handler: construirErrorResponse(status 404, mensaje)
    Handler-->>Cliente: 404 Not Found + Error JSON
    deactivate Handler
```
