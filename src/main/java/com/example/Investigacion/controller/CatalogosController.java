package com.example.Investigacion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Investigacion.facade.GestionInvestigacionFacade;
import com.example.Investigacion.security.RequireRole;
import com.example.Investigacion.security.Role;

@RestController
@RequestMapping("/api/catalogos")
@RequireRole({Role.ADMIN, Role.SECRETARIA, Role.COORDINADOR, Role.DIRECTOR, Role.PROFESOR, Role.ESTUDIANTE})
/*
 * BLOQUE: Catálogos
 * Listas de apoyo para los formularios: proyectos, profesores, estudiantes, líneas y tipos.
 */
public class CatalogosController {

    private final GestionInvestigacionFacade facade;

    public CatalogosController(GestionInvestigacionFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/proyectos")
    public List<Map<String, Object>> proyectos() {
        return facade.catalogoProyectos();
    }

    @GetMapping("/profesores")
    public List<Map<String, Object>> profesores() {
        return facade.catalogoProfesores();
    }

    @GetMapping("/estudiantes")
    public List<Map<String, Object>> estudiantes() {
        return facade.catalogoEstudiantes();
    }

    @GetMapping("/lineas")
    public List<Map<String, Object>> lineas() {
        return facade.catalogoLineas();
    }

    @GetMapping("/tipos-producto")
    public List<Map<String, Object>> tiposProducto() {
        return facade.catalogoTiposProducto();
    }

    @GetMapping("/proyectos/{idProyecto}/equipo")
    public List<Map<String, Object>> equipo(@PathVariable Integer idProyecto) {
        return facade.equipoProyecto(idProyecto);
    }

    @GetMapping("/proyectos/{idProyecto}/estudiantes")
    public List<Map<String, Object>> estudiantesProyecto(@PathVariable Integer idProyecto) {
        return facade.estudiantesProyecto(idProyecto);
    }
}
