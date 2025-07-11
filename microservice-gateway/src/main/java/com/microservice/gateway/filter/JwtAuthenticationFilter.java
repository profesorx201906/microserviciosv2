package com.microservice.gateway.filter;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.microservice.gateway.security.jwt.JwtUtils;

import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Excluir rutas de autenticación (login, registro) del filtro JWT
            if (request.getURI().getPath().startsWith("/api/auth/")) {
                return chain.filter(exchange);
            }

            // Verificar si el encabezado de autorización está presente
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return this.onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }

            if (jwt == null || !jwtUtils.validateJwtToken(jwt)) {
                return this.onError(exchange, "Invalid or missing JWT token", HttpStatus.UNAUTHORIZED);
            }

            // Si el token es válido, extraer información y propagarla
            try {
                Claims claims = jwtUtils.getAllClaimsFromToken(jwt); // Necesitarás un método para esto en JwtUtils
                String username = claims.getSubject();

                // Asegúrate de que 'roles' se declare como List<String>
                // y que el cast sea explícito si es necesario, o usa la forma genérica de
                // .get()
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");
                // Propagar el User ID y Roles a los microservicios downstream
                // Propagar el User ID y Roles a los microservicios downstream
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-ID", username)
                        // LÍNEA A CORREGIR: Convierte la lista de roles en una sola cadena separada por
                        // comas
                        .header("X-User-Roles", String.join(",", roles))
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                logger.error("Error processing JWT: {}", e.getMessage());
                return this.onError(exchange, "Error processing JWT", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Puedes agregar propiedades de configuración si las necesitas
    }
}