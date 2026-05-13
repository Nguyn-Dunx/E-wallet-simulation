package org.example.backend.modules.identity.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.backend.modules.identity.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    @Query("""
        select a from Account a
        join fetch a.role
        left join fetch a.user
        left join fetch a.admin
        where a.loginKey = :loginKey
        and a.deletedAt is null
    """)
    Optional<Account> findAuthAccount(String loginKey);

    @Modifying
    @Query("""
        update Account a
        set a.tokenVersion = a.tokenVersion + 1
        where a.loginKey = :loginKey
    """)
    int increaseTokenVersion(@Param("loginKey") String loginKey);

    @Query("""
    select a.tokenVersion
    from Account a
    where a.loginKey = :loginKey
""")
    Optional<Integer> findTokenVersion(@Param("loginKey") String loginKey);

    boolean existsByLoginKeyIgnoreCase(@NotBlank(message = "{validation.auth.loginKey.required}") String loginKey);
}
