package com.stlghana.admin_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PermissionDTO {

    private UUID id;
    @NotNull
    @NotEmpty
    @Size(min = 3,max = 60)
    private String name;
    @Size(min = 3,max = 255)
    private String description;
    private UUID permissionCategoryId;
    private String permissionCategoryName;
    @JsonProperty("isEnabled")
    private boolean enabled;
}
