package com.sa.event_mng.modules.identity.domain.repository;

import com.sa.event_mng.modules.identity.domain.model.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    void deleteByExpiryTimeBefore(Date date);
}
