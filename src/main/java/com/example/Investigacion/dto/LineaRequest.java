package com.example.Investigacion.dto;

import jakarta.validation.constraints.NotNull;

public record LineaRequest(
        @NotNull(message = "La linea de investigacion es obligatoria.") Integer idLinea) {
}
