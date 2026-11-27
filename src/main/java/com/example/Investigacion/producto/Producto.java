package com.example.Investigacion.producto;

import java.time.LocalDate;

import com.example.Investigacion.common.ApiException;

/*
 * BLOQUE: Factory Method
 * Clase abstracta base: valida campos obligatorios y arma los parámetros de los procedimientos.
 */
public abstract class Producto {

    private final int tipo;
    private final String titulo;
    private final String nombre;
    private final String editorial;
    private final String datoA;
    private final String datoB;
    private final String datoC;
    private final String datoD;
    private final LocalDate fecha1;
    private final LocalDate fecha2;

    protected Producto(int tipo, String titulo, String nombre, String editorial,
            String datoA, String datoB, String datoC, String datoD,
            LocalDate fecha1, LocalDate fecha2) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.nombre = nombre;
        this.editorial = editorial;
        this.datoA = datoA;
        this.datoB = datoB;
        this.datoC = datoC;
        this.datoD = datoD;
        this.fecha1 = fecha1;
        this.fecha2 = fecha2;
    }

    public Object[] parametrosRegistro(int idProyecto) {
        return new Object[] {
                idProyecto, tipo, titulo, nombre, editorial,
                datoA, datoB, datoC, datoD, fecha1, fecha2
        };
    }

    public Object[] parametrosReclasificacion(int idProducto) {
        return new Object[] {
                idProducto, tipo, nombre, editorial,
                datoA, datoB, datoC, datoD, fecha1, fecha2
        };
    }

    public abstract String descripcionTipo();

    public int getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEditorial() {
        return editorial;
    }

    public String getDatoA() {
        return datoA;
    }

    public String getDatoB() {
        return datoB;
    }

    public String getDatoC() {
        return datoC;
    }

    public String getDatoD() {
        return datoD;
    }

    public LocalDate getFecha1() {
        return fecha1;
    }

    public LocalDate getFecha2() {
        return fecha2;
    }

    protected static String requerido(String valor, String campo, String tipo) {
        if (valor == null || valor.isBlank()) {
            throw new ApiException(400,
                    "El campo '" + campo + "' es obligatorio para registrar un producto de tipo " + tipo + ".");
        }
        return valor.trim();
    }

    protected static LocalDate requerido(LocalDate valor, String campo, String tipo) {
        if (valor == null) {
            throw new ApiException(400,
                    "El campo '" + campo + "' es obligatorio para registrar un producto de tipo " + tipo + ".");
        }
        return valor;
    }
}
