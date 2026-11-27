package com.example.Investigacion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Investigacion.facade.GestionInvestigacionFacade;
import com.example.Investigacion.security.RequireRole;
import com.example.Investigacion.security.Role;
import com.example.Investigacion.security.SessionKeys;
import com.example.Investigacion.security.SessionUser;

@RestController
@RequestMapping("/api/reportes")
/*
 * BLOQUE: Reportes
 * Procedimientos CA1-CA5 y CM1-CM5 más consultas filtradas por persona.
 */
public class ReporteController {

    private final GestionInvestigacionFacade facade;

    public ReporteController(GestionInvestigacionFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/ca1")
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> ca1(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.reporteCa1(usuario);
    }

    @GetMapping("/ca2")
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> ca2(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.reporteCa2(usuario);
    }

    @GetMapping("/ca3")
    @RequireRole({Role.ADMIN, Role.SECRETARIA, Role.COORDINADOR, Role.DIRECTOR, Role.PROFESOR})
    public List<Map<String, Object>> ca3(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.reporteCa3(usuario);
    }

    @GetMapping("/ca4")
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> ca4(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.reporteCa4(usuario);
    }

    @GetMapping("/ca5")
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> ca5(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @RequestParam(required = false, defaultValue = "30") Integer dias) {
        return facade.reporteCa5(usuario, dias);
    }

    @GetMapping("/cm1")
    @RequireRole({Role.ADMIN, Role.SECRETARIA, Role.COORDINADOR, Role.DIRECTOR, Role.PROFESOR})
    public List<Map<String, Object>> cm1(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @RequestParam(required = false) Integer idProyecto) {
        return facade.reporteCm1(usuario, idProyecto);
    }

    @GetMapping("/cm2")
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> cm2(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.reporteCm2(usuario);
    }

    @GetMapping("/cm3")
    @RequireRole({Role.ADMIN, Role.SECRETARIA, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> cm3(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.reporteCm3(usuario);
    }

    @GetMapping("/cm4")
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> cm4(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.reporteCm4(usuario);
    }

    @GetMapping("/cm5")
    @RequireRole({Role.ADMIN, Role.SECRETARIA, Role.COORDINADOR, Role.DIRECTOR, Role.ESTUDIANTE})
    public List<Map<String, Object>> cm5(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.reporteCm5(usuario);
    }

    @GetMapping("/vista-docente")
    @RequireRole({Role.ADMIN, Role.PROFESOR})
    public List<Map<String, Object>> vistaDocente(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.vistaDocente(usuario);
    }

    @GetMapping("/vista-estudiante")
    @RequireRole({Role.ADMIN, Role.ESTUDIANTE})
    public List<Map<String, Object>> vistaEstudiante(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.vistaEstudiante(usuario);
    }

    @GetMapping("/docente/{idProfesor}/proyectos")
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR, Role.PROFESOR})
    public List<Map<String, Object>> proyectosDocente(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProfesor) {
        return facade.proyectosPorDocente(usuario, idProfesor);
    }

    @GetMapping("/estudiante/{idEstudiante}/proyectos")
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR, Role.ESTUDIANTE})
    public List<Map<String, Object>> proyectosEstudiante(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idEstudiante) {
        return facade.proyectosPorEstudiante(usuario, idEstudiante);
    }
}
