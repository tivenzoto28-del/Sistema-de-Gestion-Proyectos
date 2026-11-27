package com.example.Investigacion.producto;

import com.example.Investigacion.dto.ProductoRequest;

/*
 * BLOQUE: Producto concreto
 * Patente con número de registro, solicitud, país y clasificación.
 */
public class Patente extends Producto {

    public Patente(ProductoRequest request) {
        super(4,
                request.titulo(),
                requerido(request.nombrePatente(), "nombre de la patente", "Patente"),
                null,
                requerido(request.numRegistro(), "numero de registro", "Patente"),
                requerido(request.paisRegistro(), "pais de registro", "Patente"),
                requerido(request.numSolicitud(), "numero de solicitud", "Patente"),
                request.clasificacionInter(),
                requerido(request.fechaSolicitud(), "fecha de solicitud", "Patente"),
                null);
    }

    @Override
    public String descripcionTipo() {
        return "Patente";
    }
}
