# Backlog de Issues - Archivo Digital

- **Issue #1 (`[SETUP]`):** Configuración de estructura de capas (Patrones de Fowler) y perfiles H2/PostgreSQL.
- **Issue #2 (`[DOCS]`):** Inicialización de la carpeta de documentación `/docs/` y diagramas de arquitectura, clases, DER y secuencias.
- **Issue #3 (`[MODEL]`):** Implementación de entidades de dominio (`PiezaFisica`, `Digitalizacion`, `Tag`, `EstadoPieza`).
- **Issue #4 (`[REPO]`):** Creación de repositorios Spring Data JPA (`PiezaFisicaRepository`, `DigitalizacionRepository`, `TagRepository`) y búsqueda avanzada paginada cruzando `PiezaFisica -> Digitalizacion -> Tag`.
  - **Sub-Issue #13 (`[DTO]`):** Implementación de Request/Response DTOs y Mappers.
- **Issue #5 (`[SERVICE-API]`):** Módulo de Digitalización (alta de piezas físicas y asociación de digitalizaciones).
- **Issue #6 (`[SERVICE-API]`):** Módulo de Catalogación (administración de tags dinámicos y asociación a digitalizaciones).
- **Issue #7 (`[SERVICE-API]`):** Módulo de Disponibilización (búsqueda avanzada paginada de gran volumen sobre piezas físicas).
- **Issue #8 (`[BOT]`):** Integración del Bot de Telegram para visualización y consultas.
- **Issue #9 (`[EXTRA - BATCH]`):** Proceso batch de carga masiva por CSV.
- **Issue #10 (`[EXTRA - 3RD PARTY]`):** Integración con APIs externas de terceros (OCR / Image Labeling).
- **Issue #11 (`[TEST]`):** Pruebas unitarias de servicios con JUnit 5 y Mockito por integrante.
- **Issue #12 (`[DOCS & DEPLOY]`):** Documentación final (`api.md`, `test.md`) y despliegue en Render.
