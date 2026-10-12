package com.sa.event_mng.modules.event.domain.repository;

import com.sa.event_mng.modules.event.domain.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEventId(Long eventId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TicketType t SET t.remainingQuantity = t.remainingQuantity - :quantity, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id AND t.remainingQuantity >= :quantity")
    int decreaseRemainingQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TicketType t SET t.remainingQuantity = CASE WHEN (t.remainingQuantity + :quantity) > t.totalQuantity THEN t.totalQuantity ELSE (t.remainingQuantity + :quantity) END, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int increaseRemainingQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
}
