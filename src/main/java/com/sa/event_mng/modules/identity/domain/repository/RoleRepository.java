package com.sa.event_mng.modules.identity.domain.repository;

import com.sa.event_mng.modules.identity.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByName(String name);
}
