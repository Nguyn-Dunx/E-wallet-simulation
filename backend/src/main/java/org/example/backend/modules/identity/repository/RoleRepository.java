package org.example.backend.modules.identity.repository;

import org.example.backend.modules.identity.common.enums.RoleName;
import org.example.backend.modules.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepository
        extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(RoleName roleName);
}
