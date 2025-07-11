package com.microservice.gateway.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
// import com.profesorx.auth.model.User; // ¡Elimina esta importación! Ya no la necesitamos

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Arrays; // Añade esta importación

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private String id; // Podría ser String si tu ID de usuario en el JWT es String
    private String username;
    private String email; // Opcional, si lo incluyes en el JWT

    @JsonIgnore
    private String password; // No se usa en el Gateway, puede ser null o vacío

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(String id, String username, String email, String password,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    // *** Este es el método CLAVE para el Gateway ***
    // Construye UserDetailsImpl a partir de los datos que extraes del JWT
    public static UserDetailsImpl buildFromJwtClaims(String username, List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // Puedes poner null o vacío para campos no disponibles/no necesarios en el
        // Gateway
        return new UserDetailsImpl(
                username, // Usamos el username como ID también si no tienes un ID específico en el JWT
                username,
                null, // No necesitas el email aquí a menos que lo extraigas del JWT
                null, // No necesitas la contraseña aquí
                authorities);
    }

    // Si tu JWT tiene un ID de usuario numérico/string y lo quieres usar
    public static UserDetailsImpl buildFromJwtClaims(String userId, String username, List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                userId,
                username,
                null,
                null,
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getId() { // Cambia el tipo de retorno a String si el ID es String
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}