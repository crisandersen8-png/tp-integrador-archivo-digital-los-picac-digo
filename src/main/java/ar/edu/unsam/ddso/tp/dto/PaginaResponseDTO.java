package ar.edu.unsam.ddso.tp.dto;

import java.util.List;

/** DTO de salida generico para respuestas paginadas con estructura JSON estable. */
public record PaginaResponseDTO<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last) {

    public PaginaResponseDTO {
        content = content == null ? List.of() : List.copyOf(content);
    }
}
