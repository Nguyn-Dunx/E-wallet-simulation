package org.example.backend.modules.identity.services;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.exception.ElementAlreadyExistsException;
import org.example.backend.modules.identity.common.enums.EkycStatus;
import org.example.backend.modules.identity.common.enums.LoginType;
import org.example.backend.modules.identity.common.enums.RoleName;
import org.example.backend.modules.identity.dto.request.SignupAdminRequest;
import org.example.backend.modules.identity.dto.request.SignupUserRequest;
import org.example.backend.modules.identity.dto.response.CommandResponse;
import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.entity.Admin;
import org.example.backend.modules.identity.entity.Role;
import org.example.backend.modules.identity.entity.User;
import org.example.backend.modules.identity.repository.AccountRepository;
import org.example.backend.modules.identity.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public CommandResponse signupUser(SignupUserRequest request) {

        if (accountRepository.existsByLoginKeyIgnoreCase(request.getLoginKey())) {
            throw new ElementAlreadyExistsException("Login key already exists");
        }

        Account account = new Account();
        account.setLoginKey(request.getLoginKey());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setLoginType(LoginType.PHONE);
        account.setStatus("ACTIVE");
        Role userRole = roleRepository
                .findByName(RoleName.USER)
                .orElseThrow();

        account.setRole(userRole);

        User user = new User();
        user.setFullName(request.getFullName());
        user.setPhone(request.getLoginKey());
        user.setEkycStatus(EkycStatus.UNVERIFIED);
        user.setAccount(account);
        account.setUser(user);

        accountRepository.save(account);

        return CommandResponse.builder()
                .message("Registering successfully")
                .build();
    }

    public CommandResponse signupAdmin(SignupAdminRequest request) {

        if (accountRepository.existsByLoginKeyIgnoreCase(request.getLoginKey())) {
            throw new ElementAlreadyExistsException("Login key already exists");
        }

        Account account = new Account();
        account.setLoginKey(request.getLoginKey());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setLoginType(LoginType.EMPLOYEE_CODE);
        account.setStatus("ACTIVE");

        Admin admin = new Admin();
        admin.setEmployeeCode(request.getEmployeeCode());
        admin.setAccount(account);
        account.setAdmin(admin);

        accountRepository.save(account);

        return CommandResponse.builder()
                .message("Successfully")
                .build();
    }
}