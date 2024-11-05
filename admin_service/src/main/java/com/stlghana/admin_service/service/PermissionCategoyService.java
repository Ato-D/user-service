package com.stlghana.admin_service.service;

import com.stlghana.admin_service.dto.PermissionCategoryDTO;
import com.stlghana.admin_service.dto.ResponseRecord;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

public interface PermissionCategoyService {

    ResponseEntity<ResponseRecord> findAll(Map<String, String> params);

    ResponseEntity<ResponseRecord> findById(UUID id);

    ResponseEntity<ResponseRecord> upsert(PermissionCategoryDTO permissionCategoryDTO, UUID id);
}
