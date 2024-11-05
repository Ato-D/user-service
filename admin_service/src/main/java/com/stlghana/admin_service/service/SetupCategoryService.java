package com.stlghana.admin_service.service;

import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.SetupCategoryDTO;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

public interface SetupCategoryService {

    ResponseEntity<ResponseRecord> findAll(Map<String, String> params);

    ResponseEntity<ResponseRecord> findById(UUID id);

    ResponseEntity<ResponseRecord> upsert(SetupCategoryDTO setupCategoryDTO, UUID id);
}
