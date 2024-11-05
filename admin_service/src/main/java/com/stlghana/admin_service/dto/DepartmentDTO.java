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
public class DepartmentDTO {

    private UUID id;
    @NotEmpty
    @Size(min = 3,max = 60)
    private String name;
    @NotNull
    private UUID companyId;
    //    @NotBlank
    private UUID managerId;
    private String manager;
    @JsonProperty("isEnabled")
    private boolean enabled;
}
