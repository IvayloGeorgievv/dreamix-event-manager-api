package org.example.eventmanagementapi.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    @Override
    @EntityGraph(attributePaths = {"venue", "venue.building", "performers"})
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);

    Optional<Event> findByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndDeletedFalse(UUID id);

    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN FETCH e.venue v
            JOIN FETCH v.building
            LEFT JOIN FETCH e.performers
            WHERE LOWER(e.title) = LOWER(:title)
            AND e.dateAndTime >= :now
            AND e.deleted = false
            ORDER BY e.dateAndTime ASC
            """)
    List<Event> findSchedulesByTitle(@Param("title") String title, @Param("now") LocalDateTime now);

    @Query("""
            SELECT COALESCE(SUM(t.pricePaid), 0)
            FROM Ticket t
            WHERE t.event.id = :eventId
            AND t.deleted = false
            """)
    BigDecimal calculateTotalRevenueForEvent(@Param("eventId") UUID eventId);
}
