package ar.edu.unsam.ddso.tp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO de entrada para crear una nueva etiqueta (Tag). */
public record TagCreateRequestDTO(
        @NotBlank(message = "El nombre del tag es obligatorio")
                @Size(max = 100, message = "El nombre del tag no puede superar los 100 caracteres")
                String nombre) {}

