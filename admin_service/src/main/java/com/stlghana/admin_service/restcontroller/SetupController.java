package com.stlghana.admin_service.restcontroller;

import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.SetupDTO;
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
@RequestMapping(CONTEXT_PATH + "/setups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Setup Endpoints", description = "This contains all endpoints that are used to " +
        "interact with the setup model")
public class SetupController {

    private final SetupService setupService;


    /**
     * Handles a GET request to retrieve all setups.
     *
     * @param params The parameters for the list of setups to retrieve like pagination,filters etc.
     * @return ResponseEntity containing the ResponseRecord with the list of setups per params.
     */
    @GetMapping
    ResponseRecord findAllSetup(@RequestParam(defaultValue = "{}") Map<String, String> params) {
        return setupService.findAll(params).getBody();
    }

    /**
     * Handles a GET request to retrieve a setup by its ID.
     *
     * @param id The ID of the setup to retrieve.
     * @return ResponseEntity containing the ResponseRecord with the requested setup.
     */
    @GetMapping("/{id}")
    public ResponseRecord findSetupById(@PathVariable UUID id) {
        return setupService.findById(id).getBody();
    }

    /**
     * Handles a POST request to create a new setup.
     *
     * @param setupDTO The SetupDTO containing information of the new setup.
     * @return ResponseEntity containing the ResponseRecord with the created setup.
     */
    @PostMapping
    public ResponseRecord saveSetup(@Valid @RequestBody SetupDTO setupDTO) {
        return setupService.upsert(setupDTO, null).getBody();
    }

    /**
     * Handles a PUT request to update an existing setup.
     *
     * @param setupDTO The updated SetupDTO containing information to update setup.
     * @param id       The ID of the setup to be updated.
     * @param enabled  The enabled state of the setup to be updated.
     * @return ResponseEntity containing the ResponseRecord with the updated setup.
     */
    @PutMapping("/{id}")
    public ResponseRecord updateSetup(@Valid @RequestBody SetupDTO setupDTO, @PathVariable(name = "id") UUID id,
                                      @RequestParam(name = "enabled", defaultValue = "true") boolean enabled) {
        setupDTO.setEnabled(enabled);
        return setupService.upsert(setupDTO, id).getBody();
    }

}
