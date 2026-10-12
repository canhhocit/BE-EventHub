package com.sa.event_mng.modules.ordering.application.mapper;

import com.sa.event_mng.modules.ordering.application.dto.response.OrderItemResponse;
import com.sa.event_mng.modules.ordering.application.dto.response.OrderResponse;
import com.sa.event_mng.modules.ordering.domain.model.Order;
import com.sa.event_mng.modules.ordering.domain.model.OrderItem;
import com.sa.event_mng.modules.ticketing.application.dto.response.TicketResponse;
import com.sa.event_mng.modules.ticketing.domain.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "items", source = "items")
    @Mapping(target = "tickets", source = "tickets")
    OrderResponse toOrderResponse(Order order);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items);

    @Mapping(target = "ticketTypeId", source = "ticketType.id")
    @Mapping(target = "ticketTypeName", source = "ticketType.name")
    @Mapping(target = "eventId", source = "ticketType.event.id")
    @Mapping(target = "eventName", source = "ticketType.event.name")
    @Mapping(target = "eventLocation", source = "ticketType.event.location")
    @Mapping(target = "eventProvince", source = "ticketType.event.province")
    @Mapping(target = "eventStartTime", source = "ticketType.event.startTime")
    @Mapping(target = "eventEndTime", source = "ticketType.event.endTime")
    @Mapping(target = "eventImage", expression = "java(orderItem.getTicketType() != null && orderItem.getTicketType().getEvent() != null && orderItem.getTicketType().getEvent().getImages() != null && !orderItem.getTicketType().getEvent().getImages().isEmpty() ? orderItem.getTicketType().getEvent().getImages().get(0).getImageUrl() : null)")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    List<TicketResponse> toTicketResponseList(List<Ticket> tickets);

    @Mapping(target = "eventName", source = "ticketType.event.name")
    @Mapping(target = "ticketTypeName", source = "ticketType.name")
    TicketResponse toTicketResponse(Ticket ticket);
}
