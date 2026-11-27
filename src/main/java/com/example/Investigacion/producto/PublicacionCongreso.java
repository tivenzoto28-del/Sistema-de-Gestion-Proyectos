package com.example.Investigacion.producto;

import com.example.Investigacion.dto.ProductoRequest;

/*
 * BLOQUE: Producto concreto
 * Ponencia o publicación de congreso con fecha, lugar, país y tipo de participación.
 */
public class PublicacionCongreso extends Producto {

    public PublicacionCongreso(ProductoRequest request) {
        super(2,
                request.titulo(),
                requerido(request.nombreCongreso(), "nombre del congreso", "Congreso"),
                null,
                requerido(request.tipoCongreso(), "tipo de congreso", "Congreso"),
                requerido(request.lugarCelebracion(), "lugar de celebracion", "Congreso"),
                requerido(request.pais(), "pais", "Congreso"),
                requerido(request.tipoParticipacion(), "tipo de participacion", "Congreso"),
                requerido(request.fechaInicio(), "fecha de inicio", "Congreso"),
                requerido(request.fechaFin(), "fecha de fin", "Congreso"));
    }

    @Override
    public String descripcionTipo() {
        return "Congreso";
    }
}
