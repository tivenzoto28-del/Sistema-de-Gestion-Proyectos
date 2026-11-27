package com.example.Investigacion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Investigacion.dto.RegistrarLineaRequest;
import com.example.Investigacion.facade.GestionInvestigacionFacade;
import com.example.Investigacion.security.RequireRole;
import com.example.Investigacion.security.Role;
import com.example.Investigacion.security.SessionKeys;
import com.example.Investigacion.security.SessionUser;

import jakarta.validation.Valid;

/*
 * BLOQUE: Líneas de investigación
 * Alta de líneas y consulta del catálogo para admin y secretaría.
 */
@RestController
@RequestMapping("/api/lineas")
public class LineaController {

    private final GestionInvestigacionFacade facade;

    public LineaController(GestionInvestigacionFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    @RequireRole({Role.ADMIN, Role.SECRETARIA, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> listar(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.catalogoLineas();
    }

    @PostMapping
    @RequireRole({Role.ADMIN, Role.SECRETARIA})
    public Map<String, Object> registrar(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @RequestBody @Valid RegistrarLineaRequest request) {
        return facade.registrarLinea(usuario, request);
    }
}
