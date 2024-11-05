package com.stlghana.admin_service.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stlghana.admin_service.dto.DepartmentDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(
        name = "tbl_department",
        schema = "voip_admin"
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DepartmentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name",nullable = false, unique = true, length = 60)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "manager_id", nullable = false)
    private UserModel manager;

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

    @PrePersist
    public void prePersist() {
        this.isEnabled = true;
    }

    public DepartmentDTO toDTO() {
        return DepartmentDTO
                .builder()
                .id(this.id)
                .name(this.name)
                .managerId(Optional.ofNullable(this.manager)
                        .map(UserModel::getId).orElse(null))
                .manager(Optional.ofNullable(this.manager)
                        .map(manager -> manager.getFirstName() + " " + manager.getLastName()).orElse(null))
                .enabled(this.isEnabled)
                .build();
    }
}
