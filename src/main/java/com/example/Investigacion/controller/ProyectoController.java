package com.example.Investigacion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Investigacion.dto.AsignarProfesorRequest;
import com.example.Investigacion.dto.CambioDirectorRequest;
import com.example.Investigacion.dto.CerrarProyectoRequest;
import com.example.Investigacion.dto.ProrrogaRequest;
import com.example.Investigacion.dto.RegistrarEstudianteRequest;
import com.example.Investigacion.dto.RegistrarProyectoRequest;
import com.example.Investigacion.dto.RelevoRequest;
import com.example.Investigacion.facade.GestionInvestigacionFacade;
import com.example.Investigacion.security.RequireRole;
import com.example.Investigacion.security.Role;
import com.example.Investigacion.security.SessionKeys;
import com.example.Investigacion.security.SessionUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/proyectos")
/*
 * BLOQUE: Proyectos
 * CRUD y operaciones transaccionales: alta, prórroga, director, relevo, cierre, equipo y estudiantes.
 */
public class ProyectoController {

    private final GestionInvestigacionFacade facade;

    public ProyectoController(GestionInvestigacionFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR})
    public List<Map<String, Object>> listar(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario) {
        return facade.listarProyectos(usuario);
    }

    @GetMapping("/{idProyecto}")
    @RequireRole({Role.ADMIN, Role.COORDINADOR, Role.DIRECTOR})
    public Map<String, Object> detalle(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProyecto) {
        return facade.detalleProyecto(usuario, idProyecto);
    }

    @PostMapping
    @RequireRole({Role.ADMIN, Role.SECRETARIA})
    public Map<String, Object> registrar(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @RequestBody @Valid RegistrarProyectoRequest request) {
        return facade.registrarProyecto(usuario, request);
    }

    @PutMapping("/{idProyecto}")
    @RequireRole({Role.ADMIN})
    public Map<String, Object> actualizar(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProyecto,
            @RequestBody @Valid RegistrarProyectoRequest request) {
        return facade.actualizarProyecto(usuario, idProyecto, request);
    }

    @DeleteMapping("/{idProyecto}")
    @RequireRole({Role.ADMIN})
    public Map<String, Object> eliminar(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProyecto) {
        return facade.eliminarProyecto(usuario, idProyecto);
    }

    @PostMapping("/{idProyecto}/prorroga")
    @RequireRole({Role.ADMIN, Role.SECRETARIA})
    public Map<String, Object> prorroga(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProyecto,
            @RequestBody @Valid ProrrogaRequest request) {
        return facade.registrarProrroga(usuario, idProyecto, request);
    }

    @PostMapping("/{idProyecto}/cambio-director")
    @RequireRole({Role.ADMIN})
    public Map<String, Object> cambioDirector(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProyecto,
            @RequestBody @Valid CambioDirectorRequest request) {
        return facade.cambiarDirector(usuario, idProyecto, request);
    }

    @PostMapping("/{idProyecto}/relevo")
    @RequireRole({Role.ADMIN})
    public Map<String, Object> relevo(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProyecto,
            @RequestBody @Valid RelevoRequest request) {
        return facade.relevarDireccion(usuario, idProyecto, request);
    }

    @PostMapping("/{idProyecto}/desvincular-cierre")
    @RequireRole({Role.ADMIN})
    public Map<String, Object> desvincularCierre(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProyecto) {
        return facade.desvincularCierre(usuario, idProyecto);
    }

    @PostMapping("/{idProyecto}/cerrar")
    @RequireRole({Role.ADMIN})
    public Map<String, Object> cerrarProyecto(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProyecto,
            @RequestBody @Valid CerrarProyectoRequest request) {
        return facade.cerrarProyecto(usuario, idProyecto, request);
    }

    @PostMapping("/{idProyecto}/equipo")
    @RequireRole({Role.ADMIN})
    public Map<String, Object> asignarProfesor(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProyecto,
            @RequestBody @Valid AsignarProfesorRequest request) {
        return facade.asignarProfesor(usuario, idProyecto, request);
    }

    @PostMapping("/{idProyecto}/estudiantes")
    @RequireRole({Role.ADMIN})
    public Map<String, Object> registrarEstudiante(
            @org.springframework.web.bind.annotation.SessionAttribute(SessionKeys.USER) SessionUser usuario,
            @PathVariable Integer idProyecto,
            @RequestBody @Valid RegistrarEstudianteRequest request) {
        return facade.registrarEstudiante(usuario, idProyecto, request);
    }
}
