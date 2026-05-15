package org.example.backend.modules.identity.repository;

import org.example.backend.modules.identity.entity.PendingUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PendingUserRepository extends JpaRepository<PendingUser, String> {
    Optional<PendingUser> findByPhoneNumber(String phoneNumber);
}
