package com.biblioteca.libros.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "security.api-key.enabled", havingValue = "true")
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${security.api-key.value:biblioteca-soa-2026}")
    private String apiKeyValida;

    // Rutas siempre públicas: el health check y la documentación no requieren autenticación
    private static final String[] RUTAS_PUBLICAS = {
        "/libros/health",
        "/actuator",
        "/swagger-ui",
        "/api-docs"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        String ruta = request.getRequestURI();

        for (String publica : RUTAS_PUBLICAS) {
            if (ruta.startsWith(publica)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String apiKey = request.getHeader("X-API-Key");

        if (apiKeyValida.equals(apiKey)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                  "error": "401 Unauthorized",
                  "mensaje": "API Key inválida o ausente",
                  "solucion": "Incluye el header  X-API-Key: biblioteca-soa-2026",
                  "concepto_soa": "En SOA el consumidor debe identificarse antes de usar el servicio"
                }""");
        }
    }
}
