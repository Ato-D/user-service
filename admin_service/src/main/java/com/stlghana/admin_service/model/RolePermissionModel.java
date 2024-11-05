package com.stlghana.admin_service.model;

import com.stlghana.admin_service.dto.RolePermissionDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Optional;

@Entity
@IdClass(RolePermissionModel.class)
@Table(
        name = "tbl_role_permission",
        schema = "voip_admin"
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RolePermissionModel implements Serializable {


    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private RoleModel role;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id")
    private PermissionModel permission;


    public RolePermissionDTO toDTO() {
        return RolePermissionDTO
                .builder()
                .roleId(Optional.ofNullable(this.role)
                        .map(RoleModel::getId).orElse(null))
                .roleName(Optional.ofNullable(this.role)
                        .map(RoleModel::getName).orElse(null))
//                .permissionId(Optional.ofNullable(this.permission)
//                        .map(PermissionModel::getId).orElse(null))
                .permissionName(Optional.ofNullable(this.permission)
                        .map(PermissionModel::getName).orElse(null))
                .build();
    }


}
