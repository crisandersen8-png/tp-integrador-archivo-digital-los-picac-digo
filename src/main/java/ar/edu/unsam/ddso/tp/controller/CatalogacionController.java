package ar.edu.unsam.ddso.tp.controller;

import ar.edu.unsam.ddso.tp.dto.AsociarTagRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitalizacionResponseDTO;
import ar.edu.unsam.ddso.tp.dto.TagCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.TagResponseDTO;
import ar.edu.unsam.ddso.tp.service.CatalogacionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controlador REST para la administracion de tags y su asociacion a digitalizaciones. */
@RestController
@RequestMapping("/api/v1")
public class CatalogacionController {

    private final CatalogacionService catalogacionService;

    public CatalogacionController(CatalogacionService catalogacionService) {
        this.catalogacionService = catalogacionService;
    }

    @GetMapping("/tags")
    public ResponseEntity<List<TagResponseDTO>> listarTags() {
        return ResponseEntity.ok(catalogacionService.listarTags());
    }

    @PostMapping("/tags")
    public ResponseEntity<TagResponseDTO> crearTag(
            @Valid @RequestBody TagCreateRequestDTO request) {
        TagResponseDTO creado = catalogacionService.crearTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/digitalizaciones/{id}/tags")
    public ResponseEntity<DigitalizacionResponseDTO> asociarTag(
            @PathVariable Long id, @Valid @RequestBody AsociarTagRequestDTO request) {
        return ResponseEntity.ok(catalogacionService.asociarTagADigitalizacion(id, request));
    }
}

