package org.example.backend.modules.identity.dto.mapper;

import org.example.backend.modules.identity.common.enums.LoginType;
import org.example.backend.modules.identity.dto.request.SignupAdminRequest;
import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.entity.Admin;
import org.example.backend.modules.identity.entity.Role;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = LoginType.class
)
public interface SignupAdminMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)

    @Mapping(
            target = "loginKey",
            expression = "java(request.getLoginKey().trim())"
    )

    @Mapping(
            target = "loginType",
            expression = "java(LoginType.EMPLOYEE_CODE)"
    )

    @Mapping(target = "passwordHash", source = "passwordHash")

    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "loginFailedCount", constant = "0")
    @Mapping(target = "tokenVersion", constant = "0")

    @Mapping(target = "role", source = "role")

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "admin", ignore = true)
    Account toAccount(
            SignupAdminRequest request,
            Role role,
            String passwordHash
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)

    @Mapping(target = "account", source = "account")

    @Mapping(
            target = "employeeCode",
            expression = "java(request.getEmployeeCode().trim())"
    )

    @Mapping(target = "department", ignore = true)
    @Mapping(target = "internalNote", ignore = true)
    Admin toAdmin(
            SignupAdminRequest request,
            Account account
    );
}