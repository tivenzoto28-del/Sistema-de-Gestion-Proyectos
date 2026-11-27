package com.example.Investigacion.security;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.Investigacion.common.ApiException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
/*
 * BLOQUE: Seguridad
 * Verifica sesión y rol antes de llamar al controlador; devuelve 401 o 403.
 */
public class RoleInterceptor implements HandlerInterceptor {

    /*
     * Control de acceso
     * Lee la sesión, compara los roles de la anotación y responde 401 o 403 antes de entrar al controlador.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        SessionUser usuario = session == null
                ? null
                : (SessionUser) session.getAttribute(SessionKeys.USER);

        if (usuario == null) {
            throw new ApiException(401, "Debe iniciar sesion para acceder a este recurso.");
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if (requireRole != null
                && Arrays.stream(requireRole.value()).noneMatch(rol -> rol == usuario.rol())) {
            throw new ApiException(403,
                    "El rol " + usuario.rolEtiqueta() + " no tiene permiso para esta operacion.");
        }

        return true;
    }
}
