package com.sa.event_mng.modules.ordering.domain.repository;

import com.sa.event_mng.modules.ordering.domain.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
}

