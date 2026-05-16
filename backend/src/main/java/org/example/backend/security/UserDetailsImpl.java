package org.example.backend.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.backend.modules.identity.common.enums.AccountStatus;
import org.example.backend.modules.identity.common.enums.LoginType;
import org.example.backend.modules.identity.entity.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserDetailsImpl implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final String username;

    @JsonIgnore
    private final String password;

    private final LoginType loginType;

    private final String displayName;

    private final AccountStatus status;

    private final Integer tokenVersion;

    private final Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(Account account) {
        String displayName = resolveDisplayName(account);
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(account.getRole().getName().asAuthority())
        );
        return new UserDetailsImpl(
                account.getId(),
                account.getLoginKey(),
                account.getPasswordHash(),
                account.getLoginType(),
                displayName,
                account.getStatus(),
                account.getTokenVersion(),
                authorities
        );
    }

    private static String resolveDisplayName(Account account) {
        LoginType type = account.getLoginType();

        if (type == LoginType.PHONE) {
            return account.getUser() != null
                    ? account.getUser().getFullName()
                    : account.getLoginKey();
        } else if (type == LoginType.EMPLOYEE_CODE) {
            return account.getAdmin() != null
                    ? account.getAdmin().getEmployeeCode()
                    : account.getLoginKey();
        }

        throw new IllegalStateException("Unsupported login type: " + type);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; //Check status
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != AccountStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; //Check password_changed_at
    }

    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ACTIVE;
    }
}
