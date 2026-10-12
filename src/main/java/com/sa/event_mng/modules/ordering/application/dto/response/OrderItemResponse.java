package com.sa.event_mng.modules.ordering.application.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private Long ticketTypeId;
    private String ticketTypeName;
    private Long eventId;
    private String eventName;
    private String eventImage;
    private String eventLocation;
    private String eventProvince;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
