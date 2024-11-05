package com.stlghana.admin_service.model.views;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stlghana.admin_service.record.UserRecord;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_v")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserView {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "username")
    private String userName;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Column(name = "manager_id")
    private UUID managerId;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "department_name")
    private String departmentName;

    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tbl_user_role",
            joinColumns =@JoinColumn(name = "user_id"))
    @Column(name = "role_id")
    private List<UUID> roles;

    @Transient
    private String userRoles;

    @Column(name = "created_at")
    private LocalDate createdAt;

    public UserRecord toList() {
        return new UserRecord(
                id,
                userName,
                firstName,
                lastName,
                email,
                departmentName,
                userRoles
        );
    }
}
