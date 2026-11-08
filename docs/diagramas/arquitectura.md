Diagramas de Arquitectura

Este documento describe la vista de arquitectura del sistema Archivo Digital (TP Integrador - Diseño de Software UNSAM), reflejando una solución basada en una arquitectura monolítica en capas con Spring Boot, servicios REST de backend y un bot de Telegram como interfaz de visualización.

1. Vista de Capas del Sistema (Patrones de Fowler)

El backend sigue una arquitectura limpia en capas desacopladas dentro del monolito:

flowchart TD
    Client["Cliente HTTP / Postman / Bot de Telegram"] --> Controller["Capa Controller (@RestController)"]
    Controller --> DTO["Capa DTO (Request/Response Records)"]
    Controller --> Service["Capa Service (Lógica Transaccional de Negocio)"]
    Service --> Model["Capa Model (@Entity - Entidades de Dominio)"]
    Service --> Repository["Capa Repository (Spring Data JPA)"]
    Repository --> DB[("Base de Datos - H2 in-memory / PostgreSQL")]


2. Vista C4 - Nivel 1: Contexto del Sistema

Describe las interacciones entre los actores y el sistema monolítico de Archivo Digital.

flowchart TD
    subgraph Actores["Actores"]
        Usuario["Usuario Consultor / Bot de Telegram"]
        Admin["Administrador del Sistema<br/>(Gestiona Tags y Piezas)"]
    end

    subgraph Frontera["Frontera del Sistema"]
        Sistema["Sistema Archivo Digital<br/>[Monolito Spring Boot]<br/>Gestiona piezas, digitalizaciones, tags y búsquedas."]
    end

    subgraph Externos["Sistemas Externos"]
        TelegramAPI["Telegram Bot API"]
    end

    Usuario -->|Busca contenido| Sistema
    Admin -->|Gestiona piezas y tags| Sistema
    Sistema -->|Envía y recibe mensajes| TelegramAPI


3. Vista C4 - Nivel 2: Contenedores

Detalla los componentes ejecutables dentro del monolito.

flowchart LR
    Usuario["Usuario / Bot"] -->|HTTP / Webhook| Monolith["Aplicación Monolítica Spring Boot<br/>[Java 21 / Spring Boot]<br/>- APIs REST de Digitalización, Catalogación y Disponibilización<br/>- Servicio de Bot de Telegram"]
    Monolith -->|JPA / JDBC| DB[("Base de Datos Relacional<br/>[H2 / PostgreSQL]")]


4. Decisiones Arquitectónicas Principales

Monolito Único: Se optó por una arquitectura monolítica unificada (sin microservicios ni frontend web complejo) para cumplir de forma eficiente con el alcance acotado del TP.

Desacoplamiento mediante DTOs: Los controladores utilizan DTOs (records) para evitar exponer directamente las entidades de base de datos (@Entity), previniendo problemas de serialización JSON y bucles infinitos.

Flexibilidad en Base de Datos: Uso de H2 en memoria para desarrollo ágil y pruebas locales, con soporte transparente para PostgreSQL mediante JPA/Hibernate.