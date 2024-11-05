package com.stlghana.admin_service.restcontroller;

import com.stlghana.admin_service.dto.DepartmentDTO;
import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.service.DepartmentService;
import com.stlghana.admin_service.service.SetupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

import static com.stlghana.admin_service.config.SecurityConfig.CONTEXT_PATH;

@RestController
@RequestMapping(CONTEXT_PATH + "/departments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Department Endpoints", description = "This contains all endpoints that are used to " +
        "interact with the department model")


public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * Handles a GET request to retrieve all departments.
     *
     * @param params The parameters for the list of departments to retrieve like pagination,filters etc.
     * @return ResponseEntity containing the ResponseRecord with the list of departments per params.
     */
    @GetMapping
    ResponseRecord findAllDepartments(@RequestParam(defaultValue = "{}") Map<String, String> params) {
        return departmentService.findAll(params).getBody();
    }

    /**
     * Handles a GET request to retrieve a department by its ID.
     *
     * @param id The ID of the department to retrieve.
     * @return ResponseEntity containing the ResponseRecord with the requested department.
     */
    @GetMapping("/{id}")
    public ResponseRecord findDepartmentById(@PathVariable UUID id){
        return departmentService.findById(id).getBody();
    }

    /**
     * Handles a POST request to create a new department.
     *
     * @param departmentDTO The DepartmentDTO containing information of the new department.
     * @return ResponseEntity containing the ResponseRecord with the created department.
     */
    @PostMapping
    public ResponseRecord saveDepartment(@Valid @RequestBody DepartmentDTO departmentDTO){
        return departmentService.upsert(departmentDTO,null).getBody();
    }

    /**
     * Handles a PUT request to update an existing department.
     *
     * @param departmentDTO The updated DepartmentDTO containing information to update department.
     * @param id The ID of the department to be updated.
     * @param enabled The enabled state of the department to be updated.
     * @return ResponseEntity containing the ResponseRecord with the updated department.
     */
    @PutMapping("/{id}")
    public ResponseRecord updateDepartment(@Valid @RequestBody DepartmentDTO departmentDTO, @PathVariable(name = "id") UUID id,
                                           @RequestParam (name = "enabled", defaultValue = "true") boolean enabled){
        departmentDTO.setEnabled(enabled);
        return departmentService.upsert(departmentDTO,id).getBody();
    }


}
