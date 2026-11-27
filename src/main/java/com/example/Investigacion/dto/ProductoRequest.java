package com.example.Investigacion.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * BLOQUE: Entrada
 * Datos comunes y específicos de los cuatro tipos de producto científico.
 */
public record ProductoRequest(
        Integer idProyecto,
        @NotNull(message = "El tipo de producto es obligatorio.") Integer tipo,
        @NotBlank(message = "El titulo de la publicacion es obligatorio.") String titulo,

        String nombreRevista,
        String editorial,
        String volumen,
        String quartil,

        String nombreCongreso,
        String tipoCongreso,
        String lugarCelebracion,
        String pais,
        String tipoParticipacion,

        String isbn,
        String tituloLibro,
        String lugarPublicacion,
        Integer totalPaginas,

        String numRegistro,
        String nombrePatente,
        String paisRegistro,
        String numSolicitud,
        String clasificacionInter,

        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDate fechaPublicacion,
        LocalDate fechaSolicitud) {
}
