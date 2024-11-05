package com.stlghana.admin_service.serviceImpl;

import com.stlghana.admin_service.dto.*;
import com.stlghana.admin_service.model.DepartmentModel;
import com.stlghana.admin_service.model.PermissionModel;
import com.stlghana.admin_service.model.RoleModel;
import com.stlghana.admin_service.model.UserModel;
import com.stlghana.admin_service.model.views.UserView;
import com.stlghana.admin_service.record.UserRecord;
import com.stlghana.admin_service.repository.*;
import com.stlghana.admin_service.service.UserService;
import com.stlghana.admin_service.service.external.KeycloakService;
import com.stlghana.admin_service.utility.Pagination;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.stlghana.admin_service.utility.AppUtils.*;


@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final ModelMapper modelMapper;
    private final KeycloakService keycloakService;
    private final UserViewRepository userViewRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    @Override
    public ResponseEntity<ResponseRecord> findAll(Map<String, String> params) {
        log.info(getAuthenticatedUserName() + " inside find All Users :::  " +
                "Trying to fetch users per given params -> {}",params);
        ResponseRecord response;

        String searchValue = params != null ? params.getOrDefault("search","")
                : "";

        UUID[] roleIds = extractUUIDsFromParams(params, "roleIds").toArray(new UUID[0]);
        var roles = getUserRoles();
        boolean isAdmin = hasAdminRole(roles);
        boolean isManager = hasManagerRole(roles);

        UUID userId = getAuthenticatedUserId();

        try{

            if (params == null || params.getOrDefault("paginate", "false").equalsIgnoreCase("false")) {
                List<UserView> res = null;
                if (isAdmin) {
                    res = userViewRepository.findAllUsersByFirstNameOrLastName(searchValue);
                } else if (isManager) {
                    userViewRepository.findAllUsersByManagerIdAndFirstNameOrLastName(searchValue,userId);
                }

                if (isNotNullOrEmpty(res)) {
                    var userList = res.parallelStream().map(UserView::toList);
                    log.info("Success!, statusCode -> {} and Message -> {}", HttpStatus.OK, userList);
                    response = getResponseRecord("Successfully retrieved records!", HttpStatus.OK, userList);
                } else {
                    log.info("Success! and statusCode -> {} ", HttpStatus.OK);
                    response = getResponseRecord("Successfully retrieved records!", HttpStatus.OK);
                }
                return response.toResponseEntity();
            }  else {
                Pageable pageable = getPageRequest(params);
                Page<UserView> res = null;

                if (isAdmin) {
                    res = userViewRepository.findAllUsersByFirstNameOrLastName(searchValue, pageable);
                } else if (isManager) {
                    userViewRepository.findAllUsersByManagerIdAndFirstNameOrLastName(searchValue, userId, pageable);
                }

                if (isNotNullOrEmpty(res)) {
                    Page<UserRecord> userRecords = res.map(UserView::toList);
                    Pagination pagination = mapToPagination(userRecords);

                    log.info("Success!, statusCode -> {} and Message -> {}", HttpStatus.OK, pagination);
                    response = getResponseRecord("Successfully retrieved records!", HttpStatus.OK, pagination);
                } else {
                    log.info("Success! and statusCode -> {} ", HttpStatus.OK);
                    response = getResponseRecord("Successfully retrieved records!", HttpStatus.OK);
                }
            }
        }
    catch (ResponseStatusException e) {
        log.error("Exception Occurred! StatusCode -> {} and Message -> {} and Cause -> {}", e.getStatusCode(), e.getMessage(),e.getReason());
        response = getResponseRecord(e.getReason(), HttpStatus.valueOf(e.getStatusCode().value()));
    } catch (Exception e){
        log.error("Exception Occurred! StatusCode -> {} and Cause -> {} and Message -> {}", 500, e.getCause(),e.getMessage());
        response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
        return response.toResponseEntity();
}

    @Override
    public ResponseEntity<ResponseRecord> findById(UUID id) {
        log.info(getAuthenticatedUserName() + " inside find User by Id ::: " +
                "Trying to fetch user by id -> {}", id);
        ResponseRecord response;

        try{
            var record = userViewRepository.findById(id);
            if (record.isPresent()) {
                log.info("Success! Status Code -> {}, HTTP Response -> {}, Message -> {}", 200, HttpStatus.OK, record);
                var userview = record.get();
                var roles = roleRepository.findNamesByIds(userview.getRoles());
                userview.setUserRoles(roles.toString());

                response = getResponseRecord("Success!", HttpStatus.OK, userview);
                return response.toResponseEntity();
            }
            log.info("Not Found! statusCode -> {}, Cause -> {}, Message -> {}", 204, HttpStatus.NO_CONTENT, "Record Not Found");
            response = getResponseRecord("No Record Found", HttpStatus.NOT_FOUND);
            return response.toResponseEntity();
        }
        catch (Exception e) {
            log.error("Exception Occurred! statusCode -> {}, Cause -> {}, Message -> {}", 500, e.getCause(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e.getCause());
        }
    }

    @Override
    public ResponseEntity<ResponseRecord> findBySearchParamFromActiveDirectory(String search) {
        log.info(getAuthenticatedUserName() + "inside search User from Active Directory " + "search param", search);

        ResponseRecord response;
        try {

            var activeDirectoryUserFromKeycloak = keycloakService.findBySearchParam(search);
            log.info("Active directory users from keycloak -> {}", activeDirectoryUserFromKeycloak);

            List<UserDTO> activeDirectoryUsers = activeDirectoryUserFromKeycloak.parallelStream()
                                            .map(item -> {
                                                var res = modelMapper.map(item, UserDTO.class);
                                                var record = userRepository.findById(item.getId());
                                                res.setExists(record.isPresent());

                                                var attributes = item.getAttributes();
                                                log.info("Attributes -> {}", attributes);
                                                if (attributes !=null && attributes.containsKey("department")) {
                                                    List<String> department = attributes.get("department");
                                                    if (!department.isEmpty()) {
                                                        res.setDepartmentId(UUID.fromString(department.get(0)));
                                                    }
                                                }
                                                return res;
                                            }).toList();
            log.info("Success! statusCode -> {} and Message -> {} ", HttpStatus.OK, activeDirectoryUsers);
            response = getResponseRecord("Users from active directory",HttpStatus.OK, activeDirectoryUsers);

        } catch (ResponseStatusException e) {
            log.error("Exception Occurred!, statusCode -> {} and Message -> {}", e.getReason(), e.getMessage());
            response = getResponseRecord(e.getMessage(),HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            log.error("Exception Occurred! statusCode -> {} and Cause -> {} and Message -> {}", 500, e.getCause(), e.getMessage());
            response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.toResponseEntity();
    }

    @Override
    public ResponseEntity<ResponseRecord> save(UserDTO userDTO) {
        log.info(getAuthenticatedUserName() + " Inside Save User ::: Trying to save a user");
        ResponseRecord response;

        var roles = getUserRoles();
        boolean isAdmin = hasAdminRole(roles);

        try {

//            UUID userId = getAuthenticatedUserId();


            if (!isAdmin) {
                response = getResponseRecord("No authorization to save user", HttpStatus.FORBIDDEN);
                return response.toResponseEntity();
            }  else {
                Set<RoleModel> userRoles = (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()
                        ? new HashSet<>() : userDTO.getRoles()
                        .stream()
                        .map(id -> roleRepository.findById(id)
                                .orElseThrow(() ->
                                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Role Record " + id + " does not exist")))
                        .collect(Collectors.toSet()));

                Set<PermissionModel> userPermissions = (userDTO.getPermissions() == null || userDTO.getPermissions().isEmpty())
                        ? new HashSet<>() : userDTO.getPermissions()
                        .stream()
                        .map(id -> permissionRepository.findById(id)
                                .orElseThrow(() ->
                                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Permission record " + id + " does not exist")))
                        .collect(Collectors.toSet());

                UserModel manager = isNotNullOrEmpty(userDTO.getManagerId())
                        ? findEntityById(userRepository, userDTO.getManagerId(), "User")
                        : null;

                DepartmentModel userDepartment = isNotNullOrEmpty(userDTO.getDepartmentId())
                        ? findEntityById(departmentRepository, userDTO.getDepartmentId(), "Department")
                        : null;

                if (!userDTO.isExists()) {
                    saveUserInKeycloak(userDTO);
                }

              var keycloakId = keycloakService.findByUserName(userDTO.getUserName());

                /**
                 * Updating user details in keycloak after saving user record
                 */

                if (isNotNullOrEmpty(keycloakId)) {
                    List<KeycloakRoleDTO> keycloakUserRoles = new ArrayList<>();
                    if (isNotNullOrEmpty(userRoles)) {
                        keycloakUserRoles = userRoles.stream()
                                .map(role -> modelMapper.map(role, KeycloakRoleDTO.class)).toList();
                    }
                    updateUserInKeycloak(userDTO, keycloakId, keycloakUserRoles);
                }

                var user = UserModel
                        .builder()
                        .id(keycloakId)
                        .userName(userDTO.getUserName())
                        .email(userDTO.getEmail())
                        .firstName(userDTO.getFirstName())
                        .lastName(userDTO.getLastName())
                        .phoneNumber(userDTO.getPhoneNumber())
                        .department(userDepartment)
                        .manager(manager)
                        .roles(userRoles)
                        .permissions(userPermissions)
                        .permissions(userPermissions)
                        .isEnabled(true)
                        .createdAt(ZonedDateTime.now())
                        .createdBy(getAuthenticatedUserId())
                        .updatedBy(getAuthenticatedUserId())
                        .updatedAt(ZonedDateTime.now())
                        .build();

                UserModel record;

                try{
                    record = userRepository.save(user);
                }  catch (DataIntegrityViolationException e) {
                    log.error("Exception Occurred! Message -> {} and Cause -> {}", e.getMostSpecificCause(), e.getMessage());
                    response = getResponseRecord(e.getMessage(), HttpStatus.BAD_REQUEST);
                    return response.toResponseEntity();
                }
                log.info("Success! statusCode -> {} and Message -> {}", HttpStatus.CREATED, record);
                response = getResponseRecord("Record saved successfully", HttpStatus.CREATED, record.toDTO());
            }
        } catch (ResponseStatusException e) {
            log.error("Exception Occurred!, statusCode -> {} and Message -> {}", e.getStatusCode(), e.getReason());
            response = getResponseRecord(e.getReason(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            log.error("Exception Occurred!, statusCode -> {} and Cause -> {} and Message -> {}", 500, e.getCause(), e.getMessage());
            response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.toResponseEntity();
    }

    @Override
    public ResponseEntity<ResponseRecord> update(UserDTO userDTO, UUID id) {
        log.info(getAuthenticatedUserName() + " Inside Update User :::::: Trying to update user");
        ResponseRecord response;

        var roles = getUserRoles();
        boolean isAdmin = hasAdminRole(roles);

        try {
                if (!isAdmin) {
                    response = getResponseRecord("No authorization to update users", HttpStatus.FORBIDDEN);
                    return response.toResponseEntity();
                } else {
                    Set<RoleModel> userRoles = (userDTO.getRoles() == null || userDTO.getRoles().isEmpty())
                            ? new HashSet<>() : userDTO.getRoles()
                            .stream()
                            .map(roleId -> roleRepository.findById(id)
                                    .orElseThrow(() ->
                                            new ResponseStatusException(HttpStatus.NOT_FOUND, " Role Record " + id + " does not exist")))
                            .collect(Collectors.toSet());

                    UserModel manager = isNotNullOrEmpty(userDTO.getManagerId())
                            ? findEntityById(userRepository, userDTO.getManagerId(), "User")
                            : null;

                    DepartmentModel userDepartment = isNotNullOrEmpty(userDTO.getDepartmentId())
                            ? findEntityById(departmentRepository, userDTO.getDepartmentId(), "Department")
                            : null;


                    List<KeycloakRoleDTO> keycloakUserRoles = new ArrayList<>();
                    if (isNotNullOrEmpty(userRoles)) {
                        keycloakUserRoles = userRoles.stream().map(role ->
                                modelMapper.map(role, KeycloakRoleDTO.class)).toList();
                    }

                    updateUserInKeycloak(userDTO, id, keycloakUserRoles);

                    UserModel existingUser = findEntityById(userRepository, id, "User");

                    existingUser.setRoles(userRoles);
                    existingUser.setManager(manager);
                    existingUser.setDepartment(userDepartment);
                    existingUser.setUpdatedAt(ZonedDateTime.now());
                    existingUser.setUpdatedBy(getAuthenticatedUserId());

                    UserModel record = userRepository.save(existingUser);
                    log.info("Success!, statusCode -> {}, Message -> {}", HttpStatus.CREATED, record);
                    response = getResponseRecord("Record updated successfully", HttpStatus.ACCEPTED, record.toDTO());
                }
            } catch (ResponseStatusException e) {
                log.error("Exception Occurred!, statusCode -> {} and Message -> {}",
                        e.getStatusCode(), e.getReason());
                response = getResponseRecord(e.getReason(), HttpStatus.valueOf(e.getStatusCode().value()));
            }
        catch (Exception e) {
                log.error("Exception Occurred!, statusCode -> {} and Cause -> {} and Message -> {}",
                        500, e.getCause(), e.getMessage());
                response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return response.toResponseEntity();
        }



    @Override
    public ResponseEntity<ResponseRecord> disable(UserDTO userDTO, UUID id) {
        log.info(getAuthenticatedUserName() + " Inside disable User ::: Trying to disable user -> {}",userDTO);
        ResponseRecord response;

        try{
            disableUserInKeycloak(userDTO, id);
            UserModel existingUser = findEntityById(userRepository, id, "User");

            existingUser.setIsEnabled(userDTO.isEnabled());
            existingUser.setUpdatedBy(getAuthenticatedUserId());
            existingUser.setUpdatedAt(ZonedDateTime.now());

            boolean isEnabled = userDTO.isEnabled();
            var record = userRepository.save(existingUser);

            log.info("Success! statusCode -> {} and Message -> {} ", HttpStatus.OK, record);

            response = isEnabled
                    ? getResponseRecord("User successfully enabled", HttpStatus.OK, record.toDTO())
                    : getResponseRecord("User successfully disabled", HttpStatus.OK, record.toDTO());
        } catch (ResponseStatusException e) {
            log.error("Error Occurred!, Message -> {}, Cause -> {}", e.getMessage(), e.getReason());
            response = getResponseRecord(e.getReason(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            log.error("Error Occurred!, statusCode -> {} and Cause -> {} and Message -> {}",
                    500, e.getCause(), e.getMessage());
            response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.toResponseEntity();
    }


    /**
     * Saves a new user in Keycloak.
     *
     * @param userDTO The UserDTO containing user information.
     */

    private void saveUserInKeycloak(UserDTO userDTO) {
        log.info("Inside the save keycloak User ::: Trying to save user in keycloak");
        Map<String, List> attributes = getUserAttributes(userDTO);
        var userRoles = roleRepository.findNamesByIds(userDTO.getRoles());
        var credentials = KeycloakCredentialsDTO
                .builder()
                .temporary(true)
                .type("password")
                .value(userDTO.getFirstName() + "@1234")
                .build();
        var body = KeycloakUserDTO
                .builder()
                .userName(userDTO.getUserName())
                .email(userDTO.getEmail())
                .emailVerified(true)
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .enabled(true)
                .attributes(attributes)
                .credentials(List.of(credentials))
                .build();

        var res = keycloakService.saveUser(body);

        if (res.getStatusCode().isError()) {
            throw new ResponseStatusException(HttpStatus.valueOf(res.getStatusCode().value()), "User not saved in keycloak");
        }
        log.info("Successfully saved user in kyecloak! statusCode -> {}", res.getStatusCode(), body);
    }


    /**
     * Updates an existing user in Keycloak.
     *
     * @param userDTO   The UserDTO containing updated user information.
     * @param id        The UUID of the user to update.
     */

    public void updateUserInKeycloak(UserDTO userDTO, UUID id, List<KeycloakRoleDTO> userRoles) {
        log.info("Inside Update Keycloak User ::: Trying to update user in keycloak");
        Map<String, List> attributes = getUserAttributes(userDTO);
        var body = KeycloakUserDTO
                .builder()
                .attributes(attributes)
                .enabled(userDTO.isEnabled())
                .emailVerified(true)
                .userRoles(userRoles)
                .build();
        var res = keycloakService.updateUser(id, body);

        if (res.getStatusCode().isError()){
            throw new ResponseStatusException(HttpStatus.valueOf(res.getStatusCode().value()),
            "User not updated in keycloak");
        }
        log.info("Successfully updated in keycloak! statusCode -> {} Updated user -> {}",
                res.getStatusCode(), body);
    }


    /**
     * Disables an existing user in Keycloak.
     *
     * @param userDTO   The UserDTO containing updated user information.
     * @param id        The UUID of the user to update.
     */
    public void disableUserInKeycloak(UserDTO userDTO, UUID id) {
        log.info("Inside Enable keycloak ::: Trying toi enable user in keycloak");
        var body = KeycloakUserDTO
                .builder()
                .enabled(userDTO.isEnabled())
                .emailVerified(true)
                .build();
        var res = keycloakService.disableUser(id, body);

        if (res.getStatusCode().isError()) {
            throw new ResponseStatusException(HttpStatus.valueOf(res.getStatusCode().value()),
                    "User not updated in keycloak");
        }
        log.info("Successfully enabled or disabled user in keycloak! statusCode -> {} " +
                "Updated user -> {}", res.getStatusCode(), body);

    }

    /**
     * Maps user attributes from the User DTO to keycloak user attributes
     *
     * @param userDTO The user body to extract company as attribute from
     * @return The Map of user attributes
     */

    private Map<String, List> getUserAttributes(UserDTO userDTO) {
        Map<String, List> attributes = new HashMap<>();

        if (isNotNullOrEmpty(userDTO.getDepartmentId())) {
            var department = new HashSet<>(Collections.singleton(userDTO.getDepartmentId()))
                    .stream()
                    .filter(departmentRepository::existsById)
                    .toList();

            if (!department.isEmpty()) {
                attributes.put("department", department);
            }

            if (isNotNullOrEmpty(userDTO.getManagerId())) {
                var manager = new HashSet<>(Collections.singleton(userDTO.getManagerId()))
                        .stream()
                        .filter(userRepository::existsById)
                        .toList();
                if (!manager.isEmpty()) {
                    attributes.put("manager", manager);
                }

            }
        }
        return attributes;
    }
}

