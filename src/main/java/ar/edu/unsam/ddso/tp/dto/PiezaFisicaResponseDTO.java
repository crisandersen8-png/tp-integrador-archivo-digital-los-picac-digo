package ar.edu.unsam.ddso.tp.dto;

import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import java.time.LocalDate;

/** DTO de salida con el resumen de una pieza fisica. */
public record PiezaFisicaResponseDTO(
        Long id,
        String codigoInventario,
        String nombre,
        String descripcion,
        LocalDate fechaIngreso,
        String tipoMaterial,
        EstadoPieza estado,
        int cantidadDigitalizaciones) {}
