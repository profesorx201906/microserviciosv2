package com.microservice.auth;

import com.microservice.auth.model.ERole;
import com.microservice.auth.model.Permission;
import com.microservice.auth.model.Role;
import com.microservice.auth.repository.PermissionRepository;
import com.microservice.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PermissionRepository permissionRepository; // Inyecta el nuevo repositorio

    @Override
    public void run(String... args) throws Exception {
        // Asegúrate de que los roles existan
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(null, ERole.ROLE_USER)));
        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseGet(() -> roleRepository.save(new Role(null, ERole.ROLE_MODERATOR)));
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, ERole.ROLE_ADMIN)));

        // Define y guarda permisos si no existen
        Permission readStudentPerm = permissionRepository.findByName("READ_STUDENT")
                .orElseGet(() -> permissionRepository.save(new Permission(null, "READ_STUDENT")));
        Permission createStudentPerm = permissionRepository.findByName("CREATE_STUDENT")
                .orElseGet(() -> permissionRepository.save(new Permission(null, "CREATE_STUDENT")));
        Permission deleteStudentPerm = permissionRepository.findByName("DELETE_STUDENT")
                .orElseGet(() -> permissionRepository.save(new Permission(null, "DELETE_STUDENT")));
        Permission updateStudentPerm = permissionRepository.findByName("UPDATE_STUDENT")
                .orElseGet(() -> permissionRepository.save(new Permission(null, "UPDATE_STUDENT")));
        // Puedes añadir más permisos según necesites

        // Asigna permisos a los roles
        Set<Permission> userPermissions = new HashSet<>();
        userPermissions.add(readStudentPerm); // USER solo puede leer

        Set<Permission> modPermissions = new HashSet<>(userPermissions); // MOD tiene todo lo de USER +
        modPermissions.add(updateStudentPerm); // MOD puede actualizar

        Set<Permission> adminPermissions = new HashSet<>(modPermissions); // ADMIN tiene todo lo de MOD +
        adminPermissions.add(createStudentPerm); // ADMIN puede crear
        adminPermissions.add(deleteStudentPerm); // ADMIN puede eliminar

        // Asigna los conjuntos de permisos a los roles y guarda (o actualiza)
        userRole.setPermissions(userPermissions);
        roleRepository.save(userRole);

        modRole.setPermissions(modPermissions);
        roleRepository.save(modRole);

        adminRole.setPermissions(adminPermissions);
        roleRepository.save(adminRole);
    }
}