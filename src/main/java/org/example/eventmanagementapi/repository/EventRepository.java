package org.example.eventmanagementapi.repository;

import org.example.eventmanagementapi.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findAllByDeletedFalse();

    Optional<Event> findByIdAndDeletedFalse(UUID id);
    boolean existsByIdAndDeletedFalse(UUID id);

    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN FETCH e.venue v
            JOIN FETCH v.building
            LEFT JOIN FETCH e.performers
            WHERE e.dateAndTime > :now
            AND e.deleted = false
            ORDER BY e.dateAndTime ASC
            """)
    List<Event> findUpcoming(@Param("now") LocalDateTime now);

    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN FETCH e.venue v
            JOIN FETCH v.building b
            LEFT JOIN FETCH e.performers
            WHERE LOWER(b.city) = LOWER(:city)
            AND e.deleted = false
            """)
    List<Event> findByCity(@Param("city") String city);

    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN FETCH e.venue v
            JOIN FETCH v.building b
            LEFT JOIN FETCH e.performers
            WHERE e.soldTicketsCount < v.capacity
            AND e.dateAndTime > :now
            AND e.deleted = false
            """)
    List<Event> findAvailable(@Param("now") LocalDateTime now);

    @Query("""
            SELECT COALESCE(SUM(t.pricePaid), 0)
            FROM Ticket t
            WHERE t.event.id = :eventId
            AND t.deleted = false
            """)
    BigDecimal calculateTotalRevenueForEvent(@Param("eventId") UUID eventId);
}
