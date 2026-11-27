package com.example.Investigacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * BLOQUE: Entrada
 * Datos para asignar un profesor a un proyecto con su cargo y resolución.
 */
public record AsignarProfesorRequest(
        @NotNull(message = "El profesor es obligatorio.") Integer idProfesor,
        @NotBlank(message = "El cargo es obligatorio.") String cargo,
        String resolucionIngreso) {
}
