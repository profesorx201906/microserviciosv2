package com.microservice.student.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component; // Agrega esto
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component // Marca como componente para que Spring lo pueda inyectar si es necesario,
           // aunque lo instanciemos en WebSecurityConfig
public class GatewayAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Lee los encabezados que el Gateway propagó
        String userId = request.getHeader("X-User-ID");
        String userRolesHeader = request.getHeader("X-User-Roles");

        if (userId != null && userRolesHeader != null && !userId.isEmpty() && !userRolesHeader.isEmpty()) {
            // Convierte la cadena de roles (ej. "ROLE_USER,ROLE_ADMIN") a una lista de
            // GrantedAuthority
            List<SimpleGrantedAuthority> authorities = Arrays.stream(userRolesHeader.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // Crea un objeto de autenticación con la información del usuario y sus roles
            // El 'null' es para la contraseña, ya que no la necesitamos aquí.
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
                    authorities);

            // Establece la autenticación en el contexto de seguridad de Spring
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continúa con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}