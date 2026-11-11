package ar.edu.unsam.ddso.tp.controller;

import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.service.DisponibilizacionService;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controlador REST para la busqueda avanzada paginada de piezas fisicas. */
@RestController
@RequestMapping("/api/v1/piezas-fisicas")
public class DisponibilizacionController {

    private final DisponibilizacionService disponibilizacionService;

    public DisponibilizacionController(DisponibilizacionService disponibilizacionService) {
        this.disponibilizacionService = disponibilizacionService;
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<PiezaFisicaResponseDTO>> buscarPiezas(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fechaHasta,
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                disponibilizacionService.buscarPiezas(
                        termino, fechaDesde, fechaHasta, tag, pageable));
    }
}

