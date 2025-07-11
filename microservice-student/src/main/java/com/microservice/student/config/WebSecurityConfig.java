package com.microservice.student.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // Importa esto
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Importa esto

import com.microservice.student.security.GatewayAuthFilter;

@Configuration
@EnableMethodSecurity // Habilita las anotaciones @PreAuthorize y @PostAuthorize
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Deshabilita CSRF (ya que es stateless con JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Asegura
                                                                                                              // que no
                                                                                                              // se
                                                                                                              // creen
                                                                                                              // sesiones
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // Permite todas las solicitudes, el
                                                                              // Gateway ya las autenticó. La
                                                                              // autorización fina se hará con
                                                                              // @PreAuthorize.
        ;

        // Añade tu filtro personalizado antes del filtro de autenticación de Spring
        // Security
        // para que lea los encabezados del Gateway y establezca el contexto de
        // seguridad.
        http.addFilterBefore(new GatewayAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}