package ar.edu.unsam.ddso.tp.dto;

import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import java.time.LocalDate;
import java.util.List;

/** DTO de salida con el detalle completo de una pieza fisica y sus digitalizaciones. */
public record PiezaFisicaDetalleResponseDTO(
        Long id,
        String codigoInventario,
        String nombre,
        String descripcion,
        LocalDate fechaIngreso,
        String tipoMaterial,
        EstadoPieza estado,
        List<DigitalizacionResponseDTO> digitalizaciones) {

    public PiezaFisicaDetalleResponseDTO {
        digitalizaciones = digitalizaciones == null ? List.of() : List.copyOf(digitalizaciones);
    }
}
