package com.stlghana.admin_service.record;

import java.util.UUID;

public record UserRecord (

    UUID id,

    String userName,

    String firstName,

    String lastName,

    String email,

    String department,

    String roles

)   {
}
