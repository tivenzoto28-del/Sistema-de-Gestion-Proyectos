package com.example.Investigacion.dto;

import jakarta.validation.constraints.NotBlank;

/*
 * BLOQUE: Entrada
 * Credenciales del formulario de inicio de sesión.
 */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio.") String usuario,
        @NotBlank(message = "La contrasena es obligatoria.") String password) {
}
