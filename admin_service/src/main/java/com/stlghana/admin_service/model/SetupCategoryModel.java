package com.stlghana.admin_service.model;


import com.stlghana.admin_service.dto.SetupCategoryDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tbl_setup_category",
        schema = "voip_admin"
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetupCategoryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name",nullable = false, length = 65)
    private String name;

    @Column(name = "description",nullable = false, length = 265)
    private String description;

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


    public SetupCategoryDTO toDTO() {
        return SetupCategoryDTO
                .builder()
                .id(this.id)
                .name(this.name)
                .enabled(this.isEnabled)
                .build();
    }
}
