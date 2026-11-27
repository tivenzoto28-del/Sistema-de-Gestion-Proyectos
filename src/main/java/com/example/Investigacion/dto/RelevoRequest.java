package com.example.Investigacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * BLOQUE: Entrada
 * Datos para relevar la dirección con un nuevo subrogante.
 */
public record RelevoRequest(
        @NotNull(message = "El director saliente es obligatorio.") Integer idDirectorSaliente,
        @NotNull(message = "El nuevo director subrogante es obligatorio.") Integer idNuevoSubrogante,
        @NotBlank(message = "La resolucion de salida es obligatoria.") String resolucionSalida,
        @NotBlank(message = "El motivo de salida es obligatorio.") String motivoSalida,
        @NotBlank(message = "La resolucion de ingreso es obligatoria.") String resolucionIngreso) {
}
