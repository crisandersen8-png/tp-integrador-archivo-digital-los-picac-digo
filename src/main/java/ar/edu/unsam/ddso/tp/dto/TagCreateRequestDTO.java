package ar.edu.unsam.ddso.tp.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO de entrada para crear una nueva etiqueta (Tag). */
public record TagCreateRequestDTO(
        @NotBlank(message = "El nombre del tag es obligatorio") String nombre) {}

