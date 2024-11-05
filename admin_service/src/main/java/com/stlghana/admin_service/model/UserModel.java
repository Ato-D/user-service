package com.stlghana.admin_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stlghana.admin_service.dto.UserDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "tbl_user",
        schema = "voip_admin")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "username",nullable = false, length = 20)
    private String userName;

    @Column(name = "email",nullable = false, length = 65)
    private String email;

    @Column(name = "first_name",nullable = false)
    private String firstName;

    @Column(name = "last_name",nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "department_id")
    private DepartmentModel department;

    @ManyToOne(fetch = FetchType.EAGER)
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "manager_id")
    private UserModel manager;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tbl_user_role",
            schema = "voip_admin",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleModel> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tbl_user_permissions",
            schema = "voip_admin",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionModel> permissions = new HashSet<>();

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "created_by",nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.isEnabled = true;
    }

    public UserDTO toDTO() {
        return UserDTO
                .builder()
                .id(this.id)
                .userName(this.userName)
                .email(this.email)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .phoneNumber(this.phoneNumber)
                .userRoles(this.roles.stream()
                        .map(RoleModel::getName).collect(Collectors.toList()))
                .userRoles(this.permissions.stream()
                        .map(PermissionModel::getName).collect(Collectors.toList()))
                .departmentId(Optional.ofNullable(this.department)
                        .map(DepartmentModel::getId).orElse(null))
                .departmentName(Optional.ofNullable(this.department)
                        .map(DepartmentModel::getName).orElse(null))
                .managerId(Optional.ofNullable(this.manager)
                        .map(UserModel::getId).orElse(null))
                .managerName(Optional.ofNullable(this.manager)
                        .map(manager -> manager.getFirstName() + " " + manager.getLastName())
                        .orElse(null))
                .enabled(this.isEnabled)
                .build();
    }
}
