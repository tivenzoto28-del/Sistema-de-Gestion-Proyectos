package com.example.Investigacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * BLOQUE: Entrada
 * Usuario, contraseña, rol y persona vinculada para la creación delegada de cuentas.
 */
public record CrearUsuarioRequest(
        @NotBlank(message = "El usuario es obligatorio.")
        @Pattern(regexp = "[A-Za-z0-9_]{3,50}", message = "El usuario solo admite letras, numeros y guion bajo.")
        String usuario,

        @NotBlank(message = "La contrasena es obligatoria.")
        @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres.")
        String password,

        @NotBlank(message = "El rol es obligatorio.")
        String rol,

        Integer idProfesor,
        Integer idEstudiante,
        String nombrePersona) {
}
