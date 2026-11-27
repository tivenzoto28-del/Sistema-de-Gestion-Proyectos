package com.example.Investigacion.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrarLineaRequest(
        @NotBlank(message = "El nombre de la linea es obligatorio.") String nombreLinea,
        String descriptores) {
}
