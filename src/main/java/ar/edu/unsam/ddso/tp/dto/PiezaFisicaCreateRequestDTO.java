package ar.edu.unsam.ddso.tp.dto;

import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** DTO de entrada para registrar una nueva pieza fisica. */
public record PiezaFisicaCreateRequestDTO(
        @NotBlank(message = "El codigo de inventario es obligatorio") String codigoInventario,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        String descripcion,
        @NotNull(message = "La fecha de ingreso es obligatoria") LocalDate fechaIngreso,
        @NotBlank(message = "El tipo de material es obligatorio") String tipoMaterial,
        EstadoPieza estado) {}
