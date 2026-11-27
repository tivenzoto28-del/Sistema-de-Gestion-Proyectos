package com.example.Investigacion.auth;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Investigacion.dto.LoginRequest;
import com.example.Investigacion.security.SessionKeys;
import com.example.Investigacion.security.SessionUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
/*
 * BLOQUE: Sesión
 * Expone login, logout, consulta de sesión y health check sin devolver contraseñas.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        SessionUser usuario = authService.autenticar(request.usuario(), request.password());

        HttpSession sesionAnterior = httpRequest.getSession(false);
        if (sesionAnterior != null) {
            sesionAnterior.invalidate();
        }
        HttpSession sesion = httpRequest.getSession(true);
        sesion.setAttribute(SessionKeys.USER, usuario);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("usuario", usuario.usuario());
        respuesta.put("rol", usuario.rol().name());
        respuesta.put("rolEtiqueta", usuario.rolEtiqueta());
        respuesta.put("nombrePersona", usuario.nombrePersona());
        respuesta.put("idProfesor", usuario.idProfesor());
        respuesta.put("idEstudiante", usuario.idEstudiante());
        respuesta.put("mensaje", "Sesion iniciada correctamente.");
        return respuesta;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        return Map.of("mensaje", "Sesion finalizada.");
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession sesion) {
        SessionUser usuario = (SessionUser) sesion.getAttribute(SessionKeys.USER);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("usuario", usuario.usuario());
        respuesta.put("rol", usuario.rol().name());
        respuesta.put("rolEtiqueta", usuario.rolEtiqueta());
        respuesta.put("nombrePersona", usuario.nombrePersona());
        respuesta.put("idProfesor", usuario.idProfesor());
        respuesta.put("idEstudiante", usuario.idEstudiante());
        return respuesta;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("estado", "OK", "aplicacion", "Gestion de Investigacion");
    }
}
