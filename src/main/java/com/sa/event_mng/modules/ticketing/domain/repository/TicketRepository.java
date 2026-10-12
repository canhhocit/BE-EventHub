package com.sa.event_mng.modules.ticketing.domain.repository;

import com.sa.event_mng.modules.ticketing.domain.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketCode(String ticketCode);
}
