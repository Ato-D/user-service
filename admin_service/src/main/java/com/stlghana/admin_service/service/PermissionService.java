package com.stlghana.admin_service.service;

import com.stlghana.admin_service.dto.PermissionDTO;
import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.RolePermissionDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PermissionService {

    ResponseEntity<ResponseRecord> findAll(Map<String, String> params);

    ResponseEntity<ResponseRecord> findById(UUID id);

    ResponseEntity<ResponseRecord> upsert(PermissionDTO permissionDTO, UUID id);

    ResponseEntity<ResponseRecord> saveRolePermission(RolePermissionDTO rolePermissionDTO);

    ResponseEntity<ResponseRecord> assignPermissionsToRole(UUID roleId, List<UUID> permissionIds);



}
