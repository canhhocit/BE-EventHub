package com.sa.event_mng.task;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Cập nhật trạng thái sự kiện và giải phóng đơn hàng giữ vé hết hạn ngay trong request
 * nếu các task định kỳ quá 1 phút chưa chạy (ví dụ Render instance vừa thức dậy).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventStatusRefreshInterceptor implements HandlerInterceptor {

    private final EventStatusTask eventStatusTask;
    private final TicketExpirationTask ticketExpirationTask;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (eventStatusTask.claimIfStale()) {
            try {
                eventStatusTask.autoUpdateEventStatus();
            } catch (Exception e) {
                log.warn("Không cập nhật được trạng thái sự kiện: {}", e.getMessage());
            }
        }
        if (ticketExpirationTask.claimIfStale()) {
            try {
                ticketExpirationTask.scanAndReleaseExpiredOrders();
            } catch (Exception e) {
                log.warn("Không giải phóng được đơn hàng hết hạn: {}", e.getMessage());
            }
        }
        return true;
    }
}
