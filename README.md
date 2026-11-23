# Los Picacódigo — Archivo Digital

Trabajo Práctico Integrador de **Diseño de Software (UNSAM)**: sistema backend monolítico en **Java 21 + Spring Boot** para la gestión del acervo cultural de una fundación (piezas físicas, digitalizaciones, catalogación con tags dinámicos y búsquedas paginadas por API REST y Bot de Telegram).

## Integrantes

| # | Nombre | Usuario GitHub |
|---|---|---|
| 1 | Cristian Andersen | `@crisandersen8-png` |
| 2 | Kalil | `@yasinkalil10` |
| 3 | Matías | `@Matykyng` |
---

## Estructura del proyecto

```text
src/main/java/ar/edu/unsam/ddso/tp/
├── ArchivoDigitalApplication.java     # Entry point Spring Boot
├── controller/                        # Endpoints REST (/api/v1)
├── dto/                               # Records inmutables de request/response y PaginaResponseDTO
├── service/                           # Lógica transaccional y ArchivoDigitalMapper
├── model/                             # Entidades JPA (@Entity), enums e invariantes de dominio
├── repository/                        # Spring Data JPA + PiezaFisicaSpecifications
├── exception/                         # Excepciones de aplicación/HTTP y GlobalExceptionHandler
└── bot/                               # Módulo de Bot de Telegram (Issue #8)

src/main/resources/
├── application.yml                    # Configuración base (perfil activo local + props Telegram)
├── application-local.yml              # H2 in-memory (create-drop) + consola H2 habilitada
├── application-postgres.yml           # PostgreSQL + JPA (update)
└── application-prod.yml               # PostgreSQL para entorno productivo

src/test/java/ar/edu/unsam/ddso/tp/
├── ArchivoDigitalApplicationTests.java          # Smoke test (contextLoads)
├── model/                                       # Tests unitarios de reglas de negocio
├── repository/                                  # Tests de integración JPA (@DataJpaTest)
├── service/                                     # Tests unitarios con JUnit 5 + Mockito
├── controller/ArchivoDigitalIntegracionTest.java # Tests E2E con MockMvc + H2
└── bot/                                         # Tests unitarios del Bot de Telegram (Mockito)
```

---

## Forma de uso

### Requisitos previos
- **Java 21** instalado (`java -version`).
- Maven Wrapper incluido en el repositorio (`./mvnw` en Linux/macOS o `.\mvnw.cmd` en Windows).

### Cómo correr los tests y controles de calidad

```bash
# Ejecutar toda la suite de tests (unitarios + integración)
./mvnw test

# Ejecutar análisis estático (Checkstyle, PMD y SpotBugs)
./mvnw checkstyle:check pmd:check spotbugs:check
```

*(En Windows PowerShell, reemplazar `./mvnw` por `.\mvnw.cmd`.)*

### Cómo ejecutar la app en local (H2 en memoria)

```bash
./mvnw spring-boot:run
```

- **Perfil por defecto:** `local` (base H2 en memoria con `ddl-auto: create-drop`).
- **API REST base:** `http://localhost:8080/api/v1`
- **Consola H2:** `http://localhost:8080/h2-console` (`JDBC URL`: `jdbc:h2:mem:tp`, usuario: `sa`, contraseña vacía).
- **Swagger UI** *(con Issue #8 integrado)*: `http://localhost:8080/swagger-ui.html`

### Cómo ejecutar con PostgreSQL

```bash
# Configurar variables de entorno (valores por defecto: localhost:5432/archivo_digital, postgres/postgres)
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Variables soportadas: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

### Cómo activar el Bot de Telegram (Issue #8)

El bot viene desactivado por defecto (`telegram.bot.enabled=false`) para no requerir credenciales al desarrollar o correr tests. Para activarlo:

```powershell
# Ejemplo en PowerShell
$env:TELEGRAM_BOT_ENABLED="true"
$env:TELEGRAM_BOT_TOKEN="TU_TOKEN_DE_BOTFATHER"
$env:TELEGRAM_BOT_USERNAME="tu_bot_username"
.\mvnw.cmd spring-boot:run
```

Comandos disponibles en Telegram: `/start`, `/ayuda`, `/buscar <termino>`, `/buscar tag:<etiqueta>`, `/buscar <termino> tag:<etiqueta> p:<numero>`.

---

## Endpoints principales de la API REST

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/piezas-fisicas` | Registrar una nueva pieza física |
| `GET` | `/api/v1/piezas-fisicas/{id}` | Obtener detalle de una pieza con sus digitalizaciones y tags |
| `POST` | `/api/v1/piezas-fisicas/{id}/digitalizaciones` | Asociar una digitalización (URL HTTP/HTTPS) a una pieza |
| `GET` | `/api/v1/tags` | Listar todos los tags dinámicos |
| `POST` | `/api/v1/tags` | Crear un nuevo tag en tiempo de ejecución |
| `DELETE` | `/api/v1/tags/{id}` | Eliminar un tag desvinculándolo de sus digitalizaciones |
| `POST` | `/api/v1/digitalizaciones/{id}/tags` | Asociar un tag existente a una digitalización |
| `GET` | `/api/v1/piezas-fisicas/buscar` | Búsqueda avanzada paginada (`termino`, `fechaDesde`, `fechaHasta`, `tag`, `page`, `size`, `sort`) |

---

## Documentación del proyecto

- **Arquitectura y diagramas C4:** [`ARCHITECTURE.md`](ARCHITECTURE.md)
- **Convenciones de código y capas:** [`AGENTS.md`](AGENTS.md)
- **Backlog de Issues:** [`Backlog.md`](Backlog.md)
- **Especificación completa de la API REST:** [`docs/api.md`](docs/api.md)
- **Estrategia y plan de pruebas:** [`docs/test.md`](docs/test.md)
- **Diagramas de arquitectura, clases, DER y secuencias:** [`docs/diagramas/`](docs/diagramas/README.md)
