package com.example.Investigacion.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Investigacion.facade.GestionInvestigacionFacade;
import com.example.Investigacion.security.RequireRole;
import com.example.Investigacion.security.Role;

@RestController
@RequestMapping("/api/dashboard")
/*
 * BLOQUE: Panel general
 * Resumen institutional para roles administrativos.
 */
public class DashboardController {

    private final GestionInvestigacionFacade facade;

    public DashboardController(GestionInvestigacionFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR})
    public Map<String, Object> resumen() {
        return facade.resumenDashboard();
    }
}
