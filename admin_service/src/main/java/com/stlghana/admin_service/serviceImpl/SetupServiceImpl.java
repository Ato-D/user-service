package com.stlghana.admin_service.serviceImpl;

import com.stlghana.admin_service.dto.ResponseRecord;
import com.stlghana.admin_service.dto.SetupDTO;
import com.stlghana.admin_service.model.SetupCategoryModel;
import com.stlghana.admin_service.model.SetupModel;
import com.stlghana.admin_service.repository.SetupCategoryRepository;
import com.stlghana.admin_service.repository.SetupRepository;
import com.stlghana.admin_service.service.SetupService;
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
public class SetupServiceImpl implements SetupService {

    private final SetupRepository setupRepository;
    private final SetupCategoryRepository setupCategoryRepository;


    @Override
    public ResponseEntity<ResponseRecord> findAll(Map<String, String> params) {
        log.info(getAuthenticatedUserName() + " inside find All Setups :::" +
                "  Trying to fetch setups  per given params -> {}",params);
        ResponseRecord response;

        try {
            var roles = getUserRoles();
            boolean isAdmin = hasAdminRole(roles);

            String searchValue = params != null ? params.getOrDefault("search","")
                    : "";

            List<UUID> setupCategoryIds = extractUUIDsFromParams(params,"setupCategoryId");

            if (params == null || params.getOrDefault("paginate", "false").equalsIgnoreCase(("false"))) {
                List<SetupModel> res;

                if (isNotNullOrEmpty(setupCategoryIds)) {
                    res = setupRepository.findAllBySetupCategoryId(setupCategoryIds);
                    var setupDTOStream = res.parallelStream().map(SetupModel::toDTO);
                    log.info("Success!, statusCode -> {} and Message -> {}", HttpStatus.OK, res);
                    response = getResponseRecord("Successfully retrieved records!", HttpStatus.OK, setupDTOStream);
                    return response.toResponseEntity();
                }
                res = isAdmin ? setupRepository.findAllByName(searchValue)
                        : new ArrayList<>();

                var setupDTOStream = res.parallelStream().map(SetupModel::toDTO);
                log.info("Success!, statusCode -> {} and Message -> {}", HttpStatus.OK, res);
                response = getResponseRecord("Successfully retrieved records!", HttpStatus.OK, setupDTOStream);

            } else {
                Pageable pageable = getPageRequest(params);
                Page<SetupModel> res;

                if (isNotNullOrEmpty(setupCategoryIds)) {
                    res = setupRepository.findAllBySetupsCategoryIds(setupCategoryIds,pageable);
                    var setupDTOStream = res.stream().map(SetupModel::toDTO);
                    log.info("Success!, statusCode -> {} and Message -> {}", HttpStatus.OK, res);
                    response = getResponseRecord("Successfully retrieved records!", HttpStatus.OK, setupDTOStream);
                    return response.toResponseEntity();
                }

                res = isAdmin ? setupRepository.findAllByName(searchValue,pageable)
                        : new PageImpl<>(new ArrayList<>(),pageable,0);

                Page<SetupDTO> setupDTOS = res.map(SetupModel::toDTO);
                Pagination pagination = mapToPagination(setupDTOS);

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
        log.info(getAuthenticatedUserName() + " inside find Setup by Id ::: Trying to find setup  by id -> {}", id);
        ResponseRecord response;

        try {
            var record = setupRepository.findById(id);
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
    public ResponseEntity<ResponseRecord> upsert(SetupDTO setupDTO, UUID id) {
        log.info(getAuthenticatedUserName() + " inside upsert Setup by Id ::: Trying to upsert setup " +
                " by id -> {}", id);
        ResponseRecord response;

        try {
            boolean isAdmin = hasAdminRole(getUserRoles());
            if (isAdmin) {

                SetupModel setupModel;

                SetupCategoryModel setupCategory = isNotNullOrEmpty(setupDTO.getSetupCategoryId())
                        ? findEntityById(setupCategoryRepository, setupDTO.getSetupCategoryId(),
                        "SetupCategory")
                        : null;

                if (!isNotNullOrEmpty(id)) {
                    /**
                     * Inserts a new setup record
                     */
                    setupModel = SetupModel
                            .builder()
                            .name(setupDTO.getName())
                            .setupCategory(setupCategory)
                            .description(setupDTO.getDescription())
                            .isEnabled(true)
                            .updatedAt(ZonedDateTime.now())
                            .updatedBy(getAuthenticatedUserId())
                            .createdAt(ZonedDateTime.now())
                            .createdBy(getAuthenticatedUserId())
                            .build();
                } else {

                    /**
                     * Updates an existing setup record by id
                     */
                    setupModel = setupRepository.findById(id)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT,
                                    "Setup record " + id + " does not exist"));
                    setupModel.setName(setupDTO.getName());
                    setupModel.setSetupCategory(setupCategory);
                    setupModel.setDescription(setupDTO.getDescription());
                    setupModel.setIsEnabled(setupDTO.isEnabled());
                    setupModel.setUpdatedAt(ZonedDateTime.now());
                    setupModel.setUpdatedBy(getAuthenticatedUserId());
                }

                var record = setupRepository.save(setupModel);
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
}
