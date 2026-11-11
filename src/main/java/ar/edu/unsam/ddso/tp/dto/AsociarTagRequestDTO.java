package ar.edu.unsam.ddso.tp.dto;

import jakarta.validation.constraints.NotNull;

/** DTO de entrada para asociar un tag existente a una digitalizacion. */
public record AsociarTagRequestDTO(
        @NotNull(message = "El ID del tag es obligatorio") Long tagId) {}

