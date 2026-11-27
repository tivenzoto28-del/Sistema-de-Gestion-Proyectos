package com.example.Investigacion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.Investigacion.security.RoleInterceptor;

@Configuration
/*
 * BLOQUE: Configuración web
 * Registra el interceptor de roles para /api/** excepto login y health.
 */
public class WebConfig implements WebMvcConfigurer {

    private final RoleInterceptor roleInterceptor;

    public WebConfig(RoleInterceptor roleInterceptor) {
        this.roleInterceptor = roleInterceptor;
    }

    /*
     * Registro del interceptor
     * Aplica la seguridad a /api/** excepto login y health.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/health");
    }
}
