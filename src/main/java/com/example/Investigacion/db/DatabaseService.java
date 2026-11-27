package com.example.Investigacion.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.Investigacion.common.ApiException;
import com.example.Investigacion.common.GlobalExceptionHandler;
import com.example.Investigacion.security.SessionUser;

import jakarta.annotation.PostConstruct;

@Service
/*
 * BLOQUE: Persistencia
 * Abre conexiones por usuario autenticado o con la cuenta de catálogos; separa fallo de conexión de error SQL.
 */
public class DatabaseService {

    private final String url;
    private final String catalogUser;
    private final String catalogPassword;

    public DatabaseService(
            @Value("${app.db.url}") String url,
            @Value("${app.db.catalog-user}") String catalogUser,
            @Value("${app.db.catalog-password}") String catalogPassword) {
        this.url = url;
        this.catalogUser = catalogUser;
        this.catalogPassword = catalogPassword;
    }

    @PostConstruct
    public void cargarDriver() throws ClassNotFoundException {
        Class.forName("org.mariadb.jdbc.Driver");
    }

    public Connection abrir(String usuario, String password) throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }

    /*
     * Conexión del usuario autenticado
     * Cada operación de negocio usa la cuenta MariaDB del usuario para aplicar sus GRANT reales.
     */
    public <T> T conUsuario(SessionUser usuario, SqlWork<T> trabajo) {
        try (Connection conexion = abrir(usuario.usuario(), usuario.password())) {
            return trabajo.ejecutar(conexion);
        } catch (SQLException ex) {
            throw new ApiException(400, GlobalExceptionHandler.mensajeSql(ex), ex);
        }
    }

    /*
     * Conexión privilegiada de catálogos
     * Se usa para listas de apoyo, creación de usuarios y consultas filtradas; distingue fallo de conexión de error SQL.
     */
    public <T> T conCatalogo(SqlWork<T> trabajo) {
        Connection conexion;
        try {
            conexion = abrir(catalogUser, catalogPassword);
        } catch (SQLException ex) {
            throw new ApiException(500,
                    "No se pudo conectar con el usuario de catalogos '" + catalogUser
                            + "'. Verifique app.db.catalog-user y app.db.catalog-password. Detalle: "
                            + GlobalExceptionHandler.mensajeSql(ex),
                    ex);
        }
        try (conexion) {
            return trabajo.ejecutar(conexion);
        } catch (SQLException ex) {
            throw new ApiException(400, GlobalExceptionHandler.mensajeSql(ex), ex);
        }
    }

    public <T> T conCredenciales(String usuario, String password, SqlWork<T> trabajo) {
        try (Connection conexion = abrir(usuario, password)) {
            return trabajo.ejecutar(conexion);
        } catch (SQLException ex) {
            throw new ApiException(400, GlobalExceptionHandler.mensajeSql(ex), ex);
        }
    }
}
