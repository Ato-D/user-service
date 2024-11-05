package com.stlghana.admin_service.service;

import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.UserDTO;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

public interface UserService {

    ResponseEntity<ResponseRecord> findAll(Map<String, String> params);

    ResponseEntity<ResponseRecord> findById(UUID id);

    ResponseEntity<ResponseRecord> findBySearchParamFromActiveDirectory(String search);

    ResponseEntity<ResponseRecord> save(UserDTO userDTO);

    ResponseEntity<ResponseRecord> update(UserDTO userDTO, UUID id);

    ResponseEntity<ResponseRecord> disable(UserDTO userDTO, UUID id);
}
