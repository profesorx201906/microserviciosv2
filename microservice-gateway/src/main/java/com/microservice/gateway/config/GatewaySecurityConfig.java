package com.microservice.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity // Para Spring Cloud Gateway que usa Spring WebFlux
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Deshabilita CSRF
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/**").permitAll() // Permite acceso público a rutas de autenticación
                        .anyExchange().permitAll() // Permite todas las demás solicitudes. El filtro
                                                   // JwtAuthenticationFilter se encargará de la seguridad.
                );
        return http.build();
    }
}