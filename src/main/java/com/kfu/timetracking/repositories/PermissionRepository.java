package com.kfu.timetracking.repositories;

import com.kfu.timetracking.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByResourceAndOperation(String resource, String operation);
}
