# Diagrama de Clases de Dominio

Este documento contiene el **Diagrama de Clases de Dominio** para el sistema **Archivo Digital** (TP Integrador - Diseño de Software UNSAM).

---

## 1. Modelo de Dominio

El dominio del sistema se compone de tres entidades principales que modelan el acervo cultural, sus archivos digitales y su catalogación mediante etiquetas dinámicas:

```mermaid
classDiagram
    class PiezaFisica {
        -Long id
        -String codigoInventario
        -String nombre
        -String descripcion
        -LocalDate fechaIngreso
        -String tipoMaterial
        -List~Digitalizacion~ digitalizaciones
        +agregarDigitalizacion(String url, String descripcion) void
    }

    class Digitalizacion {
        -Long id
        -String urlRecurso
        -String descripcion
        -LocalDateTime fechaDigitalizacion
        -PiezaFisica piezaFisica
        -Set~Tag~ tags
        +asociarTag(Tag tag) void
    }

    class Tag {
        -Long id
        -String nombre
        -Set~Digitalizacion~ digitalizaciones
    }

    PiezaFisica "1" *-- "0..*" Digitalizacion : contiene
    Digitalizacion "*" o-- "*" Tag : etiquetada con