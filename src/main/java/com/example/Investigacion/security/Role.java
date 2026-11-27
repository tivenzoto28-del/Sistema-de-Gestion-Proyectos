package com.example.Investigacion.security;

/*
 * BLOQUE: Seguridad
 * Roles del sistema y conversión desde el texto almacenado en la base de datos.
 */
public enum Role {
    ADMIN("Administrador del sistema"),
    SECRETARIA("Secretaria"),
    COORDINADOR("Coordinador"),
    DIRECTOR("Director de investigacion"),
    PROFESOR("Profesor"),
    ESTUDIANTE("Estudiante");

    private final String etiqueta;

    Role(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Role desdeNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        for (Role rol : values()) {
            if (rol.name().equalsIgnoreCase(nombre.trim())) {
                return rol;
            }
        }
        return null;
    }
}
