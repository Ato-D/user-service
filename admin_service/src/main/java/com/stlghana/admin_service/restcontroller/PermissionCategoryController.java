package com.stlghana.admin_service.restcontroller;

import com.stlghana.admin_service.dto.PermissionCategoryDTO;
import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.service.PermissionCategoyService;
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
@RequestMapping(CONTEXT_PATH + "/permission-categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permission Category Endpoints", description = "This contains all endpoints that are used to " +
        "interact with the permission category model")
public class PermissionCategoryController {

    private final PermissionCategoyService permissionCategoyService;

    /**
     * Handles a GET request to retrieve all permission categories.
     *
     * @param params The parameters for the list of permission categories to retrieve like pagination,filters etc.
     * @return ResponseEntity containing the ResponseRecord with the list of permission categories per params.
     */
    @GetMapping
    ResponseRecord findAllPermissionCategories(@RequestParam(defaultValue = "{}") Map<String, String> params) {
        return permissionCategoyService.findAll(params).getBody();
    }

    /**
     * Handles a GET request to retrieve a permission category by its ID.
     *
     * @param id The ID of the permission category to retrieve.
     * @return ResponseEntity containing the ResponseRecord with the requested permission category.
     */
    @GetMapping("/{id}")
    public ResponseRecord findPermissionCategoryById(@PathVariable UUID id){
        return permissionCategoyService.findById(id).getBody();
    }

    /**
     * Handles a POST request to create a new permission category.
     *
     * @param permissionCategoryDTO The PermissionCategoryDTO containing information of the new permission category.
     * @return ResponseEntity containing the ResponseRecord with the created permission category.
     */
    @PostMapping
    public ResponseRecord savePermissionCategory(@Valid @RequestBody PermissionCategoryDTO permissionCategoryDTO){
        return permissionCategoyService.upsert(permissionCategoryDTO,null).getBody();
    }

    /**
     * Handles a PUT request to update an existing permission category.
     *
     * @param permissionCategoryDTO The updated PermissionCategoryDTO containing information to update permission category.
     * @param id The ID of the permission category to be updated.
     * @param enabled The enabled state of the permission category to be updated.
     * @return ResponseEntity containing the ResponseRecord with the updated permission category.
     */
    @PutMapping("/{id}")
    public ResponseRecord updateSPermissionCategory(@Valid @RequestBody PermissionCategoryDTO permissionCategoryDTO, @PathVariable(name = "id") UUID id,
                                                    @RequestParam (name = "enabled", defaultValue = "true") boolean enabled){
        permissionCategoryDTO.setEnabled(enabled);
        return permissionCategoyService.upsert(permissionCategoryDTO,id).getBody();
    }


}
