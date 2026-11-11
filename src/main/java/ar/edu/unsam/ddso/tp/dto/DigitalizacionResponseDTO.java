package ar.edu.unsam.ddso.tp.dto;

import java.time.LocalDateTime;
import java.util.List;

/** DTO de salida para representar una digitalizacion y sus tags asociados. */
public record DigitalizacionResponseDTO(
        Long id,
        Long piezaFisicaId,
        String urlRecurso,
        String descripcion,
        LocalDateTime fechaDigitalizacion,
        List<TagResponseDTO> tags) {

    public DigitalizacionResponseDTO {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
