package org.example.backend.modules.identity.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.modules.identity.common.enums.AccountStatus;
import org.example.backend.modules.identity.common.enums.RoleName;
import org.example.backend.modules.identity.dto.response.AdminAccountResponse;
import org.example.backend.modules.identity.services.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<Page<AdminAccountResponse>>> getAccounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) RoleName role,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminAccountResponse> accounts = adminService.getAccounts(keyword, status, role, pageable);
        return ResponseEntity.ok(ApiResponse.success("Accounts fetched successfully", accounts));
    }

    @PostMapping("/accounts/{accountId}/lock")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> lockAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(adminService.lockAccount(accountId));
    }

    @PostMapping("/accounts/{accountId}/unlock")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> unlockAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(adminService.unlockAccount(accountId));
    }

    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<AdminAccountResponse>> deleteAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(adminService.deleteAccount(accountId));
    }
}
