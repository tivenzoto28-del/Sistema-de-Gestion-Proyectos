package com.example.Investigacion.db;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * BLOQUE: Utilidades JDBC
 * Consultas, actualizaciones y llamadas a procedimientos con PreparedStatement y conversión de fechas.
 */
public final class Sql {

    private Sql() {
    }

    /*
     * Consultas parametrizadas
     * Ejecuta SELECT con PreparedStatement y convierte fechas a texto para el frontend.
     */
    public static List<Map<String, Object>> consultar(Connection conexion, String sql, Object... parametros)
            throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            asignar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                return leer(rs);
            }
        }
    }

    public static Map<String, Object> consultarUno(Connection conexion, String sql, Object... parametros)
            throws SQLException {
        List<Map<String, Object>> filas = consultar(conexion, sql, parametros);
        return filas.isEmpty() ? null : filas.get(0);
    }

    public static int actualizar(Connection conexion, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            asignar(ps, parametros);
            return ps.executeUpdate();
        }
    }

    /*
     * Procedimientos almacenados
     * Construye la llamada CALL con marcadores y devuelve el result set cuando existe.
     */
    public static List<Map<String, Object>> llamar(Connection conexion, String procedimiento, Object... parametros)
            throws SQLException {
        String sql = "{call " + procedimiento + "(" + marcadores(parametros.length) + ")}";
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            asignar(cs, parametros);
            boolean tieneResultado = cs.execute();
            if (tieneResultado) {
                try (ResultSet rs = cs.getResultSet()) {
                    return leer(rs);
                }
            }
            return List.of();
        }
    }

    public static void ejecutarProcedimiento(Connection conexion, String procedimiento, Object... parametros)
            throws SQLException {
        String sql = "{call " + procedimiento + "(" + marcadores(parametros.length) + ")}";
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            asignar(cs, parametros);
            cs.execute();
        }
    }

    private static String marcadores(int cantidad) {
        if (cantidad == 0) {
            return "";
        }
        return String.join(",", java.util.Collections.nCopies(cantidad, "?"));
    }

    /* BLOQUE: Asignación segura de parámetros
     * Maneja nulos, fechas, decimales y tipos primitivos sin concatenar valores en SQL. */
    private static void asignar(PreparedStatement ps, Object... parametros) throws SQLException {
        if (parametros == null) {
            return;
        }
        for (int i = 0; i < parametros.length; i++) {
            Object valor = parametros[i];
            int indice = i + 1;
            if (valor == null) {
                ps.setNull(indice, Types.NULL);
            } else if (valor instanceof LocalDate fecha) {
                ps.setDate(indice, Date.valueOf(fecha));
            } else if (valor instanceof LocalDateTime fechaHora) {
                ps.setTimestamp(indice, Timestamp.valueOf(fechaHora));
            } else if (valor instanceof OffsetDateTime fechaHora) {
                ps.setTimestamp(indice, Timestamp.from(fechaHora.toInstant()));
            } else if (valor instanceof BigDecimal decimal) {
                ps.setBigDecimal(indice, decimal);
            } else if (valor instanceof Integer entero) {
                ps.setInt(indice, entero);
            } else if (valor instanceof Long largo) {
                ps.setLong(indice, largo);
            } else if (valor instanceof Short corto) {
                ps.setShort(indice, corto);
            } else if (valor instanceof Byte octeto) {
                ps.setByte(indice, octeto);
            } else if (valor instanceof Boolean booleano) {
                ps.setBoolean(indice, booleano);
            } else if (valor instanceof Double decimal) {
                ps.setDouble(indice, decimal);
            } else if (valor instanceof Float decimal) {
                ps.setFloat(indice, decimal);
            } else {
                ps.setObject(indice, valor);
            }
        }
    }

    private static List<Map<String, Object>> leer(ResultSet rs) throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnas = meta.getColumnCount();
        while (rs.next()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            for (int i = 1; i <= columnas; i++) {
                fila.put(meta.getColumnLabel(i), convertir(rs.getObject(i)));
            }
            filas.add(fila);
        }
        return filas;
    }

    private static Object convertir(Object valor) throws SQLException {
        if (valor instanceof Date fecha) {
            return fecha.toLocalDate().toString();
        }
        if (valor instanceof Timestamp fechaHora) {
            return fechaHora.toLocalDateTime().toString();
        }
        if (valor instanceof Time hora) {
            return hora.toString();
        }
        if (valor instanceof LocalDate fecha) {
            return fecha.toString();
        }
        if (valor instanceof LocalDateTime fechaHora) {
            return fechaHora.toString();
        }
        if (valor instanceof Clob clob) {
            return clob.getSubString(1, (int) clob.length());
        }
        return valor;
    }
}
