# Nombre Equipo

Integrantes

nombre user_git
1
2
3
4

# Forma de uso

```
src/main/java/ar/edu/unsam/ddso/tp/
└── ArchivoDigitalApplication.java          # entry point
src/main/resources/
├── application.yml             # nombre de la app + perfil activo + config base H2
├── application-local.yml       # H2 + JPA para desarrollo local
├── application-postgres.yml    # PostgreSQL + JPA
└── application-prod.yml        # PostgreSQL para producción
src/test/java/ar/edu/unsam/ddso/tp/
└── ArchivoDigitalApplicationTests.java       # smoke test (contextLoads)
```

## Cómo correr los tests

```bash
./mvnw test
```

## Cómo ejecutar la app en local

```bash
./mvnw spring-boot:run
```

Perfil `local` por defecto (H2 in-memory, consola en `http://localhost:8080/h2-console`).
