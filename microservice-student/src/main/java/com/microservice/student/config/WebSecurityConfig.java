package com.microservice.student.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// REMUEVE esta anotación si no vas a usar @PreAuthorize
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.microservice.student.security.GatewayAuthFilter;

@Configuration
// Si remueves @EnableMethodSecurity, @PreAuthorize dejará de funcionar
// @EnableMethodSecurity
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
                .authorizeHttpRequests(auth -> auth
                        // Permite acceso a rutas públicas (si las tienes, el Gateway ya debería
                        // manejarlas)
                        // .requestMatchers("/api/public/**").permitAll()

                        // Configura las reglas de acceso por URL y rol
                        // Las rutas deben coincidir con las que llegan a este microservicio (después
                        // del Gateway)
                        .requestMatchers("/api/student/all").hasAnyRole("MODERATOR", "ADMIN") // Para
                                                                                              // /api/student/all
                        .requestMatchers("/api/student/search/**").hasAnyRole("USER", "MODERATOR", "ADMIN") // Para
                                                                                                            // /api/student/search/{id}
                                                                                                            // o
                                                                                                            // /api/student/search-by-course/{courseId}
                        .requestMatchers("/api/student/create").hasRole("ADMIN") // Ejemplo: Solo ADMIN puede crear

                        // Todas las demás solicitudes requieren algún tipo de autenticación
                        // Si no se especifica un rol, solo que esté autenticado (JWT válido)
                        .anyRequest().authenticated());

        // Añade tu filtro personalizado antes del filtro de autenticación de Spring
        // Security
        // para que lea los encabezados del Gateway y establezca el contexto de
        // seguridad.
        http.addFilterBefore(new GatewayAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}