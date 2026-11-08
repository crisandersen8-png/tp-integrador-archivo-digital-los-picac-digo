# Especificación de la API REST - Archivo Digital

Este documento detalla los endpoints, contratos de intercambio (DTOs) y convenciones de la API REST del sistema **Archivo Digital** (TP Integrador - Diseño de Software UNSAM).

La API constituye el núcleo de servicios del backend monolítico en Spring Boot, dando soporte a los módulos de **Catalogación**, **Digitalización** y **Disponibilización**, consumidos por operadores de la fundación y por el **Bot de Telegram** para la consulta del acervo cultural.

---

## 1. Convenciones Generales

- **URL Base:** `/api/v1`
- **Formato de intercambio:** `application/json; charset=utf-8` en peticiones y respuestas.
- **Desacoplamiento con DTOs:** La capa `controller` utiliza exclusivamente `record`s de Java para requests y responses. No se exponen entidades JPA (`@Entity`) directamente para prevenir bucles de serialización y desacoplar la persistencia del contrato público.
- **Manejo Centralizado de Excepciones:** Gestionado por `@RestControllerAdvice` (`GlobalExceptionHandler`), asegurando respuestas homogéneas de error.

---

## 2. Entornos y Consumidores

| Entorno | URL Base | Descripción |
|---|---|---|
| **Local** | `http://localhost:8080/api/v1` | Entorno de desarrollo con base de datos H2 en memoria. |
| **Producción** | `http://<host>:<puerto>/api/v1` | Entorno productivo con PostgreSQL. |

**Clientes de la API:**
- **Administrador / Archivista:** Altas, modificaciones y bajas de piezas físicas, carga de recursos digitales y administración de tags.
- **Bot de Telegram:** Servicio consumidor que realiza búsquedas de recursos digitales y consulta información de las piezas físicas para usuarios finales.

---

## 3. Códigos de Estado HTTP

| Código | Estado | Significado en este dominio |
|---|---|---|
| `200` | OK | Consulta o actualización exitosa de piezas, digitalizaciones o tags. |
| `201` | Created | Registro exitoso de una pieza física, digitalización o tag. |
| `204` | No Content | Eliminación exitosa (o desasociación de tag) sin contenido de retorno. |
| `400` | Bad Request | Falla en validaciones de entrada (`@Valid`), datos malformados o parámetros inválidos. |
| `404` | Not Found | Pieza física, digitalización o tag no encontrado por su identificador. |
| `409` | Conflict | Violación de restricciones únicas (ej. `codigoInventario` o nombre de `Tag` duplicado). |
| `500` | Internal Server Error | Excepción inesperada del servidor. |

---

## 4. Estructura Estándar de Error

Todas las respuestas de error retornan un payload con la siguiente estructura:

```json
{
  "timestamp": "2026-11-07T21:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Pieza física con ID 10 no encontrada",
  "path": "/api/v1/piezas-fisicas/10"
}
```

---

## 5. Resumen de Endpoints

### 5.1. Módulo de Piezas Físicas (Catalogación)
| Método | Endpoint | Descripción | Request DTO | Response DTO | Código |
|---|---|---|---|---|---|
| `POST` | `/api/v1/piezas-fisicas` | Registrar nueva pieza física | `PiezaFisicaCreateRequestDTO` | `PiezaFisicaResponseDTO` | 201 |
| `GET` | `/api/v1/piezas-fisicas` | Listar piezas físicas (con filtros opcionales) | - | `List<PiezaFisicaResponseDTO>` | 200 |
| `GET` | `/api/v1/piezas-fisicas/{id}` | Obtener detalle de pieza con sus digitalizaciones | - | `PiezaFisicaDetalleResponseDTO` | 200 |
| `PUT` | `/api/v1/piezas-fisicas/{id}` | Actualizar datos de una pieza física | `PiezaFisicaUpdateRequestDTO` | `PiezaFisicaResponseDTO` | 200 |
| `DELETE` | `/api/v1/piezas-fisicas/{id}` | Eliminar una pieza física y sus digitalizaciones | - | - | 204 |

