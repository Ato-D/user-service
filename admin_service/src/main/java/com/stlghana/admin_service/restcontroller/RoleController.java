package com.stlghana.admin_service.restcontroller;

import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.RoleDTO;
import com.stlghana.admin_service.service.RoleService;
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
@RequestMapping(CONTEXT_PATH + "/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Endpoints", description = "This contains all endpoints that are used to interact with the role model")
public class RoleController {

    private final RoleService roleService;

    /**
     * Handles a GET request to retrieve all roles.
     *
     * @param params The parameters for the list of roles to retrieve like pagination,filters etc.
     * @return ResponseEntity containing the ResponseRecord with the list of roles per params.
     */
    @GetMapping
    ResponseRecord findAllRoles(@RequestParam(defaultValue = "{}") Map<String, String> params) {
        return roleService.findAll(params).getBody();
    }

    /**
     * Handles a GET request to retrieve a role by its ID.
     *
     * @param id The ID of the role to retrieve.
     * @return ResponseEntity containing the ResponseRecord with the requested role.
     */
    @GetMapping("/{id}")
    public ResponseRecord findRoleById(@PathVariable UUID id){
        return roleService.findById(id).getBody();
    }

    /**
     * Handles a POST request to create a new role.
     *
     * @param roleDTO The RoleDTO containing information of the new role.
     * @return ResponseEntity containing the ResponseRecord with the created role.
     */
    @PostMapping
    public ResponseRecord saveRole(@Valid @RequestBody RoleDTO roleDTO){
        return roleService.upsert(roleDTO,null).getBody();
    }

    /**
     * Handles a PUT request to update an existing role.
     *
     * @param roleDTO The updated RoleDTO containing information to update role.
     * @param id The ID of the role to be updated.
     * @return ResponseEntity containing the ResponseRecord with the updated role.
     */
    @PutMapping("/{id}")
    public ResponseRecord updateRole(@Valid @RequestBody RoleDTO roleDTO, @PathVariable(name = "id") UUID id,
                                     @RequestParam (name = "enabled", defaultValue = "true") boolean enabled) {
        roleDTO.setId(id);
        roleDTO.setEnabled(enabled);
        return roleService.upsert(roleDTO,id).getBody();
    }

}
