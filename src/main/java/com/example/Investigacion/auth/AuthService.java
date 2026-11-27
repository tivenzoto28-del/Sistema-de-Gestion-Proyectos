package com.example.Investigacion.auth;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.Investigacion.common.ApiException;
import com.example.Investigacion.db.DatabaseService;
import com.example.Investigacion.db.Sql;
import com.example.Investigacion.security.Role;
import com.example.Investigacion.security.SessionUser;

@Service
/*
 * BLOQUE: Autenticación
 * Valida credenciales abriendo una conexión JDBC con el usuario y carga su persona vinculada desde la base de datos.
 */
public class AuthService {

    private final DatabaseService databaseService;

    public AuthService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /*
     * Validación de credenciales y vinculación de persona
     * Lee el rol y el id de persona de la base de datos, verifica la conexión JDBC y construye SessionUser.
     */
    public SessionUser autenticar(String usuario, String password) {
        Map<String, Object> persona = databaseService.conCatalogo(conexion ->
                Sql.consultarUno(conexion, """
                        SELECT rol, id_profesor, id_estudiante, nombre_persona
                          FROM usuario_persona
                         WHERE usuario = ?
                        """, usuario));

        if (persona == null) {
            throw new ApiException(401,
                    "El usuario '" + usuario + "' no esta registrado en el sistema.");
        }

        Role rol = Role.desdeNombre((String) persona.get("rol"));
        if (rol == null) {
            throw new ApiException(401, "El rol configurado para '" + usuario + "' no es valido.");
        }

        try (Connection conexion = databaseService.abrir(usuario, password)) {
            if (!conexion.isValid(3)) {
                throw new ApiException(401, "No fue posible validar la conexion del usuario.");
            }
        } catch (SQLException ex) {
            throw new ApiException(401,
                    "Credenciales invalidas. Verifique su usuario y contrasena.");
        }

        return new SessionUser(
                usuario,
                password,
                rol,
                entero(persona.get("id_profesor")),
                entero(persona.get("id_estudiante")),
                String.valueOf(persona.get("nombre_persona")));
    }

    private Integer entero(Object valor) {
        return valor instanceof Number numero ? numero.intValue() : null;
    }
}
