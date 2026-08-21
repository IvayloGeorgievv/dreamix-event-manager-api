package org.example.eventmanagementapi.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findAllByDeletedFalse();

    Optional<Ticket> findByIdAndDeletedFalse(UUID id);

    List<Ticket> findByCustomerIdAndDeletedFalse(UUID customerId);

    List<Ticket> findByEventIdAndDeletedFalse(UUID eventId);

    boolean existsByEventIdAndSeatNumberAndDeletedFalse(UUID eventId, String seatNumber);
}
