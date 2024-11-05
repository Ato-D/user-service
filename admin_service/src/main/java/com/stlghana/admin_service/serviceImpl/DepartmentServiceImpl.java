package com.stlghana.admin_service.serviceImpl;

import com.stlghana.admin_service.dto.DepartmentDTO;
import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.model.DepartmentModel;
import com.stlghana.admin_service.model.UserModel;
import com.stlghana.admin_service.repository.DepartmentRepository;
import com.stlghana.admin_service.repository.UserRepository;
import com.stlghana.admin_service.service.DepartmentService;
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
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;


    @Override
    public ResponseEntity<ResponseRecord> findAll(Map<String, String> params) {
        log.info(getAuthenticatedUserName() + " inside find All Departments :::  Trying to fetch departments" +
                " per given params -> {}",params);
        ResponseRecord response;

        try {
            var roles = getUserRoles();
            boolean isAdmin = hasAdminRole(roles);

            String searchValue = params != null ? params.getOrDefault("search","")
                    : "";

            if (params == null || params.getOrDefault("paginate", "false").equalsIgnoreCase(("false"))) {
                List<DepartmentModel> res;
                res = isAdmin ? departmentRepository.findAllByName(searchValue)
                        : new ArrayList<>();

                var departmentDTOStream = res.parallelStream().map(DepartmentModel::toDTO);
                log.info("Success!, statusCode -> {} and Message -> {}", HttpStatus.OK, res);
                response = getResponseRecord("Successfully retrieved records!", HttpStatus.OK, departmentDTOStream);

            } else {
                Pageable pageable = getPageRequest(params);
                Page<DepartmentModel> res;
                res = isAdmin ? departmentRepository.findAllByName(searchValue,pageable)
                        : new PageImpl<>(new ArrayList<>(),pageable,0);

                Page<DepartmentDTO> departmentDTOS = res.map(DepartmentModel::toDTO);
                Pagination pagination = mapToPagination(departmentDTOS);

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
        log.info(getAuthenticatedUserName() + " inside find Department by Id ::: " +
                "Trying to find department by id -> {}", id);
        ResponseRecord response;

        try {
            var record = departmentRepository.findById(id);
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
    public ResponseEntity<ResponseRecord> upsert(DepartmentDTO departmentDTO, UUID id) {
        log.info(getAuthenticatedUserName() + " inside upsert Department by Id ::: Trying to upsert department by id -> {}", id);
        ResponseRecord response;

        try {
            boolean isAdmin = hasAdminRole(getUserRoles());
            if (isAdmin) {
                DepartmentModel departmentModel;

                UserModel manager = new UserModel();

                if (isNotNullOrEmpty(departmentDTO.getManagerId())) {
                    manager = userRepository.findById(departmentDTO.getManagerId()).orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Manager record " + id + " does not exist"));
                }

                if (!isNotNullOrEmpty(id)) {
                    /**
                     * Inserts a new department record
                     */
                    departmentModel = DepartmentModel
                            .builder()
                            .name(departmentDTO.getName().toUpperCase())
                            .manager(manager)
                            .isEnabled(true)
                            .updatedAt(ZonedDateTime.now())
                            .updatedBy(getAuthenticatedUserId())
                            .createdAt(ZonedDateTime.now())
                            .createdBy(getAuthenticatedUserId())
                            .build();
                } else {
                    /**
                     * Updates an existing department record by id
                     */
                    departmentModel = departmentRepository.findById(id)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT,
                                    "Department record " + id + " does not exist"));
                    departmentModel.setName(departmentDTO.getName().toUpperCase());
                    departmentModel.setManager(manager);
                    departmentModel.setIsEnabled(departmentDTO.isEnabled());
                    departmentModel.setUpdatedAt(ZonedDateTime.now());
                    departmentModel.setUpdatedBy(getAuthenticatedUserId());
                }

                var record = departmentRepository.save(departmentModel);
                log.info("Success! statusCode -> {} and Message -> {}", id == null ? HttpStatus.CREATED
                        : HttpStatus.ACCEPTED, record);
                response = getResponseRecord("Record " + (id == null ? "saved" : "updated") +
                        " successfully", id == null ? HttpStatus.CREATED : HttpStatus.ACCEPTED, record.toDTO());
            } else {
                response = getResponseRecord("No authorization to upsert setups", HttpStatus.FORBIDDEN);
            }

        } catch (ResponseStatusException e) {
            log.info("Error Occurred! statusCode -> {} and Message -> {} and Reason -> {}",
                    e.getStatusCode(), e.getMessage(), e.getReason());
            response = getResponseRecord(e.getReason(), HttpStatus.valueOf(e.getStatusCode().value()));
        } catch (DataIntegrityViolationException e) {
            log.error("Exception Occurred! Message -> {} and Cause -> {}", e.getMostSpecificCause(), e.getMessage());
            response = getResponseRecord(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Exception Occurred! statusCode -> {} and Cause -> {} and Message -> {}",
                    500, e.getCause(), e.getMessage());
            response = getResponseRecord(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response.toResponseEntity();

    }
}
