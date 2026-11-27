package com.example.Investigacion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Investigacion.dto.CrearUsuarioRequest;
import com.example.Investigacion.facade.GestionInvestigacionFacade;
import com.example.Investigacion.security.RequireRole;
import com.example.Investigacion.security.Role;
import com.example.Investigacion.security.SessionKeys;
import com.example.Investigacion.security.SessionUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
/*
 * BLOQUE: Usuarios
 * Creación delegada y listado según el rol de quien crea: admin o secretaría.
 */
public class UsuarioController {

    private final GestionInvestigacionFacade facade;

    public UsuarioController(GestionInvestigacionFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    @RequireRole({Role.ADMIN, Role.SECRETARIA})
    public List<Map<String, Object>> listar(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.listarUsuarios(usuario);
    }

    @PostMapping
    @RequireRole({Role.ADMIN, Role.SECRETARIA})
    public Map<String, Object> crear(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser creador,
            @RequestBody @Valid CrearUsuarioRequest request) {
        return facade.crearUsuario(creador, request);
    }
}
