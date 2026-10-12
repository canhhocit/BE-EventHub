package com.sa.event_mng.modules.marketing.domain.repository;

import com.sa.event_mng.modules.marketing.domain.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    org.springframework.data.domain.Page<Voucher> findByCreatorId(Long creatorId, org.springframework.data.domain.Pageable pageable);
    
    @org.springframework.data.jpa.repository.Query("SELECT v FROM Voucher v WHERE (v.event.id = :eventId OR (v.creator.id = :organizerId AND v.event IS NULL)) " +
           "AND (v.quantity IS NULL OR v.quantity > 0)")
    java.util.List<Voucher> findActiveVouchersForEvent(Long eventId, Long organizerId, java.time.LocalDateTime now);
}
