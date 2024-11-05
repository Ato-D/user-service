package com.stlghana.admin_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RolePermissionDTO {

    private UUID roleId;
    private String roleName;
    private List<UUID> permissionIds = new ArrayList<>();
    private String permissionName;
}