### 5.2. Módulo de Digitalizaciones
| Método | Endpoint | Descripción | Request DTO | Response DTO | Código |
|---|---|---|---|---|---|
| `POST` | `/api/v1/piezas-fisicas/{piezaId}/digitalizaciones` | Cargar recurso digitalizado a una pieza | `DigitalizacionCreateRequestDTO` | `DigitalizacionResponseDTO` | 201 |
| `GET` | `/api/v1/digitalizaciones/{id}` | Obtener detalle de una digitalización y sus tags | - | `DigitalizacionResponseDTO` | 200 |
| `POST` | `/api/v1/digitalizaciones/{id}/tags` | Asociar etiqueta/tag a una digitalización | `AsociarTagRequestDTO` | `DigitalizacionResponseDTO` | 200 |
| `DELETE` | `/api/v1/digitalizaciones/{id}/tags/{tagId}` | Desvincular tag de una digitalización | - | `DigitalizacionResponseDTO` | 200 |
| `DELETE` | `/api/v1/digitalizaciones/{id}` | Eliminar una digitalización | - | - | 204 |

### 5.3. Módulo de Tags (Etiquetas Dinámicas)
| Método | Endpoint | Descripción | Request DTO | Response DTO | Código |
|---|---|---|---|---|---|
| `POST` | `/api/v1/tags` | Crear nuevo tag en tiempo de ejecución | `TagCreateRequestDTO` | `TagResponseDTO` | 201 |
| `GET` | `/api/v1/tags` | Listar todos los tags del sistema | - | `List<TagResponseDTO>` | 200 |
| `DELETE` | `/api/v1/tags/{id}` | Eliminar un tag | - | - | 204 |

### 5.4. Módulo de Disponibilización (Búsquedas para Bot de Telegram y Clientes)
| Método | Endpoint | Descripción | Parámetros Query | Response DTO | Código |
|---|---|---|---|---|---|
| `GET` | `/api/v1/digitalizaciones` | Buscar digitalizaciones por tag | `?tag=FOTOGRAFIA` | `List<DigitalizacionResponseDTO>` | 200 |
| `GET` | `/api/v1/piezas-fisicas/buscar` | Buscar piezas por nombre o código | `?q=acta` | `List<PiezaFisicaResponseDTO>` | 200 |

---

## 6. Detalle de Contratos y Ejemplos de Intercambio

### 6.1. Registrar Pieza Física

- **Endpoint:** `POST /api/v1/piezas-fisicas`
- **Descripción:** Da de alta un bien cultural en el acervo patrimonial.

#### Request Body (`PiezaFisicaCreateRequestDTO`):
```json
{
  "codigoInventario": "INV-2026-0042",
  "nombre": "Acta Fundacional del Instituto",
  "descripcion": "Manuscrito en papel de lino con firmas de miembros fundadores",
  "fechaIngreso": "2026-03-15",
  "tipoMaterial": "Documento Papel"
}
```

#### Response Body (`201 Created` - `PiezaFisicaResponseDTO`):
```json
{
  "id": 1,
  "codigoInventario": "INV-2026-0042",
  "nombre": "Acta Fundacional del Instituto",
  "descripcion": "Manuscrito en papel de lino con firmas de miembros fundadores",
  "fechaIngreso": "2026-03-15",
  "tipoMaterial": "Documento Papel",
  "cantidadDigitalizaciones": 0
}
```

---

### 6.2. Consultar Pieza Física con Digitalizaciones (Detalle Completo)

- **Endpoint:** `GET /api/v1/piezas-fisicas/1`
- **Descripción:** Retorna la información de la pieza física junto al listado de sus digitalizaciones asociadas.

