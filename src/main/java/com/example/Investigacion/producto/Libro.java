package com.example.Investigacion.producto;

import com.example.Investigacion.dto.ProductoRequest;

/*
 * BLOQUE: Producto concreto
 * Libro con ISBN, editorial y fecha de publicación.
 */
public class Libro extends Producto {

    public Libro(ProductoRequest request) {
        super(3,
                request.titulo(),
                requerido(request.tituloLibro(), "titulo del libro", "Libro"),
                requerido(request.editorial(), "editorial", "Libro"),
                requerido(request.isbn(), "ISBN", "Libro"),
                null,
                null,
                null,
                requerido(request.fechaPublicacion(), "fecha de publicacion", "Libro"),
                null);
    }

    @Override
    public String descripcionTipo() {
        return "Libro";
    }
}
