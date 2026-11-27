package com.example.Investigacion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Investigacion.dto.ProductoRequest;
import com.example.Investigacion.facade.GestionInvestigacionFacade;
import com.example.Investigacion.security.RequireRole;
import com.example.Investigacion.security.Role;
import com.example.Investigacion.security.SessionKeys;
import com.example.Investigacion.security.SessionUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
/*
 * BLOQUE: Productos
 * Registro y reclasificación de productos científicos mediante Factory Method.
 */
public class ProductoController {

    private final GestionInvestigacionFacade facade;

    public ProductoController(GestionInvestigacionFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> listar(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.listarProductos(usuario);
    }

    @PostMapping
    @RequireRole({Role.ADMIN, Role.SECRETARIA})
    public Map<String, Object> registrar(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @RequestBody @Valid ProductoRequest request) {
        return facade.registrarProducto(usuario, request);
    }

    @PostMapping("/{idProducto}/reclasificar")
    @RequireRole({Role.ADMIN})
    public Map<String, Object> reclasificar(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProducto,
            @RequestBody @Valid ProductoRequest request) {
        return facade.reclasificarProducto(usuario, idProducto, request);
    }
}
