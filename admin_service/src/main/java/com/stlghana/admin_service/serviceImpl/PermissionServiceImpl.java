package com.stlghana.admin_service.serviceImpl;

import com.stlghana.admin_service.dto.PermissionDTO;
import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.RolePermissionDTO;
import com.stlghana.admin_service.model.PermissionCategoryModel;
import com.stlghana.admin_service.model.PermissionModel;
import com.stlghana.admin_service.model.RoleModel;
import com.stlghana.admin_service.model.RolePermissionModel;
import com.stlghana.admin_service.repository.PermissionCategoryRepository;
import com.stlghana.admin_service.repository.PermissionRepository;
import com.stlghana.admin_service.repository.RolePermissionRepository;
import com.stlghana.admin_service.repository.RoleRepository;
import com.stlghana.admin_service.service.PermissionService;
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

import java.security.Permission;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.stlghana.admin_service.utility.AppUtils.*;


@Service
@AllArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionCategoryRepository permissionCategoryRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;


    @Override
    public ResponseEntity<ResponseRecord> findAll(Map<String, String> params) {
        log.info(getAuthenticatedUserName() + " inside find All Permissions :::  Trying to fetch " +
                "permissions per given params -> {}",params);
        ResponseRecord response;

        try{
            var roles = getUserRoles();
            boolean isAdmin = hasAdminRole(roles);

            String searchValue = params != null ? params.getOrDefault("search","")
                    : "";
            if (params == null || params.getOrDefault("paginate", "false").equalsIgnoreCase(("false"))) {
                List<PermissionModel> res;

//            var roleIds = extractUUIDsFromParams(params,"roleIds");
            res = isAdmin ? permissionRepository.findAllByName(searchValue)
                    : new ArrayList<>();
                var permissionDTOStream = res.parallelStream().map(PermissionModel::toDTO);
                log.info("Success!, statusCode -> {} and Message -> {}", HttpStatus.OK, res);
                response = getResponseRecord("Successfully retrieved records!", HttpStatus.OK, permissionDTOStream);

            } else {
                Pageable pageable = getPageRequest(params);
                Page<PermissionModel> res;
                res = isAdmin ? permissionRepository.findAllByName(searchValue, pageable)
                        : new PageImpl<>(new ArrayList<>(),pageable,0);

                Page<PermissionDTO> permissionDTOS = res.map(PermissionModel::toDTO);
                Pagination pagination = mapToPagination(permissionDTOS);

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
            String error = e.getMessage();
            System.out.println(error);
            response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.toResponseEntity();
    }

    @Override
    public ResponseEntity<ResponseRecord> findById(UUID id) {
        log.info(getAuthenticatedUserName() + " inside find Permission by Id ::: Trying to find permission " +
                "by id -> {}", id);
        ResponseRecord response;

        try {
            var record = permissionRepository.findById(id);
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
    public ResponseEntity<ResponseRecord> upsert(PermissionDTO permissionDTO, UUID id) {
        log.info(getAuthenticatedUserName() + " inside upsert Permission by Id ::: " +
                "Trying to upsert permission  by id -> {}", id);
        ResponseRecord response;

        try {
            boolean isAdmin = hasAdminRole(getUserRoles());
            if (isAdmin) {

                PermissionModel permissionModel;

                PermissionCategoryModel permissionCategoryModel = isNotNullOrEmpty(permissionDTO.getPermissionCategoryId())
                        ? findEntityById(permissionCategoryRepository, permissionDTO.getPermissionCategoryId(), "Permission")
                        : null;

                if (!isNotNullOrEmpty(id)) {
                    /**
                     * Inserts a new permission record if there is no permission id in the database
                     */


                    permissionModel = PermissionModel
                            .builder()
                            .name(permissionDTO.getName())
                            .permissionCategory(permissionCategoryModel)
//                            .description(permissionDTO.getDescription())
                            .isEnabled(true)
                            .updatedAt(ZonedDateTime.now())
                            .updatedBy(getAuthenticatedUserId())
                            .createdAt(ZonedDateTime.now())
                            .createdBy(getAuthenticatedUserId())
                            .build();
                } else {

                    /**
                     * Updates an existing permission record by id
                     */
                    permissionModel = permissionRepository.findById(id)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT,
                                    "Permission record " + id + " does not exist"));
                    permissionModel.setName(permissionDTO.getName());
                    permissionModel.setPermissionCategory(permissionCategoryModel);
//                    permissionModel.setDescription(permissionDTO.getDescription());
                    permissionModel.setIsEnabled(permissionDTO.isEnabled());
                    permissionModel.setUpdatedAt(ZonedDateTime.now());
                    permissionModel.setUpdatedBy(getAuthenticatedUserId());
                }

                var record = permissionRepository.save(permissionModel);
                log.info("Success! statusCode -> {} and Message -> {}", id == null ? HttpStatus.CREATED
                        : HttpStatus.ACCEPTED, record);
                response = getResponseRecord("Record " + (id == null ? "saved" : "updated") +
                        " successfully", id == null ? HttpStatus.CREATED : HttpStatus.ACCEPTED, record.toDTO());
            } else {
                response = getResponseRecord("No authorization to upsert setups", HttpStatus.FORBIDDEN);
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

    @Override
    public ResponseEntity<ResponseRecord> saveRolePermission(RolePermissionDTO rolePermissionDTO) {
        log.info(getAuthenticatedUserName() + " inside save Role Permission ::: " +
                "Trying to save role permission -> {}", rolePermissionDTO);

        ResponseRecord response;

        try {
            boolean isAdmin = hasAdminRole(getUserRoles());

            if (!isAdmin) {
                response = getResponseRecord("No authorization to upsert setups", HttpStatus.FORBIDDEN);
            } else {
                var permissionIds = rolePermissionDTO.getPermissionIds();

                RoleModel roleModel = isNotNullOrEmpty(rolePermissionDTO.getRoleId())
                        ? findEntityById(roleRepository, rolePermissionDTO.getRoleId(), "Role")
                        : null;

                ;
                if (isNotNullOrEmpty(permissionIds)) {
                    permissionIds.parallelStream().forEach(i -> {
                        PermissionModel permissionModel = isNotNullOrEmpty(i)
                                ? findEntityById(permissionRepository, i, "Permission")
                                : null;

                        var rolePermission = RolePermissionModel
                                .builder()
                                .role(roleModel)
                                .permission(permissionModel)
                                .build();
                        rolePermissionRepository.save(rolePermission);
                    });
                }

                log.info("Success! statusCode -> {}", HttpStatus.CREATED);
                response = getResponseRecord("Record saved successfully", HttpStatus.CREATED);
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


    public ResponseEntity<ResponseRecord> assignPermissionsToRole(UUID roleId, List<UUID> permissionIds) {
        log.info("Inside the Assign Permissions to Role method ::: Trying to assign permissions to role with ID {}", roleId);
        ResponseRecord response;

        try {
            RoleModel role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

            List<PermissionModel> permissions = permissionRepository.findAllById(permissionIds);

            if (permissions.size() != permissionIds.size()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more permissions not found");
            }

            // Assign permissions to the role
            for (PermissionModel permission : permissions) {
                RolePermissionModel rolePermission = new RolePermissionModel();
                rolePermission.setRole(role);
                rolePermission.setPermission(permission);
                rolePermissionRepository.save(rolePermission);
            }

            log.info("Successfully assigned permissions to role with ID {}", roleId);
            response = getResponseRecord("Permissions assigned successfully", HttpStatus.OK, null);
        } catch (ResponseStatusException e) {
            log.error("Error Occurred! statusCode -> {}, Message -> {}, Reason -> {}", e.getStatusCode(), e.getMessage(), e.getReason());
            response = getResponseRecord(e.getReason(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (DataIntegrityViolationException e) {
            log.error("Exception Occurred! Message -> {} and Cause -> {}", e.getMostSpecificCause(), e.getMessage());
            response = getResponseRecord(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Exception Occurred! statusCode -> {} and Cause -> {} and Message -> {}", 500, e.getCause(), e.getMessage());
            response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(response.toResponseEntity().getStatusCode());
    }


}
