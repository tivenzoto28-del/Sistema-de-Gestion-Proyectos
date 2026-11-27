package com.example.Investigacion.producto;

import org.springframework.stereotype.Component;

import com.example.Investigacion.common.ApiException;
import com.example.Investigacion.dto.ProductoRequest;

@Component
/*
 * BLOQUE: Factory Method
 * Selecciona la subclase concreta según el tipo 1=Revista, 2=Congreso, 3=Libro o 4=Patente.
 */
public class ProductoFactory {

    public Producto crear(ProductoRequest request) {
        if (request == null || request.tipo() == null) {
            throw new ApiException(400, "Debe indicar el tipo de producto cientifico.");
        }
        if (request.titulo() == null || request.titulo().isBlank()) {
            throw new ApiException(400, "El titulo de la publicacion es obligatorio.");
        }
        return switch (request.tipo()) {
            case 1 -> new PublicacionRevista(request);
            case 2 -> new PublicacionCongreso(request);
            case 3 -> new Libro(request);
            case 4 -> new Patente(request);
            default -> throw new ApiException(400,
                    "Tipo de producto no valido. Use 1=Revista, 2=Congreso, 3=Libro, 4=Patente.");
        };
    }
}
