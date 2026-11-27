package com.example.Investigacion.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/*
 * BLOQUE: Entrada
 * Nueva fecha de fin para la prórroga controlada.
 */
public record ProrrogaRequest(
        @NotNull(message = "La nueva fecha de fin es obligatoria.") LocalDate nuevaFechaFin) {
}
