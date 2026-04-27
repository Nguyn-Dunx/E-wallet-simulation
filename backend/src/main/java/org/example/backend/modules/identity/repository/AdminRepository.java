package org.example.backend.modules.identity.repository;

import org.example.backend.modules.identity.entity.Admin;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository {
    Optional<Admin> findByEmployeeCode(String code);
    boolean existsByEmployeeCode(String code);
}
