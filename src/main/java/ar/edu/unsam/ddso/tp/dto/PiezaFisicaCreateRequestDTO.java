package ar.edu.unsam.ddso.tp.dto;

import ar.edu.unsam.ddso.tp.model.enums.EstadoPieza;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** DTO de entrada para registrar una nueva pieza fisica. */
public record PiezaFisicaCreateRequestDTO(
        @NotBlank(message = "El codigo de inventario es obligatorio")
                @Size(
                        max = 100,
                        message = "El codigo de inventario no puede superar los 100 caracteres")
                String codigoInventario,
        @NotBlank(message = "El nombre es obligatorio")
                @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
                String nombre,
        String descripcion,
        @NotNull(message = "La fecha de ingreso es obligatoria") LocalDate fechaIngreso,
        @NotBlank(message = "El tipo de material es obligatorio")
                @Size(max = 50, message = "El tipo de material no puede superar los 50 caracteres")
                String tipoMaterial,
        EstadoPieza estado) {}
