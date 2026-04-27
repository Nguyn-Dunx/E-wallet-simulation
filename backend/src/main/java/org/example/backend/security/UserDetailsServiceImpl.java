package org.example.backend.security;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.config.MessageService;
import org.example.backend.modules.identity.entity.Account;
import org.example.backend.modules.identity.repository.AccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import static org.example.backend.common.MessageKeys.ERROR_USERNAME_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final MessageService messageService;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String loginKey) throws UsernameNotFoundException {
        Account account = accountRepository.findAuthAccount(loginKey)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageService.getMessage(ERROR_USERNAME_NOT_FOUND)
                ));

        return UserDetailsImpl.build(account);
    }
}
