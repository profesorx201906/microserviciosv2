package com.microservice.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource; // <-- Importa esto
import java.util.Arrays; // <-- Importa esto

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

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173")); // <-- Orígenes
                                                                                                       // permitidos de
                                                                                                       // tu frontend
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // <-- Métodos HTTP
                                                                                                // permitidos
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // <-- Encabezados permitidos
        corsConfig.setAllowCredentials(true); // Permite el envío de credenciales (cookies, encabezados de
                                              // autenticación)
        corsConfig.setMaxAge(3600L); // Tiempo en segundos que la preflight request puede ser cacheada

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Aplica esta configuración CORS a todas las rutas

        return new CorsWebFilter(source);
    }
}