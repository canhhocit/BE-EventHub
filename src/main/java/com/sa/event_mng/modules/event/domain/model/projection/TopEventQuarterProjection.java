package com.sa.event_mng.modules.event.domain.model.projection;

import java.math.BigDecimal;

public interface TopEventQuarterProjection {
    Long getEventId();
    String getEventName();
    Integer getTicketsSold();
    Double getOccupancyRate();
    BigDecimal getTotalRevenue();
    String getStatus();
    String getProvince();
    String getStartTime();
    String getEventImage();
    BigDecimal getMinPrice();
}
