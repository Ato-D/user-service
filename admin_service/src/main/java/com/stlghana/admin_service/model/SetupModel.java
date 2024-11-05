package com.stlghana.admin_service.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stlghana.admin_service.dto.SetupDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(
        name = "tbl_setup",
        schema = "voip_admin"
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetupModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name",nullable = false, length = 65)
    private String name;

    @Column(name = "description",nullable = false, length = 265)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @ToString.Exclude
    @JoinColumn(name = "setup_category_id")
    private SetupCategoryModel setupCategory;

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


    public SetupDTO toDTO() {
        return SetupDTO
                .builder()
                .id(this.id)
                .name(this.name)
                .setupCategoryId(Optional.ofNullable(this.setupCategory)
                        .map(SetupCategoryModel::getId).orElse(null))
                .setupCategoryName(Optional.ofNullable(this.setupCategory)
                        .map(SetupCategoryModel::getName).orElse(null))
                .enabled(this.isEnabled)
                .build();
    }
}
