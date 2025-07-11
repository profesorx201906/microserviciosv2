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
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/student/all").hasAnyRole("MODERATOR", "ADMIN")
                        .requestMatchers("/api/student/create").hasRole("ADMIN")
                        .requestMatchers("/api/student/search-by-course/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/student/search/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/student/search/**").hasAuthority("READ_STUDENT")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/student/{id}")
                        .hasAuthority("UPDATE_STUDENT") // Regla para PUT
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/student/{id}")
                        .hasAuthority("DELETE_STUDENT") // Regla para DELETE
                        .anyRequest().authenticated());

        http.addFilterBefore(new GatewayAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}