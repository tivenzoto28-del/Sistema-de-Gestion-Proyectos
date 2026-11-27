package com.example.Investigacion.db;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
/*
 * BLOQUE: Contrato de persistencia
 * Interfaz funcional que recibe una Connection ya configurada.
 */
public interface SqlWork<T> {
    T ejecutar(Connection conexion) throws SQLException;
}
