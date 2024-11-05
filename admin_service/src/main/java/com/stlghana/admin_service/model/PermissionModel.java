package com.stlghana.admin_service.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stlghana.admin_service.dto.PermissionDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(
        name = "tbl_permission",
        schema = "voip_admin"
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name",nullable = false, length = 65)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "permission_category_id")
    private PermissionCategoryModel permissionCategory;

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    @Column(name = "created_by",nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    public PermissionDTO toDTO() {
        return PermissionDTO
                .builder()
                .id(this.id)
                .name(this.name)
                .permissionCategoryId(Optional.ofNullable(this.permissionCategory)
                        .map(PermissionCategoryModel::getId).orElse(null))
                .permissionCategoryName(Optional.ofNullable(this.permissionCategory)
                        .map(PermissionCategoryModel::getName).orElse(null))
                .enabled(this.isEnabled)
                .build();
    }


}
