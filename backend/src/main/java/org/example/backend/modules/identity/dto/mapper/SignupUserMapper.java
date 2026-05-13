package org.example.backend.modules.identity.dto.mapper;

import org.example.backend.modules.identity.common.enums.LoginType;
import org.example.backend.modules.identity.dto.request.SignupUserRequest;
import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.entity.Role;
import org.example.backend.modules.identity.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = LoginType.class
)
public interface SignupUserMapper {

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
            expression = "java(LoginType.PHONE)"
    )

    @Mapping(target = "passwordHash", source = "passwordHash")

    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "loginFailedCount", constant = "0")
    @Mapping(target = "tokenVersion", constant = "0")

    @Mapping(target = "role", source = "role")

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "admin", ignore = true)
    Account toAccount(
            SignupUserRequest request,
            Role role,
            String passwordHash
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)

    @Mapping(target = "account", source = "account")

    @Mapping(
            target = "fullName",
            expression = "java(request.getFullName().trim())"
    )

    @Mapping(
            target = "phone",
            expression = "java(request.getLoginKey().trim())"
    )

    @Mapping(target = "ekycStatus", constant = "PENDING")
    @Mapping(target = "avatarUrl", ignore = true)
    User toUser(
            SignupUserRequest request,
            Account account
    );
}