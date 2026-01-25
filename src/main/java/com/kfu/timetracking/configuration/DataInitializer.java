package com.kfu.timetracking.configuration;

import com.kfu.timetracking.models.Permission;
import com.kfu.timetracking.models.Role;
import com.kfu.timetracking.repositories.PermissionRepository;
import com.kfu.timetracking.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("LOG: Начало инициализации прав доступа...");

        // 1. Создаем Permissions (Ресурсы и Операции)
        Permission studentsRead = createPermissionIfNotFound("STUDENTS", "READ");
        Permission studentsWrite = createPermissionIfNotFound("STUDENTS", "WRITE");
        
        Permission timeRead = createPermissionIfNotFound("TIME", "READ");
        Permission timeWrite = createPermissionIfNotFound("TIME", "WRITE");
        
        Permission predictionsRead = createPermissionIfNotFound("PREDICTIONS", "READ");

        // 2. Настраиваем роль ADMIN
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        Set<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(studentsRead);
        adminPermissions.add(studentsWrite);
        adminPermissions.add(timeRead);
        adminPermissions.add(timeWrite);
        adminPermissions.add(predictionsRead);

        updateRolePermissions(adminRole, adminPermissions);

        // 3. Настраиваем роль STUDENT
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_STUDENT")));

        Set<Permission> studentPermissions = new HashSet<>();
        studentPermissions.add(timeRead);
        studentPermissions.add(timeWrite);
        studentPermissions.add(predictionsRead);

        updateRolePermissions(studentRole, studentPermissions);
        
        System.out.println("LOG: Инициализация прав доступа завершена.");
    }

    private Permission createPermissionIfNotFound(String resource, String operation) {
        return permissionRepository.findByResourceAndOperation(resource, operation)
                .orElseGet(() -> permissionRepository.save(new Permission(resource, operation)));
    }

    private void updateRolePermissions(Role role, Set<Permission> newPermissions) {
        role.setPermissions(newPermissions);
        roleRepository.save(role);
    }
}
