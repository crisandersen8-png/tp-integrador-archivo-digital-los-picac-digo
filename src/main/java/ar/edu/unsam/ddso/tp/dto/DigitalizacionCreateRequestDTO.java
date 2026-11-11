package ar.edu.unsam.ddso.tp.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO de entrada para asociar una nueva digitalizacion a una pieza fisica. */
public record DigitalizacionCreateRequestDTO(
        @NotBlank(message = "La URL del recurso es obligatoria") String urlRecurso,
        String descripcion) {}
