package com.example.Investigacion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * BLOQUE: Entrada
 * Datos del proyecto registrado mediante transacción.
 */
public record RegistrarProyectoRequest(
        @NotBlank(message = "El nombre del proyecto es obligatorio.") String nombreProyecto,
        @NotBlank(message = "El acronimo es obligatorio.") String acronimo,
        @NotNull(message = "El presupuesto es obligatorio.")
        @DecimalMin(value = "0.00", message = "El presupuesto no puede ser negativo.") BigDecimal presupuesto,
        @NotBlank(message = "El programa es obligatorio.") String programa,
        String descripcion,
        @NotNull(message = "La fecha de inicio es obligatoria.") LocalDate inicioPro,
        @NotNull(message = "La fecha de fin es obligatoria.") LocalDate finPro,
        @NotBlank(message = "La resolucion de aprobacion es obligatoria.") String resolucion) {
}
