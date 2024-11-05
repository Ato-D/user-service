package com.stlghana.admin_service.serviceImpl;

import com.stlghana.admin_service.dto.KeycloakRoleDTO;
import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.RoleDTO;
import com.stlghana.admin_service.model.RoleModel;
import com.stlghana.admin_service.repository.RoleRepository;
import com.stlghana.admin_service.service.RoleService;
import com.stlghana.admin_service.service.external.KeycloakService;
import com.stlghana.admin_service.utility.Pagination;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.stlghana.admin_service.utility.AppUtils.*;


@Service
@AllArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final KeycloakService keycloakService;
    private final RoleRepository roleRepository;
    @Override
    public ResponseEntity<ResponseRecord> findAll(Map<String, String> params) {
        log.info(getAuthenticatedUserName() + " inside find All Roles :::  Trying to fetch roles" +
                " per given params -> {}",params);
        ResponseRecord response;

        try{
            var roles = getUserRoles();
            Boolean isAdmin = hasAdminRole(roles);
            String searchValue = params != null ? params.getOrDefault("search", "")
                    : "";
            if (params == null || params.getOrDefault("paginate", "false").equalsIgnoreCase("false")) {
                List<RoleModel> res = new ArrayList<>();
                res = isAdmin ? roleRepository.findAllByName(searchValue)
                        : new ArrayList<>();
                log.info("Success! Status Code -> {}, HTTP Response -> {}, Message -> {}", 200, HttpStatus.OK, res);
                response = getResponseRecord("Success!", HttpStatus.OK, res);
            }  else {
                Pageable pageable = getPageRequest(params);
                Page<RoleModel> res;
                res = isAdmin ? roleRepository.findAllByName(searchValue,pageable)
                        : new PageImpl<>(new ArrayList<>(),pageable,0);

                Page<RoleDTO> roleDTOs = res.map(RoleModel::toDTO);
                Pagination pagination = mapToPagination(roleDTOs);

                log.info("Success!, statusCode -> {} and Message -> {}", HttpStatus.OK, pagination);
                response = getResponseRecord("Success", HttpStatus.OK, pagination);
            }
        } catch (ResponseStatusException e) {
            log.error("Exception Occurred! StatusCode -> {} and Message -> {} and Cause -> {}",
                    e.getStatusCode(), e.getMessage(),e.getReason());
            response = getResponseRecord(e.getReason(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e){
            log.error("Exception Occurred! StatusCode -> {} and Cause -> {} and Message -> {}",
                    500, e.getCause(),e.getMessage());
            response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.toResponseEntity();
    }

    /**
     * Finds a role by its UUID.
     *
     * @param id         The UUID of the role to find.
     * @return           A ResponseEntity containing the response data.
     */
    @Override
    public ResponseEntity<ResponseRecord> findById(UUID id) {
        log.info(getAuthenticatedUserName() + " inside find Role by Id ::: Trying to find role by id -> {}", id);
        ResponseRecord response;

        try {
            var record = roleRepository.findById(id);
            if (record.isPresent()) {
                log.info("Success! StatusCode -> {} and Message -> {}", HttpStatus.OK,record);
                response = getResponseRecord("Successfully retrieved record by id " + id,
                        HttpStatus.OK, record.get().toDTO());
                return response.toResponseEntity();
            }
            log.info("Not Found! statusCode -> {} and Message -> {}", HttpStatus.NO_CONTENT, record);
            response = getResponseRecord("No Record Found!",HttpStatus.OK);

        } catch (ResponseStatusException e) {
            log.error("Exception Occurred!  Reason -> {} and Message -> {}",e.getReason(),e.getMessage());
            response = getResponseRecord(e.getMessage(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (Exception e) {
            log.error("Exception Occurred! statusCode -> {} and Cause -> {} and Message -> {}",
                    500, e.getCause(),e.getMessage());
            response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.toResponseEntity();
    }

    @Override
    public ResponseEntity<ResponseRecord> upsert(RoleDTO roleDTO, UUID id) {
        log.info(getAuthenticatedUserName() + " inside upsert Role ::: Trying to upsert role by object -> {}", roleDTO);
        ResponseRecord response;

        try{
            boolean isAdmin = hasAdminRole(getUserRoles());
            if (!isAdmin) {
                response = getResponseRecord("No authorization to upsert roles", HttpStatus.FORBIDDEN);
                return response.toResponseEntity();
            } else {
                RoleModel roleModel;
                /**
                 * Inserts or update a role record
                 */
                if (isNotNullOrEmpty(id)) {
                    updateRoleInKeycloak(roleDTO);
                } else {
                    saveRoleInKeycloak(roleDTO);
                }

                var keycloakId = keycloakService.findByRoleName(roleDTO.getName());

                roleModel = RoleModel
                        .builder()
                        .id(keycloakId)
                        .name(roleDTO.getName().toUpperCase())
                        .isEnabled(roleDTO.isEnabled())
                        .createdBy(getAuthenticatedUserId())
                        .createdAt(ZonedDateTime.now())
                        .updatedBy(getAuthenticatedUserId())
                        .updatedAt(ZonedDateTime.now())
                        .build();

                var record = roleRepository.save(roleModel);
                log.info("Success! statusCode -> {} and Message -> {}", id == null ? HttpStatus.CREATED
                        : HttpStatus.ACCEPTED, record.toDTO());
                response = getResponseRecord("Record " + (id == null ? "saved" : "updated") +
                        " successfully", id == null ? HttpStatus.CREATED : HttpStatus.ACCEPTED, record.toDTO());
                }
            } catch (ResponseStatusException e) {
                log.info("Error Occurred! statusCode -> {} and Message -> {} and Reason -> {}", e.getStatusCode(), e.getMessage(), e.getReason());
                response = getResponseRecord(e.getReason(), HttpStatus.valueOf(e.getStatusCode().value()));
            } catch (DataIntegrityViolationException e) {
                log.error("Exception Occurred! Message -> {} and Cause -> {}", e.getMostSpecificCause(), e.getMessage());
                response = getResponseRecord(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                log.error("Exception Occurred! statusCode -> {} and Cause -> {} and Message -> {}", 500, e.getCause(), e.getMessage());
                response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return response.toResponseEntity();
        }

    /**
     * Saves a new realm role in Keycloak.
     *
     * @param roleDTO The UserDTO containing user information.
     * @return
     */
    private void saveRoleInKeycloak(RoleDTO roleDTO) {
        log.info("Inside Save Keycloak Realm Role ::: Trying to save role in keycloak");
        var body = KeycloakRoleDTO
                .builder()
                .name(roleDTO.getName().toUpperCase())
                .build();

        var res = keycloakService.saveRole(body);

        if (res.getStatusCode().isError()) {
            throw new ResponseStatusException(HttpStatus.valueOf(res.getStatusCode().value()),
                    "Role not saved in keycloak");
        }
        log.info("Successfully saved role in keycloak! statusCode -> {}" +
                " Saved role -> {}", res.getStatusCode(), body);

    }

    /**
     * Updates an existing realm role in Keycloak.
     *
     * @param roleDTO The RoleDTO containing role information.
     */
    private void updateRoleInKeycloak(RoleDTO roleDTO) {
        log.info("Inside Update Keycloak Realm Role ::: Trying to update role in keycloak");
        var body = KeycloakRoleDTO
                .builder()
                .id(roleDTO.getId())
                .name(roleDTO.getName())
                .build();

        var res = keycloakService.updateRole(body);

        if (res.getStatusCode().isError()) {
            throw new ResponseStatusException(HttpStatus.valueOf(res.getStatusCode().value()),
                    "Role not updated in keycloak");
        }
        log.info("Successfully updated role in keycloak! statusCode -> {}" +
                " Saved role -> {}", res.getStatusCode(), body);
    }

}
