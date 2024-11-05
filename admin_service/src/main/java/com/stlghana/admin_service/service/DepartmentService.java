package com.stlghana.admin_service.service;

import com.stlghana.admin_service.dto.DepartmentDTO;
import com.stlghana.admin_service.dto.ResponseRecord;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

public interface DepartmentService {

    ResponseEntity<ResponseRecord> findAll(Map<String, String> params);

    ResponseEntity<ResponseRecord> findById(UUID id);

    ResponseEntity<ResponseRecord> upsert(DepartmentDTO departmentDTO, UUID id);
}
