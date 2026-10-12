package com.sa.event_mng.modules.ordering.domain.repository;

import com.sa.event_mng.model.enums.OrderStatus;
import com.sa.event_mng.model.enums.PaymentStatus;
import com.sa.event_mng.modules.ordering.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer WHERE o.id = :id")
    Optional<Order> findByIdWithCustomer(@Param("id") String id);

    Optional<Order> findByOrderCode(Long orderCode);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items oi LEFT JOIN FETCH oi.ticketType WHERE o.orderStatus = :orderStatus AND o.paymentStatus = :paymentStatus AND o.expiresAt IS NOT NULL AND o.expiresAt < :now")
    List<Order> findExpiredPendingOrders(
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("now") LocalDateTime now
    );
}
