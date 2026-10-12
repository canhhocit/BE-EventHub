package com.sa.event_mng.modules.ordering.domain.repository;

import com.sa.event_mng.modules.ordering.domain.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(String orderId);
}

