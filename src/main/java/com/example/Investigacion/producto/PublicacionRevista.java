package com.example.Investigacion.producto;

import com.example.Investigacion.dto.ProductoRequest;

/*
 * BLOQUE: Producto concreto
 * Publicación en revista con revista, editorial, volumen y cuartil.
 */
public class PublicacionRevista extends Producto {

    public PublicacionRevista(ProductoRequest request) {
        super(1,
                request.titulo(),
                requerido(request.nombreRevista(), "nombre de la revista", "Revista"),
                requerido(request.editorial(), "editorial", "Revista"),
                requerido(request.volumen(), "volumen", "Revista"),
                request.quartil(),
                null,
                null,
                null,
                null);
    }

    @Override
    public String descripcionTipo() {
        return "Revista";
    }
}
