package com.example.Investigacion.dto;

import jakarta.validation.constraints.NotBlank;

/*
 * BLOQUE: Entrada
 * Resolución de cierre y opción de desvincular equipo y estudiantes.
 */
public record CerrarProyectoRequest(
        @NotBlank(message = "La resolucion de cierre es obligatoria.") String resolucionCierre,
        Boolean desvincular) {
}
