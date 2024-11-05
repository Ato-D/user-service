package com.stlghana.admin_service.restcontroller;

import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.SetupCategoryDTO;
import com.stlghana.admin_service.service.SetupCategoryService;
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
@RequestMapping(CONTEXT_PATH + "/setup-categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Setup Category Endpoints", description = "This contains all endpoints that are used to " +
        "interact with the setup category model")
public class SetupCategoryController {

    private final SetupCategoryService setupCategoryService;

    /**
     * Handles a GET request to retrieve all setup categories.
     *
     * @param params The parameters for the list of setup categories to retrieve like pagination,filters etc.
     * @return ResponseEntity containing the ResponseRecord with the list of setup categories per params.
     */
    @GetMapping
    ResponseRecord findAllSetupCategories(@RequestParam(defaultValue = "{}") Map<String, String> params) {
        return setupCategoryService.findAll(params).getBody();
    }

    /**
     * Handles a GET request to retrieve a setup category by its ID.
     *
     * @param id The ID of the setup category to retrieve.
     * @return ResponseEntity containing the ResponseRecord with the requested setup category.
     */
    @GetMapping("/{id}")
    public ResponseRecord findSetupCategoryById(@PathVariable UUID id) {
        return setupCategoryService.findById(id).getBody();
    }

    /**
     * Handles a POST request to create a new setup category.
     *
     * @param setupCategoryDTO The SetupCategoryDTO containing information of the new setup category.
     * @return ResponseEntity containing the ResponseRecord with the created setup category.
     */
    @PostMapping
    public ResponseRecord saveSetupCategory(@Valid @RequestBody SetupCategoryDTO setupCategoryDTO) {
        return setupCategoryService.upsert(setupCategoryDTO, null).getBody();
    }

    /**
     * Handles a PUT request to update an existing setup category.
     *
     * @param setupCategoryDTO The updated SetupCategoryDTO containing information to update setup category.
     * @param id               The ID of the setup category to be updated.
     * @param enabled          The enabled state of the setup category to be updated.
     * @return ResponseEntity containing the ResponseRecord with the updated setup category.
     */
    @PutMapping("/{id}")
    public ResponseRecord updateSetupCategory(@Valid @RequestBody SetupCategoryDTO setupCategoryDTO, @PathVariable(name = "id") UUID id,
                                              @RequestParam(name = "enabled", defaultValue = "true") boolean enabled) {
        setupCategoryDTO.setEnabled(enabled);
        return setupCategoryService.upsert(setupCategoryDTO, id).getBody();
    }

}
