package com.example.Investigacion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Investigacion.facade.GestionInvestigacionFacade;
import com.example.Investigacion.security.RequireRole;
import com.example.Investigacion.security.Role;
import com.example.Investigacion.security.SessionKeys;
import com.example.Investigacion.security.SessionUser;

@RestController
@RequestMapping("/api/auditoria")
@RequireRole({Role.ADMIN})
/*
 * BLOQUE: Auditoría
 * Consultas a las tablas generadas por los triggers de base de datos.
 */
public class AuditoriaController {

    private final GestionInvestigacionFacade facade;

    public AuditoriaController(GestionInvestigacionFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/operaciones")
    public List<Map<String, Object>> operaciones(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.auditoriaOperaciones(usuario);
    }

    @GetMapping("/proyectos")
    public List<Map<String, Object>> proyectos(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.auditoriaProyectos(usuario);
    }

    @GetMapping("/participacion")
    public List<Map<String, Object>> participacion(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.auditoriaParticipacion(usuario);
    }
}
