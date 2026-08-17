package org.example.eventmanagementapi.repository;

import org.example.eventmanagementapi.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findAllByDeletedFalse();

    Optional<Ticket> findByIdAndDeletedFalse(UUID id);

    List<Ticket> findByCustomerIdAndDeletedFalse(UUID customerId);

    List<Ticket> findByEventIdAndDeletedFalse(UUID eventId);

    boolean existsByEventIdAndSeatNumberAndDeletedFalse(UUID eventId, String seatNumber);

    @Modifying
    @Query("""
            DELETE FROM Ticket t
            WHERE t.event.id = :eventId
            """)
    void deleteByEventId(UUID eventId);
}
