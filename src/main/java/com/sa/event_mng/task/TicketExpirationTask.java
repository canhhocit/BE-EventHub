package com.sa.event_mng.task;

import com.sa.event_mng.model.enums.OrderStatus;
import com.sa.event_mng.model.enums.PaymentStatus;
import com.sa.event_mng.modules.event.domain.repository.TicketTypeRepository;
import com.sa.event_mng.modules.ordering.domain.model.Order;
import com.sa.event_mng.modules.ordering.domain.model.OrderItem;
import com.sa.event_mng.modules.ordering.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketExpirationTask {

    private static final long INTERVAL_MS = 60000;

    private final OrderRepository orderRepository;
    private final TicketTypeRepository ticketTypeRepository;

    private final AtomicLong lastRunAt = new AtomicLong();


    @Scheduled(fixedRate = INTERVAL_MS)
    @Transactional
    public void scanAndReleaseExpiredOrders() {
        lastRunAt.set(System.currentTimeMillis());
        LocalDateTime now = LocalDateTime.now();

        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(
                OrderStatus.PENDING,
                PaymentStatus.PENDING,
                now
        );

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("[TICKET_EXPIRATION] Phát hiện {} đơn hàng quá hạn giữ vé. Tiến hành hoàn trả kho và hủy đơn.", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        if (item.getTicketType() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                            ticketTypeRepository.increaseRemainingQuantity(item.getTicketType().getId(), item.getQuantity());
                            log.info("[TICKET_EXPIRATION] Đã hoàn trả {} vé loại {} cho đơn #{}",
                                    item.getQuantity(), item.getTicketType().getId(), order.getOrderCode());
                        }
                    }
                }
                order.setOrderStatus(OrderStatus.CANCELLED);
                order.setPaymentStatus(PaymentStatus.FAILED);
                orderRepository.save(order);
                log.info("[TICKET_EXPIRATION] Đã hủy đơn hàng quá hạn #{} (hết hạn lúc: {})",
                        order.getOrderCode(), order.getExpiresAt());
            } catch (Exception e) {
                log.error("[TICKET_EXPIRATION] Lỗi khi xử lý hủy đơn #{}: {}", order.getOrderCode(), e.getMessage(), e);
            }
        }
    }

    public boolean claimIfStale() {
        long last = lastRunAt.get();
        long now = System.currentTimeMillis();
        return now - last >= INTERVAL_MS && lastRunAt.compareAndSet(last, now);
    }
}
