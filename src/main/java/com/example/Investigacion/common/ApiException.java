package com.example.Investigacion.common;

/*
 * BLOQUE: Errores de negocio
 * Excepción liviana que lleva código HTTP y mensaje para el frontend.
 */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public ApiException(int status, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
