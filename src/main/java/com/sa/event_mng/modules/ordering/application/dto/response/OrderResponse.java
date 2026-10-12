package com.sa.event_mng.modules.ordering.application.dto.response;

import com.sa.event_mng.model.enums.OrderStatus;
import com.sa.event_mng.model.enums.PaymentMethod;
import com.sa.event_mng.model.enums.PaymentStatus;
import com.sa.event_mng.modules.ticketing.application.dto.response.TicketResponse;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private Long orderCode;
    private BigDecimal organizerAmount; // tổng tiền btc ăn
    private Float platformFeeRate;  //phần trăm tiền admin ăn
    private BigDecimal serviceFee;  //tổng tiền admin ăn
    private BigDecimal totalAmount; //tổng tiền khách phải trả
    private BigDecimal discountAmount;
    private String voucherCode;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;
    private LocalDateTime expiresAt;
    private String paymentUrl;
    private List<OrderItemResponse> items;
    private List<TicketResponse> tickets;
}
