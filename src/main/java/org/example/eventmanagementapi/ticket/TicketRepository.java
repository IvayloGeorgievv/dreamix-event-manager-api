package org.example.eventmanagementapi.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByIdAndDeletedFalse(UUID id);

    Optional<Ticket> findByEventIdAndSeatNumber(UUID eventId, String seatNumber);

    @EntityGraph(attributePaths = {"event", "event.venue", "event.venue.building"})
    Page<Ticket> findByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    List<Ticket> findByEventIdAndDeletedFalse(UUID eventId);

    @Query("""
            SELECT t.seatNumber
            FROM Ticket t
            WHERE t.event.id = :eventId
            AND t.deleted = false
            """)
    List<String> findBookedSeatNumbersByEventId(@Param("eventId") UUID eventId);

    boolean existsByEventIdAndSeatNumberAndDeletedFalse(UUID eventId, String seatNumber);
}
