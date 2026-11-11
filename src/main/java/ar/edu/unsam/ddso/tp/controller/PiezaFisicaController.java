package ar.edu.unsam.ddso.tp.controller;

import ar.edu.unsam.ddso.tp.dto.DigitalizacionCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.DigitalizacionResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaCreateRequestDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaDetalleResponseDTO;
import ar.edu.unsam.ddso.tp.dto.PiezaFisicaResponseDTO;
import ar.edu.unsam.ddso.tp.service.PiezaFisicaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controlador REST para el alta de piezas fisicas y asociacion de digitalizaciones. */
@RestController
@RequestMapping("/api/v1/piezas-fisicas")
public class PiezaFisicaController {

    private final PiezaFisicaService piezaFisicaService;

    public PiezaFisicaController(PiezaFisicaService piezaFisicaService) {
        this.piezaFisicaService = piezaFisicaService;
    }

    @PostMapping
    public ResponseEntity<PiezaFisicaResponseDTO> crearPieza(
            @Valid @RequestBody PiezaFisicaCreateRequestDTO request) {
        PiezaFisicaResponseDTO creada = piezaFisicaService.crearPieza(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PiezaFisicaDetalleResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(piezaFisicaService.obtenerPorId(id));
    }

    @PostMapping("/{id}/digitalizaciones")
    public ResponseEntity<DigitalizacionResponseDTO> agregarDigitalizacion(
            @PathVariable Long id, @Valid @RequestBody DigitalizacionCreateRequestDTO request) {
        DigitalizacionResponseDTO creada = piezaFisicaService.agregarDigitalizacion(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
