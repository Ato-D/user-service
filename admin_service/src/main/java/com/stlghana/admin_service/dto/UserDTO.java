package com.stlghana.admin_service.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
public class UserDTO {

    private UUID id;
    private String userName;
    @Size(min = 5, message = "User email should be least 5 characters")
    @Email(message = "Invalid email address")
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private List<UUID> roles = new ArrayList<>();
    private List<UUID> permissions = new ArrayList<>();
    private UUID departmentId;
    private UUID managerId;
    private List<String> userRoles = new ArrayList<>();
    private List<String> userPermissions = new ArrayList<>();
    private String departmentName;
    private String managerName;
    private boolean enabled;
    private boolean exists;
}
