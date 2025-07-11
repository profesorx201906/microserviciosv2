package com.microservice.gateway.filter;

import com.microservice.gateway.security.jwt.JwtUtils;
import com.microservice.gateway.services.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl; // ¡Añade esta importación!
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

            if (request.getURI().getPath().startsWith("/api/auth/")) {
                return chain.filter(exchange);
            }

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

            try {
                Claims claims = jwtUtils.getAllClaimsFromToken(jwt);
                String username = claims.getSubject();

                List<String> authoritiesList;
                Object authoritiesClaim = claims.get("authorities"); // <-- Debuggea aquí
                if (authoritiesClaim instanceof List<?>) {
                    authoritiesList = ((List<?>) authoritiesClaim).stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                } else {
                    authoritiesList = new ArrayList<>();
                    logger.warn("JWT 'authorities' claim is not a List or is missing. User: {}", username);
                    // Aquí deberías añadir un log o incluso lanzar una excepción para ver qué valor
                    // tiene 'authoritiesClaim'
                    logger.error("Value of 'authoritiesClaim': {}", authoritiesClaim);
                }

                UserDetailsImpl userDetails = UserDetailsImpl.buildFromJwtClaims(username, authoritiesList);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // --- CAMBIO AQUÍ ---
                // Para WebFlux, crea un SecurityContextImpl y luego usa contextWrite
                SecurityContext securityContext = new SecurityContextImpl(authentication);
                // --- FIN DEL CAMBIO ---

                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-ID", username)
                        .header("X-User-Authorities", String.join(",", authoritiesList))
                        .build();

                // Aquí es donde el contexto de seguridad reactivo se propaga
                return chain.filter(exchange.mutate().request(mutatedRequest).build())
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));

            } catch (Exception e) {
                logger.error("Error processing JWT: {}", e.getMessage());
                return this.onError(exchange, "Error processing JWT", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String responseBody = "{\"status\": " + httpStatus.value() + ", \"error\": \"" + err + "\"}";
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody.getBytes())));
    }

    public static class Config {
        // Puedes agregar propiedades de configuración si las necesitas
    }
}