package com.example.Investigacion.security;

import java.io.Serializable;

/*
 * BLOQUE: Seguridad
 * Datos de la sesión: usuario, rol, credencial de conexión y persona vinculada.
 */
public record SessionUser(String usuario, String password, Role rol,
                          Integer idProfesor, Integer idEstudiante, String nombrePersona)
        implements Serializable {

    public String rolEtiqueta() {
        return rol.getEtiqueta();
    }
}
