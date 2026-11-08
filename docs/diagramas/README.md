# Diagramas del Sistema - Archivo Digital

Este directorio centraliza los diagramas técnicos y modelos arquitecturales exigidos por la cátedra para el proyecto.

## Índice de Diagramas

1. [**Arquitectura (`arquitectura.md`)**](arquitectura.md):
   - Estilo de arquitectura por capas (Controller, DTO, Service, Model, Repository).
   - Diagrama C4 Nivel 1: Contexto del Sistema.
   - Diagrama C4 Nivel 2: Contenedores.
2. [**Clases (`clases.md`)**](clases.md):
   - Diagrama de clases del modelo de dominio.
   - Entidades JPA, enums, invariantes y métodos de negocio.
3. [**DER - Diagrama Entidad-Relación (`der.md`)**](der.md):
   - Modelo relacional de base de datos.
   - Tablas, columnas, PKs, FKs, tipos de datos y relaciones de cardinalidad.
4. [**Secuencias (`secuencias.md`)**](secuencias.md):
   - Flujos de interacción entre actores, controladores, servicios, modelos y base de datos.
   - Casos de uso de consulta, mutación de negocio y manejo de errores.

---

## Formato y Herramientas

- Los diagramas están redactados en **Mermaid** directamente embebidos en Markdown, lo que permite renderizarlos en GitHub, IDEs compatibles y herramientas de documentación sin requerir archivos binarios externos.
- Si se utilizan herramientas externas (ej. *Draw.io*, *PlantUML*, *Lucidchart*), las imágenes exportadas (PNG/SVG) pueden alojarse en esta carpeta o subcarpeta de recursos y ser referenciadas desde los archivos markdown correspondientes.
