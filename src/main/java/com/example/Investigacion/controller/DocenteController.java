package com.example.Investigacion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Investigacion.dto.LineaRequest;
import com.example.Investigacion.facade.GestionInvestigacionFacade;
import com.example.Investigacion.security.RequireRole;
import com.example.Investigacion.security.Role;
import com.example.Investigacion.security.SessionKeys;
import com.example.Investigacion.security.SessionUser;

import jakarta.validation.Valid;

/*
 * BLOQUE: Docentes
 * Asignación y consulta de líneas de investigación por docente.
 */
@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final GestionInvestigacionFacade facade;

    public DocenteController(GestionInvestigacionFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/{idProfesor}/lineas")
    @RequireRole({Role.ADMIN, Role.SECRETARIA, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> lineas(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProfesor) {
        return facade.lineasProfesor(usuario, idProfesor);
    }

    @PostMapping("/{idProfesor}/lineas")
    @RequireRole({Role.ADMIN, Role.SECRETARIA})
    public Map<String, Object> asignarLinea(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProfesor,
            @RequestBody @Valid LineaRequest request) {
        return facade.asignarLineaProfesor(usuario, idProfesor, request);
    }
}
