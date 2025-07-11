package com.microservice.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;

    // Nueva relación Many-to-Many con permisos
    @ManyToMany(fetch = FetchType.EAGER) // FetchType.EAGER para cargar permisos junto con el rol
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    // Constructor para facilidad si solo quieres el nombre del rol
    public Role(Long id, ERole name) {
        this.id = id;
        this.name = name;
    }
}