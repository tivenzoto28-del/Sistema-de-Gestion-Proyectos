package com.example.Investigacion.common;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/*
 * BLOQUE: Manejo global de errores
 * Convierte excepciones de negocio y SQL en respuestas JSON uniformes, extrayendo mensajes SQLSTATE.
 */
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        return respuesta(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Datos invalidos en la peticion.");
        return respuesta(HttpStatus.BAD_REQUEST.value(), mensaje);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSql(SQLException ex) {
        return respuesta(HttpStatus.BAD_REQUEST.value(), mensajeSql(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage() == null ? "Error interno del servidor." : ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> respuesta(int status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status);
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }

    public static String mensajeSql(SQLException ex) {
        SQLException actual = ex;
        while (actual.getNextException() != null) {
            actual = actual.getNextException();
        }
        String texto = actual.getMessage();
        if (texto == null || texto.isBlank()) {
            texto = "Error de base de datos.";
        }
        return texto.replaceAll("\\s*\\(conn=\\d+\\)\\s*", "").trim();
    }
}
