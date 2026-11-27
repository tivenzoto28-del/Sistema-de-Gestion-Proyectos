package com.example.Investigacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * BLOQUE: Entrada
 * Datos para el cambio formal de director de un proyecto.
 */
public record CambioDirectorRequest(
        @NotNull(message = "El docente saliente es obligatorio.") Integer idDocenteSaliente,
        @NotNull(message = "El docente entrante es obligatorio.") Integer idDocenteEntrante,
        @NotBlank(message = "La resolucion de salida es obligatoria.") String resolucionSalida,
        @NotBlank(message = "El motivo de salida es obligatorio.") String motivoSalida,
        @NotBlank(message = "La resolucion de ingreso es obligatoria.") String resolucionIngreso) {
}
