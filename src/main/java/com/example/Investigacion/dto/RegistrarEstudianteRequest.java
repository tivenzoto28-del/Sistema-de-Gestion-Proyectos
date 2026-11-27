package com.example.Investigacion.dto;

import jakarta.validation.constraints.NotBlank;

/*
 * BLOQUE: Entrada
 * Datos del estudiante vinculado a un proyecto.
 */
public record RegistrarEstudianteRequest(
        @NotBlank(message = "Los nombres del estudiante son obligatorios.") String nombresEst,
        String telefonoEst,
        @NotBlank(message = "El departamento o facultad es obligatorio.") String departamentoFacultad) {
}
