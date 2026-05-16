package org.example.backend.modules.identity.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.common.dto.ApiResponse;
import org.example.backend.common.utils.JwtUtils;
import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.repository.AccountRepository;
import org.example.backend.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Getter
@Setter
@AllArgsConstructor
public class UserService {

    private final AccountRepository accountRepository;

    private final TokenBlacklistService tokenBlacklistService;

    private final JwtUtils jwtUtils;


    @Transactional
    public ApiResponse<String> updateDisplayName(String newFullName, String currentToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) auth.getPrincipal();

        Account account = accountRepository.findAuthAccount(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Not found any account"));

        account.getUser().setFullName(newFullName);
        accountRepository.save(account);

        UsernamePasswordAuthenticationToken newAuth = getUsernamePasswordAuthenticationToken(newFullName, currentUser, auth);
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        Integer currentTokenVersion = account.getTokenVersion();
        String newToken = jwtUtils.generateToken(newAuth, currentTokenVersion);

        UUID jti = jwtUtils.getJti(currentToken);
        if (jti != null) {
            tokenBlacklistService.blacklist(jti, account.getUser().getId(), null);
        }

        return ApiResponse.success(
                HttpStatus.CREATED,
                "Change successfully",
                newToken
        );
    }

    private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(String newFullName, UserDetailsImpl currentUser, Authentication auth) {
        UserDetailsImpl newUserDetails = new UserDetailsImpl(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getPassword(),
                currentUser.getLoginType(),
                newFullName,
                currentUser.getStatus(),
                currentUser.getTokenVersion(),
                currentUser.getAuthorities()
        );

        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                newUserDetails,
                auth.getCredentials(),
                auth.getAuthorities()
        );
        return newAuth;
    }
}