#### Response Body (`200 OK` - `PiezaFisicaDetalleResponseDTO`):
```json
{
  "id": 1,
  "codigoInventario": "INV-2026-0042",
  "nombre": "Acta Fundacional del Instituto",
  "descripcion": "Manuscrito en papel de lino con firmas de miembros fundadores",
  "fechaIngreso": "2026-03-15",
  "tipoMaterial": "Documento Papel",
  "digitalizaciones": [
    {
      "id": 10,
      "urlRecurso": "https://storage.archivo-digital.org/fondos/inv-0042-scan-hi.pdf",
      "descripcion": "Escaneo color alta resolución 600 DPI",
      "fechaDigitalizacion": "2026-04-01T14:30:00",
      "tags": ["MANUSCRITO", "SIGLO_XX", "ACTA"]
    }
  ]
}
```

---

### 6.3. Cargar Digitalización a una Pieza Física

- **Endpoint:** `POST /api/v1/piezas-fisicas/1/digitalizaciones`
- **Descripción:** Vincula un nuevo archivo digital a la pieza física indicada en la ruta.

#### Request Body (`DigitalizacionCreateRequestDTO`):
```json
{
  "urlRecurso": "https://storage.archivo-digital.org/fondos/inv-0042-fotografia.jpg",
  "descripcion": "Toma fotográfica de dorso del documento",
  "fechaDigitalizacion": "2026-04-02T10:15:00",
  "tags": ["FOTOGRAFIA", "DORSO"]
}
```

#### Response Body (`201 Created` - `DigitalizacionResponseDTO`):
```json
{
  "id": 11,
  "piezaFisicaId": 1,
  "urlRecurso": "https://storage.archivo-digital.org/fondos/inv-0042-fotografia.jpg",
  "descripcion": "Toma fotográfica de dorso del documento",
  "fechaDigitalizacion": "2026-04-02T10:15:00",
  "tags": [
    { "id": 4, "nombre": "FOTOGRAFIA" },
    { "id": 5, "nombre": "DORSO" }
  ]
}
```

---

### 6.4. Crear Tag Administrable

- **Endpoint:** `POST /api/v1/tags`
- **Descripción:** Crea una nueva etiqueta para catalogación dinámica en tiempo de ejecución.

#### Request Body (`TagCreateRequestDTO`):
```json
{
  "nombre": "RESTAURADO"
}
```

#### Response Body (`201 Created` - `TagResponseDTO`):
```json
{
  "id": 7,
  "nombre": "RESTAURADO"
}
```

---

### 6.5. Asociar Tag a una Digitalización

- **Endpoint:** `POST /api/v1/digitalizaciones/11/tags`
- **Descripción:** Asocia un tag existente a la digitalización especificada.

#### Request Body (`AsociarTagRequestDTO`):
```json
{
  "tagId": 7
}
```

#### Response Body (`200 OK` - `DigitalizacionResponseDTO`):
```json
{
  "id": 11,
  "piezaFisicaId": 1,
  "urlRecurso": "https://storage.archivo-digital.org/fondos/inv-0042-fotografia.jpg",
  "descripcion": "Toma fotográfica de dorso del documento",
  "fechaDigitalizacion": "2026-04-02T10:15:00",
  "tags": [
    { "id": 4, "nombre": "FOTOGRAFIA" },
    { "id": 5, "nombre": "DORSO" },
    { "id": 7, "nombre": "RESTAURADO" }
  ]
}
```

---

### 6.6. Búsqueda de Digitalizaciones por Tag (Consumo Bot de Telegram)

- **Endpoint:** `GET /api/v1/digitalizaciones?tag=FOTOGRAFIA`
- **Descripción:** Utilizado por el bot de Telegram para disponibilizar recursos digitales categorizados con una etiqueta específica.

#### Response Body (`200 OK` - `List<DigitalizacionResponseDTO>`):
```json
[
  {
    "id": 11,
    "piezaFisicaId": 1,
    "urlRecurso": "https://storage.archivo-digital.org/fondos/inv-0042-fotografia.jpg",
    "descripcion": "Toma fotográfica de dorso del documento",
    "fechaDigitalizacion": "2026-04-02T10:15:00",
    "tags": [
      { "id": 4, "nombre": "FOTOGRAFIA" },
      { "id": 5, "nombre": "DORSO" },
      { "id": 7, "nombre": "RESTAURADO" }
    ]
  }
]
```
