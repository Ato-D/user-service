package com.stlghana.admin_service.restcontroller;

import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.UserDTO;
import com.stlghana.admin_service.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

import static com.stlghana.admin_service.config.SecurityConfig.CONTEXT_PATH;

@RestController
@RequestMapping(CONTEXT_PATH + "/users")
@AllArgsConstructor
@Slf4j
@Tag(name = "User Endpoints", description = "This contains all endpoints that are used to interact with the user model")
public class UserController {

    private final UserService userService;

    @GetMapping
    ResponseRecord findAllUsers(@RequestParam(defaultValue = "{}") Map<String, String> params) {
        return userService.findAll(params).getBody();
    }


    /**
     * Handles a GET request to retrieve a user by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @return ResponseEntity containing the ResponseDTO with the requested user.
     */
    @GetMapping("/{id}")
    public ResponseRecord findUserById(@PathVariable UUID id){
        return userService.findById(id).getBody();
    }

    /**
     * Handles a GET request to create a new user from Active Directory.
     * This displays a list of users from keycloak matching the search param
     *
     * @param search The search string.
     * @return ResponseEntity containing the ResponseDTO with the created user.
     */
    @GetMapping("/active-directory")
    public ResponseRecord findActiveDirectoryUser(@RequestParam String search){
        return userService.findBySearchParamFromActiveDirectory(search).getBody();
    }

    /**
     * Handles a POST request to create a new user.
     *
     * @param userDTO The UserDTO containing information of the new user.
     * @return ResponseEntity containing the ResponseDTO with the created user.
     */
    @PostMapping
    public ResponseRecord saveUser(@Valid @RequestBody UserDTO userDTO){
        return userService.save(userDTO).getBody();
    }

    /**
     * Handles a PUT request to update an existing user.
     *
     * @param userDTO The updated UserDTO.
     * @param id The ID of the user to be updated.
     * @return ResponseEntity containing the ResponseDTO with the updated user.
     *
     * @apiNote username cannot be edited
     */
    @PutMapping("/{id}")
    public ResponseRecord updateUser(@RequestBody UserDTO userDTO, @PathVariable(name = "id") UUID id,
                                     @RequestParam(name = "enable", defaultValue = "true") boolean enabled){
        userDTO.setEnabled(enabled);
        return userService.update(userDTO,id).getBody();
    }

    /**
     * Handles a PUT request to disable a user.
     *
     * @param userDTO The UserDTO with updated status.
     * @param id The ID of the user to be disabled.
     * @return ResponseEntity containing the ResponseDTO.
     */
    @PutMapping("/enable/{id}")
    public ResponseRecord disableUser(@RequestBody UserDTO userDTO, @PathVariable(name = "id") UUID id){
        return userService.disable(userDTO, id).getBody();
    }
}
