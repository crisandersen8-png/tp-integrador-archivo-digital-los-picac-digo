# Estrategia y Plan de Pruebas

Este documento describe los lineamientos, estructura y casos de prueba del proyecto **Archivo Digital**, siguiendo las convenciones de testing establecidas para el trabajo práctico.

---

## 1. Estrategia de Testing

El proyecto adopta una pirámide de pruebas centrada en pruebas unitarias rápidas y desacopladas:

1. **Pruebas Unitarias (Domain Model y Services):**
   - Utilizan **JUnit 5** y **Mockito** (`@ExtendWith(MockitoExtension.class)`).
   - **Sin levantar contexto de Spring:** se instancian servicios y entidades manualmente con mocks en repositorios y dependencias externas. Esto asegura máxima velocidad y aislamiento.
   - Las reglas de negocio e invariantes se prueban en tests unitarios de las entidades (`model/`).
   - La orquestación y flujo se prueban en tests unitarios de los servicios (`service/`).
2. **Smoke Tests de Aplicación:**
   - Test mínimo con `@SpringBootTest` (`contextLoads()`) para validar que el contexto de Spring arranque sin errores de configuración de beans o datasources.
3. **Pruebas de Integración Web (Opcionales por feature):**
   - Con `@WebMvcTest` y `MockMvc` para verificar bindings de DTOs, validaciones `@Valid` y códigos de respuesta HTTP en controllers.

---

## 2. Convenciones de Código de Pruebas

- **Lenguaje:** Nombres de clases, métodos, variables y mensajes en **español**, sin tildes ni `ñ` (ver `AGENTS.md`).
- **Estructura Dado-Cuando-Entonces (Given-When-Then):** Cada test debe estar segmentado con comentarios explícitos:
  ```java
  @Test
  void crearRecurso_datosValidos_devuelveRecursoCreado() {
      // Dado
      RecursoRequestDTO request = new RecursoRequestDTO("Nombre", "Descripcion");
      when(repository.save(any(Recurso.class))).thenReturn(new Recurso(1L, "Nombre", "Descripcion"));

      // Cuando
      RecursoResponseDTO respuesta = service.crear(request);

      // Entonces
      assertNotNull(respuesta);
      assertEquals(1L, respuesta.id());
      assertEquals("Nombre", respuesta.nombre());
      verify(repository, times(1)).save(any(Recurso.class));
  }
  ```
- **Nomenclatura de Métodos de Test:**
  `nombreDelMetodo_escenario_resultadoEsperado()`

---

## 3. Matriz de Casos de Prueba (Plantilla)

| ID | Capa / Clase | Caso de Prueba | Precondiciones / Entradas | Resultado Esperado | Estado |
|---|---|---|---|---|---|
| TC-01 | Smoke Test | Arranque del contexto Spring Boot | Perfil `local` activo | Contexto carga sin errores | Aprobado |
| TC-02 | `model.Documento` | Validación de invariante de monto positivo | Monto negativo `-10.0` | Lanza `InvalidAmountException` | Pendiente |
| TC-03 | `model.Documento` | Transición de estado permitida | Estado `BORRADOR` a `PUBLICADO` | Estado cambia a `PUBLICADO` | Pendiente |
| TC-04 | `service.DocumentoService` | Consulta de documento existente | ID válido `1L` | Retorna `DocumentoResponseDTO` | Pendiente |
| TC-05 | `service.DocumentoService` | Consulta de documento inexistente | ID inexistente `999L` | Lanza `ResourceNotFoundException` | Pendiente |
| TC-06 | `controller.DocumentoController` | Manejo de excepciones en API | `ResourceNotFoundException` | Retorna HTTP 404 con JSON de error | Pendiente |

---

## 4. Comandos de Ejecución

### Ejecutar todas las pruebas del proyecto
```bash
./mvnw test
```

### Ejecutar una clase de test específica
```bash
./mvnw test -Dtest=ArchivoDigitalApplicationTests
```

### Ejecutar un método de prueba individual
```bash
./mvnw test -Dtest=ArchivoDigitalApplicationTests#contextLoads
```

### Ejecutar con un perfil específico
```bash
./mvnw test -Dspring.profiles.active=local
```

---

## 5. Integración Continua y Calidad

En cada push/pull request sobre `main`, el pipeline de CI (`.github/workflows/test.yml`) ejecuta:
1. Compilación del código con JDK 21.
2. Ejecución completa de la suite de pruebas unitarias (`mvn test`).
3. Verificaciones de calidad estática de código:
   - Checkstyle (`mvn checkstyle:check`)
   - PMD (`mvn pmd:check`)
   - SpotBugs (`mvn spotbugs:check`)
