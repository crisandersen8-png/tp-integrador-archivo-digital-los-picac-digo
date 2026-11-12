package ar.edu.unsam.ddso.tp.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.unsam.ddso.tp.model.Digitalizacion;
import ar.edu.unsam.ddso.tp.model.PiezaFisica;
import ar.edu.unsam.ddso.tp.model.Tag;
import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import ar.edu.unsam.ddso.tp.repository.DigitalizacionRepository;
import ar.edu.unsam.ddso.tp.repository.PiezaFisicaRepository;
import ar.edu.unsam.ddso.tp.repository.TagRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de integracion de punta a punta para los Issues #1 al #7 (MockMvc + Spring Boot + H2).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ArchivoDigitalIntegracionTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private PiezaFisicaRepository piezaFisicaRepository;

    @Autowired private DigitalizacionRepository digitalizacionRepository;

    @Autowired private TagRepository tagRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        digitalizacionRepository.deleteAll();
        piezaFisicaRepository.deleteAll();
        tagRepository.deleteAll();
    }

    // ---------- 1. Alta y consulta de piezas fisicas ----------

    @Test
    void crearPiezaConTodosLosDatosDevuelve201YPersisteEnH2() throws Exception {
        // Dado
        String json =
                """
                {
                  "codigoInventario": "INV-2026-0001",
                  "nombre": "Acta Fundacional del Instituto",
                  "descripcion": "Manuscrito en papel de lino con firmas originales",
                  "fechaIngreso": "2026-03-15",
                  "tipoMaterial": "Documento Papel",
                  "estado": "EN_EXHIBICION"
                }
                """;

        // Cuando / Entonces
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.codigoInventario", is("INV-2026-0001")))
                .andExpect(jsonPath("$.nombre", is("Acta Fundacional del Instituto")))
                .andExpect(
                        jsonPath(
                                "$.descripcion",
                                is("Manuscrito en papel de lino con firmas originales")))
                .andExpect(jsonPath("$.fechaIngreso", is("2026-03-15")))
                .andExpect(jsonPath("$.tipoMaterial", is("Documento Papel")))
                .andExpect(jsonPath("$.estado", is("EN_EXHIBICION")))
                .andExpect(jsonPath("$.cantidadDigitalizaciones", is(0)));

        assertEquals(1, piezaFisicaRepository.count());
        assertTrue(piezaFisicaRepository.existsByCodigoInventarioIgnoreCase("INV-2026-0001"));
    }

    @Test
    void crearPiezaSinEstadoNiDescripcionAsignaEstadoEnDepositoPorDefecto() throws Exception {
        // Dado
        String json =
                """
                {
                  "codigoInventario": "INV-2026-0002",
                  "nombre": "Retrato Fotografico",
                  "fechaIngreso": "2026-04-01",
                  "tipoMaterial": "Fotografia"
                }
                """;

        // Cuando / Entonces
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoInventario", is("INV-2026-0002")))
                .andExpect(jsonPath("$.estado", is("EN_DEPOSITO")))
                .andExpect(jsonPath("$.cantidadDigitalizaciones", is(0)));
    }

    @Test
    void obtenerPiezaPorIdExistenteDevuelve200ConDetalleCompleto() throws Exception {
        // Dado
        PiezaFisica pieza =
                guardarPieza("INV-2026-0010", "Carta Historica", "Carta manuscrita", "2026-02-10");

        // Cuando / Entonces
        mockMvc.perform(get("/api/v1/piezas-fisicas/{id}", pieza.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pieza.getId().intValue())))
                .andExpect(jsonPath("$.codigoInventario", is("INV-2026-0010")))
                .andExpect(jsonPath("$.nombre", is("Carta Historica")))
                .andExpect(jsonPath("$.digitalizaciones", hasSize(0)));
    }

    @Test
    void obtenerPiezaPorIdInexistenteDevuelve404ConEstructuraEstandarDeError() throws Exception {
        // Dado / Cuando / Entonces
        mockMvc.perform(get("/api/v1/piezas-fisicas/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Pieza fisica con ID 9999 no encontrada")));
    }

    @Test
    void crearPiezaConCodigoInventarioDuplicadoExactoDevuelve409() throws Exception {
        // Dado
        guardarPieza("INV-2026-0050", "Pieza Original", "Descripcion 1", "2026-01-15");
        String jsonDuplicado =
                """
                {
                  "codigoInventario": "INV-2026-0050",
                  "nombre": "Otra Pieza",
                  "fechaIngreso": "2026-03-20",
                  "tipoMaterial": "Documento Papel"
                }
                """;

        // Cuando / Entonces
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonDuplicado))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", notNullValue()));

        assertEquals(1, piezaFisicaRepository.count());
    }

    @Test
    void crearPiezaConCodigoInventarioDuplicadoEnMinusculasOConEspaciosDeberiaRechazarseCon409()
            throws Exception {
        // Dado: caso limite de normalizacion/unicidad de codigoInventario
        guardarPieza("INV-2026-0051", "Pieza Original", "Descripcion", "2026-01-15");
        String jsonConEspaciosYMinusculas =
                """
                {
                  "codigoInventario": " inv-2026-0051 ",
                  "nombre": "Pieza Duplicada con Espacios",
                  "fechaIngreso": "2026-03-20",
                  "tipoMaterial": "Documento Papel"
                }
                """;

        // Cuando / Entonces
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonConEspaciosYMinusculas))
                .andExpect(status().isConflict());
    }

    // ---------- 2. Alta y asociacion de digitalizaciones ----------

    @Test
    void agregarDigitalizacionAPiezaExistenteDevuelve201YPersisteRelacionSinDuplicar()
            throws Exception {
        // Dado
        PiezaFisica pieza =
                guardarPieza("INV-2026-0100", "Mapa Antiguo", "Mapa cartografico", "2026-03-01");
        String jsonDigitalizacion =
                """
                {
                  "urlRecurso": "https://storage.archivo-digital.org/fondos/mapa-0100.jpg",
                  "descripcion": "Escaneo de alta resolucion 600 DPI"
                }
                """;

        // Cuando / Entonces
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas/{id}/digitalizaciones", pieza.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonDigitalizacion))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.piezaFisicaId", is(pieza.getId().intValue())))
                .andExpect(
                        jsonPath(
                                "$.urlRecurso",
                                is("https://storage.archivo-digital.org/fondos/mapa-0100.jpg")))
                .andExpect(
                        jsonPath("$.descripcion", is("Escaneo de alta resolucion 600 DPI")))
                .andExpect(jsonPath("$.fechaDigitalizacion", notNullValue()))
                .andExpect(jsonPath("$.tags", hasSize(0)));

        // Verificar que la cascada + save no haya duplicado el registro en H2
        List<Digitalizacion> digitalizacionesEnBase =
                digitalizacionRepository.findByPiezaFisicaId(pieza.getId());
        assertEquals(1, digitalizacionesEnBase.size());

        mockMvc.perform(get("/api/v1/piezas-fisicas/{id}", pieza.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.digitalizaciones", hasSize(1)))
                .andExpect(
                        jsonPath(
                                "$.digitalizaciones[0].urlRecurso",
                                is("https://storage.archivo-digital.org/fondos/mapa-0100.jpg")));
    }

    @Test
    void agregarMultiplesDigitalizacionesAMismaPiezaIncrementaContadorYListaTodas()
            throws Exception {
        // Dado
        PiezaFisica pieza =
                guardarPieza("INV-2026-0101", "Libro de Actas", "Tomo I", "2026-03-05");

        // Cuando
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas/{id}/digitalizaciones", pieza.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "urlRecurso": "http://archivo.org/libro-tapa.jpg",
                                          "descripcion": "Tapa"
                                        }
                                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/piezas-fisicas/{id}/digitalizaciones", pieza.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "urlRecurso": "https://archivo.org/libro-interior.pdf",
                                          "descripcion": "Interior completo"
                                        }
                                        """))
                .andExpect(status().isCreated());

        // Entonces
        mockMvc.perform(get("/api/v1/piezas-fisicas/{id}", pieza.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.digitalizaciones", hasSize(2)));

        mockMvc.perform(get("/api/v1/piezas-fisicas/buscar").param("termino", "INV-2026-0101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].cantidadDigitalizaciones", is(2)));
    }

    @Test
    void agregarDigitalizacionAPiezaInexistenteDevuelve404() throws Exception {
        // Dado
        String json =
                """
                {
                  "urlRecurso": "https://storage.archivo-digital.org/fondos/inexistente.jpg",
                  "descripcion": "Archivo huerfano"
                }
                """;

        // Cuando / Entonces
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas/{id}/digitalizaciones", 9999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Pieza fisica con ID 9999 no encontrada")));
    }

    // ---------- 3. Creacion y asociacion de tags ----------

    @Test
    void crearYListarTagsRecortaEspaciosYDevuelve201Y200() throws Exception {
        // Dado
        String jsonTag =
                """
                {
                  "nombre": "  RESTAURADO  "
                }
                """;

        // Cuando / Entonces
        mockMvc.perform(
                        post("/api/v1/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonTag))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("RESTAURADO")));

        mockMvc.perform(get("/api/v1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("RESTAURADO")));
    }

    @Test
    void crearTagDuplicadoIgnorandoMayusculasYMinusculasDevuelve409() throws Exception {
        // Dado
        Tag existente = new Tag();
        existente.setNombre("MANUSCRITO");
        tagRepository.save(existente);

        String jsonDuplicado =
                """
                {
                  "nombre": "manuscrito"
                }
                """;

        // Cuando / Entonces
        mockMvc.perform(
                        post("/api/v1/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonDuplicado))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    void asociarMultiplesTagsADigitalizacionPersisteRelacionManyToManyYSeReflejaEnPieza()
            throws Exception {
        // Dado
        PiezaFisica pieza =
                guardarPieza("INV-2026-0200", "Diploma de Honor", "Pergamino", "2026-03-10");
        Digitalizacion dig =
                pieza.agregarDigitalizacion("https://archivo.org/diploma.jpg", "Frente");
        Digitalizacion digGuardada = digitalizacionRepository.save(dig);

        Tag tag1 = new Tag();
        tag1.setNombre("PERGAMINO");
        tag1 = tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setNombre("SIGLO_XX");
        tag2 = tagRepository.save(tag2);

        // Cuando
        mockMvc.perform(
                        post("/api/v1/digitalizaciones/{id}/tags", digGuardada.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tagId\": " + tag1.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0].nombre", is("PERGAMINO")));

        mockMvc.perform(
                        post("/api/v1/digitalizaciones/{id}/tags", digGuardada.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tagId\": " + tag2.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(
                        jsonPath(
                                "$.tags[*].nombre",
                                containsInAnyOrder("PERGAMINO", "SIGLO_XX")));

        // Entonces: verificar persistencia a traves de GET /api/v1/piezas-fisicas/{id}
        mockMvc.perform(get("/api/v1/piezas-fisicas/{id}", pieza.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.digitalizaciones", hasSize(1)))
                .andExpect(jsonPath("$.digitalizaciones[0].tags", hasSize(2)))
                .andExpect(
                        jsonPath(
                                "$.digitalizaciones[0].tags[*].nombre",
                                containsInAnyOrder("PERGAMINO", "SIGLO_XX")));
    }

    @Test
    void asociarMismoTagDosVecesADigitalizacionEsIdempotente() throws Exception {
        // Dado
        PiezaFisica pieza =
                guardarPieza("INV-2026-0201", "Afiche Historico", "Litografia", "2026-03-12");
        Digitalizacion dig =
                pieza.agregarDigitalizacion("https://archivo.org/afiche.jpg", "Frente");
        Digitalizacion digGuardada = digitalizacionRepository.save(dig);

        Tag tag = new Tag();
        tag.setNombre("LITOGRAFIA");
        tag = tagRepository.save(tag);

        String body = "{\"tagId\": " + tag.getId() + "}";

        // Cuando: se asocia dos veces el mismo tag en peticiones separadas
        mockMvc.perform(
                        post("/api/v1/digitalizaciones/{id}/tags", digGuardada.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(1)));

        mockMvc.perform(
                        post("/api/v1/digitalizaciones/{id}/tags", digGuardada.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(1)));
    }

    @Test
    void asociarTagConDigitalizacionOTagInexistenteDevuelve404() throws Exception {
        // Dado
        PiezaFisica pieza =
                guardarPieza("INV-2026-0202", "Moneda Conmemorativa", "Bronce", "2026-03-14");
        Digitalizacion dig =
                pieza.agregarDigitalizacion("https://archivo.org/moneda.jpg", "Anverso");
        Digitalizacion digGuardada = digitalizacionRepository.save(dig);

        // Cuando / Entonces: digitalizacion inexistente
        mockMvc.perform(
                        post("/api/v1/digitalizaciones/{id}/tags", 9999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tagId\": 1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Digitalizacion con ID 9999 no encontrada")));

        // Cuando / Entonces: tag inexistente
        mockMvc.perform(
                        post("/api/v1/digitalizaciones/{id}/tags", digGuardada.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"tagId\": 9999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Tag con ID 9999 no encontrado")));
    }

    @Test
    void eliminarTagExistenteAsociadoADigitalizacionDevuelve204YDesvinculaDePieza()
            throws Exception {
        // Dado
        PiezaFisica pieza =
                guardarPieza("INV-2026-0205", "Plano Arquitectonico", "Tinta", "2026-03-16");
        Digitalizacion dig =
                pieza.agregarDigitalizacion("https://archivo.org/plano.jpg", "Plano general");
        Tag tag = new Tag();
        tag.setNombre("ARQUITECTURA");
        tag = tagRepository.save(tag);
        dig.asociarTag(tag);
        digitalizacionRepository.save(dig);

        // Cuando / Entonces: elimina tag asociado devolviendo 204
        mockMvc.perform(delete("/api/v1/tags/{id}", tag.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/piezas-fisicas/{id}", pieza.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.digitalizaciones[0].tags", hasSize(0)));

        // Eliminar un tag ya inexistente devuelve 404
        mockMvc.perform(delete("/api/v1/tags/{id}", tag.getId()))
                .andExpect(status().isNotFound());
    }

    // ---------- 4. Busquedas, filtros y paginacion ----------

    @Test
    void buscarPiezasSinFiltrosDevuelveResultadosPaginados() throws Exception {
        // Dado
        guardarPieza("INV-2026-0301", "Pieza A", "Descripcion A", "2026-01-10");
        guardarPieza("INV-2026-0302", "Pieza B", "Descripcion B", "2026-02-10");
        guardarPieza("INV-2026-0303", "Pieza C", "Descripcion C", "2026-03-10");

        // Cuando / Entonces
        mockMvc.perform(
                        get("/api/v1/piezas-fisicas/buscar")
                                .param("page", "0")
                                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    void buscarPiezasPorTerminoCoincideEnNombreDescripcionOCodigoAunConDescripcionNull()
            throws Exception {
        // Dado
        guardarPieza("INV-2026-0310", "Acta de Asamblea", null, "2026-01-10");
        guardarPieza(
                "INV-2026-0311",
                "Fotografia Institucional",
                "Contiene sello del laboratorio central",
                "2026-02-10");
        guardarPieza("ESP-9999", "Objeto sin coincidencia", "Otro texto", "2026-03-10");

        // Busqueda por nombre (en pieza con descripcion null)
        mockMvc.perform(get("/api/v1/piezas-fisicas/buscar").param("termino", "  asamblea  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].codigoInventario", is("INV-2026-0310")));

        // Busqueda por descripcion
        mockMvc.perform(get("/api/v1/piezas-fisicas/buscar").param("termino", "LABORATORIO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].codigoInventario", is("INV-2026-0311")));

        // Busqueda por codigo de inventario
        mockMvc.perform(get("/api/v1/piezas-fisicas/buscar").param("termino", "esp-9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].codigoInventario", is("ESP-9999")));
    }

    @Test
    void buscarPiezasPorRangoDeFechasIncluyeLimitesExactos() throws Exception {
        // Dado: caso limite donde fechaDesde == fechaHasta
        guardarPieza("INV-2026-0320", "Pieza Previa", "Antigua", "2026-03-14");
        guardarPieza("INV-2026-0321", "Pieza Exacta", "En el dia", "2026-03-15");
        guardarPieza("INV-2026-0322", "Pieza Posterior", "Nueva", "2026-03-16");

        // Cuando / Entonces
        mockMvc.perform(
                        get("/api/v1/piezas-fisicas/buscar")
                                .param("fechaDesde", "2026-03-15")
                                .param("fechaHasta", "2026-03-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].codigoInventario", is("INV-2026-0321")));
    }

    @Test
    void buscarPiezasPorTagCruzaEntidadesSinDuplicarPiezaConMultiplesDigitalizaciones()
            throws Exception {
        // Dado
        Tag tagFoto = new Tag();
        tagFoto.setNombre("FOTOGRAFIA");
        tagFoto = tagRepository.save(tagFoto);

        PiezaFisica piezaConDosFotos =
                guardarPieza("INV-2026-0330", "Album Historico", "Album completo", "2026-04-01");
        Digitalizacion dig1 =
                piezaConDosFotos.agregarDigitalizacion("https://archivo.org/f1.jpg", "Foto 1");
        dig1.asociarTag(tagFoto);
        digitalizacionRepository.save(dig1);

        Digitalizacion dig2 =
                piezaConDosFotos.agregarDigitalizacion("https://archivo.org/f2.jpg", "Foto 2");
        dig2.asociarTag(tagFoto);
        digitalizacionRepository.save(dig2);

        guardarPieza("INV-2026-0331", "Pieza Sin Digitalizaciones", "Texto", "2026-04-02");

        // Cuando / Entonces
        mockMvc.perform(
                        get("/api/v1/piezas-fisicas/buscar")
                                .param("tag", "  fotografia ")
                                .param("sort", "fechaIngreso,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].codigoInventario", is("INV-2026-0330")))
                .andExpect(jsonPath("$.content[0].cantidadDigitalizaciones", is(2)));
    }

    // ---------- 5. Validaciones de datos y manejo de errores ----------

    @Test
    void crearPiezaConCamposObligatoriosInvalidosDevuelve400() throws Exception {
        // Dado: codigoInventario en blanco
        String jsonSinCodigo =
                """
                {
                  "codigoInventario": "   ",
                  "nombre": "Pieza sin codigo",
                  "fechaIngreso": "2026-03-15",
                  "tipoMaterial": "Papel"
                }
                """;

        // Cuando / Entonces
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonSinCodigo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("El codigo de inventario es obligatorio")));
    }

    @Test
    void agregarDigitalizacionConUrlInvalidaOSinEsquemaHttpDevuelve400() throws Exception {
        // Dado
        PiezaFisica pieza =
                guardarPieza("INV-2026-0400", "Documento Prueba", "Texto", "2026-03-15");

        // URL con esquema FTP no permitido
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas/{id}/digitalizaciones", pieza.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "urlRecurso": "ftp://servidor.archivo.org/archivo.tif",
                                          "descripcion": "Protocolo FTP"
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.error",
                                is(
                                        "La URL del recurso digitalizado debe ser una direccion"
                                                + " HTTP o HTTPS valida")));

        // URL con caracteres invalidos (espacios intermedios)
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas/{id}/digitalizaciones", pieza.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "urlRecurso": "https://servidor con espacios.org/a.jpg",
                                          "descripcion": "URL rota"
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.error",
                                is("La URL del recurso digitalizado tiene un formato invalido")));
    }

    @Test
    void crearTagConNombreEnBlancoYAsociarTagConTagIdNuloDevuelve400() throws Exception {
        // Tag con nombre en blanco
        mockMvc.perform(
                        post("/api/v1/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nombre\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("El nombre del tag es obligatorio")));

        // Asociar tag con tagId nulo
        mockMvc.perform(
                        post("/api/v1/digitalizaciones/{id}/tags", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("El ID del tag es obligatorio")));
    }

    @Test
    void enviarJsonMalformadoEstadoInvalidoOFechaDeBusquedaMalformadaDevuelve400()
            throws Exception {
        // Estado inexistente en el enum EstadoPieza
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "codigoInventario": "INV-2026-0410",
                                          "nombre": "Pieza",
                                          "fechaIngreso": "2026-03-15",
                                          "tipoMaterial": "Papel",
                                          "estado": "ESTADO_INEXISTENTE"
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.error",
                                is("El formato de los parametros o del cuerpo JSON es invalido")));

        // Parametro query fechaDesde con formato invalido
        mockMvc.perform(
                        get("/api/v1/piezas-fisicas/buscar")
                                .param("fechaDesde", "15-03-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.error",
                                is("El formato de los parametros o del cuerpo JSON es invalido")));
    }

    @Test
    void buscarPiezasConFechaDesdePosteriorAFechaHastaDevuelve400() throws Exception {
        // Dado / Cuando / Entonces
        mockMvc.perform(
                        get("/api/v1/piezas-fisicas/buscar")
                                .param("fechaDesde", "2026-12-31")
                                .param("fechaHasta", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.error",
                                is("La fecha desde no puede ser posterior a la fecha hasta")));
    }

    @Test
    void crearPiezaConTipoMaterialQueSupera50CaracteresDeberiaDevolver400() throws Exception {
        // Dado: caso limite de longitud de columna (tipoMaterial varchar(50))
        String tipoMaterialExcesivo = "M".repeat(60);
        String json =
                """
                {
                  "codigoInventario": "INV-2026-0500",
                  "nombre": "Pieza con tipoMaterial largo",
                  "fechaIngreso": "2026-03-15",
                  "tipoMaterial": "%s"
                }
                """
                        .formatted(tipoMaterialExcesivo);

        // Cuando / Entonces: un error de longitud de campo de entrada debe ser 400 Bad Request
        mockMvc.perform(
                        post("/api/v1/piezas-fisicas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearTagConNombreQueSupera100CaracteresDeberiaDevolver400() throws Exception {
        // Dado: caso limite de longitud de columna (Tag.nombre varchar(100))
        String nombreTagExcesivo = "T".repeat(120);
        String json =
                """
                {
                  "nombre": "%s"
                }
                """
                        .formatted(nombreTagExcesivo);

        // Cuando / Entonces: un error de longitud de campo de entrada debe ser 400 Bad Request
        mockMvc.perform(
                        post("/api/v1/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());
    }

    // ---------- Privados ----------

    private PiezaFisica guardarPieza(
            String codigo, String nombre, String descripcion, String fechaIso) {
        PiezaFisica pieza = new PiezaFisica();
        pieza.setCodigoInventario(codigo);
        pieza.setNombre(nombre);
        pieza.setDescripcion(descripcion);
        pieza.setFechaIngreso(LocalDate.parse(fechaIso));
        pieza.setTipoMaterial("Documento Papel");
        pieza.setEstado(EstadoPieza.EN_DEPOSITO);
        return piezaFisicaRepository.save(pieza);
    }
}

