package ar.edu.unsam.ddso.tp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO de entrada para asociar una nueva digitalizacion a una pieza fisica. */
public record DigitalizacionCreateRequestDTO(
        @NotBlank(message = "La URL del recurso es obligatoria")
                @Size(max = 500, message = "La URL del recurso no puede superar los 500 caracteres")
                String urlRecurso,
        String descripcion) {}
