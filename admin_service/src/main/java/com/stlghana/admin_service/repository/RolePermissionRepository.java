package com.stlghana.admin_service.repository;

import com.stlghana.admin_service.model.RolePermissionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermissionModel, UUID> {
}
