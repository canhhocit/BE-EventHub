package com.sa.event_mng.modules.event.domain.repository;

import com.sa.event_mng.modules.event.domain.model.Event;
import com.sa.event_mng.modules.event.domain.model.projection.EventStatusStatsProjection;
import com.sa.event_mng.modules.event.domain.model.projection.EventTemporalStatsProjection;
import com.sa.event_mng.modules.event.domain.model.projection.TopEventQuarterProjection;
import com.sa.event_mng.modules.event.domain.model.projection.EventYearlyOverviewProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StatisticsEventRepository extends JpaRepository<Event, Long> {

    @Query(value = """
            SELECT status AS status, COUNT(*) AS count
            FROM events
            WHERE EXTRACT(YEAR FROM start_time) = :year
            AND EXTRACT(QUARTER FROM start_time) = :quarter
            GROUP BY status
            """, nativeQuery = true)
    List<EventStatusStatsProjection> findEventStatusStats(@Param("quarter") Long quarter, @Param("year") Long year);

    @Query(value = """
    SELECT EXTRACT(HOUR FROM e.start_time) AS hourOfDay,
        CASE
            WHEN SUM(t.total_quantity) = 0 THEN 0
            ELSE SUM(t.total_quantity - t.remaining_quantity) * 100.0 / SUM(t.total_quantity)
        END AS percentageOfTicketsSold
    FROM events e
    JOIN ticket_types t ON e.id = t.event_id
    WHERE EXTRACT(ISODOW FROM e.start_time) = :dayOfWeek
    GROUP BY EXTRACT(HOUR FROM e.start_time)
    ORDER BY hourOfDay
    """, nativeQuery = true)
    List<EventTemporalStatsProjection> findEventTemporalStats(@Param("dayOfWeek") Integer dayOfWeek);

    @Query(value = """
    SELECT 
        e.id AS eventId,
        e.name AS eventName,
        e.status AS status,
        e.province AS province,
        CAST(e.start_time AS TEXT) AS startTime,
        (SELECT img.image_url FROM event_images img WHERE img.event_id = e.id ORDER BY img.id ASC LIMIT 1) AS eventImage,
        (SELECT MIN(tt_min.price) FROM ticket_types tt_min WHERE tt_min.event_id = e.id) AS minPrice,
        CAST(COALESCE(tt_agg.tickets_sold, 0) AS INTEGER) AS ticketsSold,
        CAST(COALESCE(tt_agg.occupancy_rate, 0) AS DOUBLE PRECISION) AS occupancyRate,
        COALESCE(paid.total_revenue, tt_agg.estimated_revenue, 0) AS totalRevenue
    FROM events e
    JOIN (
        SELECT 
            event_id,
            SUM(total_quantity - remaining_quantity) AS tickets_sold,
            SUM(total_quantity) AS total_capacity,
            CASE 
                WHEN SUM(total_quantity) = 0 THEN 0
                ELSE ROUND(SUM(total_quantity - remaining_quantity) * 100.0 / SUM(total_quantity), 1)
            END AS occupancy_rate,
            SUM((total_quantity - remaining_quantity) * price) AS estimated_revenue
        FROM ticket_types
        GROUP BY event_id
    ) tt_agg ON tt_agg.event_id = e.id
    LEFT JOIN (
        SELECT 
            tt.event_id,
            SUM(oi.subtotal) AS total_revenue
        FROM order_items oi
        JOIN orders o ON o.id = oi.order_id AND o.payment_status = 'PAID'
        JOIN ticket_types tt ON tt.id = oi.ticket_type_id
        GROUP BY tt.event_id
    ) paid ON paid.event_id = e.id
    WHERE (:quarter = 0 OR EXTRACT(QUARTER FROM e.start_time) = :quarter)
      AND (:year = 0 OR EXTRACT(YEAR FROM e.start_time) = :year)
      AND e.status IN ('OPENING', 'COMPLETED')
    ORDER BY totalRevenue DESC, ticketsSold DESC, occupancyRate DESC
    LIMIT 5
    """, nativeQuery = true)
    List<TopEventQuarterProjection> findTop5EventsByQuarter(@Param("quarter") int quarter, @Param("year") int year);

    @Query(value = """
    SELECT 
        e.id AS eventId,
        e.name AS eventName,
        e.status AS status,
        e.province AS province,
        CAST(e.start_time AS TEXT) AS startTime,
        (SELECT img.image_url FROM event_images img WHERE img.event_id = e.id ORDER BY img.id ASC LIMIT 1) AS eventImage,
        (SELECT MIN(tt_min.price) FROM ticket_types tt_min WHERE tt_min.event_id = e.id) AS minPrice,
        CAST(COALESCE(tt_agg.tickets_sold, 0) AS INTEGER) AS ticketsSold,
        CAST(COALESCE(tt_agg.occupancy_rate, 0) AS DOUBLE PRECISION) AS occupancyRate,
        COALESCE(paid.total_revenue, tt_agg.estimated_revenue, 0) AS totalRevenue
    FROM events e
    JOIN (
        SELECT 
            event_id,
            SUM(total_quantity - remaining_quantity) AS tickets_sold,
            SUM(total_quantity) AS total_capacity,
            CASE 
                WHEN SUM(total_quantity) = 0 THEN 0
                ELSE ROUND(SUM(total_quantity - remaining_quantity) * 100.0 / SUM(total_quantity), 1)
            END AS occupancy_rate,
            SUM((total_quantity - remaining_quantity) * price) AS estimated_revenue
        FROM ticket_types
        GROUP BY event_id
    ) tt_agg ON tt_agg.event_id = e.id
    LEFT JOIN (
        SELECT 
            tt.event_id,
            SUM(oi.subtotal) AS total_revenue
        FROM order_items oi
        JOIN orders o ON o.id = oi.order_id AND o.payment_status = 'PAID'
        JOIN ticket_types tt ON tt.id = oi.ticket_type_id
        GROUP BY tt.event_id
    ) paid ON paid.event_id = e.id
    WHERE e.status = 'OPENING'
    ORDER BY ticketsSold DESC, occupancyRate DESC, totalRevenue DESC
    LIMIT 5
    """, nativeQuery = true)
    List<TopEventQuarterProjection> findTopOpeningEvents();

    @Query(value = """
    SELECT
        e.name as eventName,
        e.status as status,
        COALESCE(SUM(oi.quantity), 0) as ticketsSold,
        SUM(tt.total_quantity) as totalQuantity,
        COALESCE(SUM(o.organizer_amount), 0) as organizerAmount
    FROM events e
    JOIN ticket_types tt ON tt.event_id = e.id
    LEFT JOIN order_items oi ON oi.ticket_type_id = tt.id
    LEFT JOIN orders o ON o.id = oi.order_id AND o.payment_status = 'PAID'
    WHERE e.organizer_id = :organizerId
    AND EXTRACT(YEAR FROM e.start_time) = :year
    GROUP BY e.id, e.name, e.status
    ORDER BY e.start_time ASC
    """, nativeQuery = true)
    List<EventYearlyOverviewProjection> findEventYearlyOverview(@Param("organizerId") Long organizerId, @Param("year") int year);
}
