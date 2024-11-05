package com.stlghana.admin_service.service;

import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.RoleDTO;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

public interface RoleService {

    ResponseEntity<ResponseRecord> findAll(Map<String, String> params);

    ResponseEntity<ResponseRecord> findById(UUID id);

    ResponseEntity<ResponseRecord> upsert(RoleDTO roleDTO, UUID id);
}
