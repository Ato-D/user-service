package com.stlghana.admin_service.restcontroller;

import com.stlghana.admin_service.dto.PermissionDTO;
import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.RolePermissionDTO;
import com.stlghana.admin_service.service.PermissionService;
import com.stlghana.admin_service.service.SetupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.stlghana.admin_service.config.SecurityConfig.CONTEXT_PATH;

@RestController
@RequestMapping(CONTEXT_PATH + "/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permission Endpoints", description = "This contains all endpoints that are used to " +
        "interact with the permission model")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Handles a GET request to retrieve all permission.
     *
     * @param params The parameters for the list of permission categories to retrieve like pagination,filters etc.
     * @return ResponseEntity containing the ResponseRecord with the list of permission per params.
     */
    @GetMapping
    ResponseRecord findAllPermissions(@RequestParam(defaultValue = "{}") Map<String, String> params) {
        return permissionService.findAll(params).getBody();
    }

    /**
     * Handles a GET request to retrieve a permission by its ID.
     *
     * @param id The ID of the permission to retrieve.
     * @return ResponseEntity containing the ResponseRecord with the requested permission.
     */
    @GetMapping("/{id}")
    public ResponseRecord findPermissionById(@PathVariable UUID id){
        return permissionService.findById(id).getBody();
    }

    /**
     * Handles a POST request to create a new permission.
     *
     * @param permissionDTO The PermissionDTO containing information of the new permission.
     * @return ResponseEntity containing the ResponseRecord with the created permission.
     */
    @PostMapping
    public ResponseRecord savePermissionCategory(@Valid @RequestBody PermissionDTO permissionDTO){
        return permissionService.upsert(permissionDTO,null).getBody();
    }

    /**
     * Handles a PUT request to update an existing permission.
     *
     * @param permissionDTO The updated PermissionDTO containing information to update permission.
     * @param id The ID of the permission to be updated.
     * @param enabled The enabled state of the permission category to be updated.
     * @return ResponseEntity containing the ResponseRecord with the updated permission.
     */
    @PutMapping("/{id}")
    public ResponseRecord updatePermission(@Valid @RequestBody PermissionDTO permissionDTO, @PathVariable(name = "id") UUID id,
                                           @RequestParam (name = "enabled", defaultValue = "true") boolean enabled){
        permissionDTO.setEnabled(enabled);
        return permissionService.upsert(permissionDTO,id).getBody();
    }

        /**
         * Handles a POST request to create a new role permission.
         *
         * @param rolePermissionDTO The PermissionDTO containing information of the new role permission.
         * @return ResponseEntity containing the ResponseRecord with the created role permission.
         */
    @PostMapping("/role-permissions")
    public ResponseRecord saveRolePermission(@Valid @RequestBody RolePermissionDTO rolePermissionDTO){
        return permissionService.saveRolePermission(rolePermissionDTO).getBody();
    }


    /**
     * Handles a POST request to assign permissions to a role.
     *
     * @param roleId       The ID of the role to which the permissions will be assigned.
     * @param permissionIds A list of Permission IDs to be assigned to the specified role.
     * @return ResponseEntity containing the ResponseRecord with the result of the permission assignment.
     */
    @PostMapping("/{roleId}/permissions")
    public ResponseRecord assignPermissionsToRole(
            @PathVariable UUID roleId,
            @RequestBody List<UUID> permissionIds) {

        log.info("Assigning permissions to role with ID: {}", roleId);
        return permissionService.assignPermissionsToRole(roleId, permissionIds).getBody();
    }
}
