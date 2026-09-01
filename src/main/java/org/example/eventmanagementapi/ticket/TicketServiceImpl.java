package org.example.eventmanagementapi.ticket;

import org.example.eventmanagementapi.user.UserService;
import org.example.eventmanagementapi.user.User;
import org.example.eventmanagementapi.event.EventDeletedEvent;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.event.EventService;
import org.example.eventmanagementapi.event.Event;
import org.example.eventmanagementapi.ticket.dto.UserTicketResponseDTO;
import org.example.eventmanagementapi.ticket.dto.TicketRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketResponseDTO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    private final UserService userService;
    private final EventService eventService;

    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public TicketResponseDTO buyTicket(final TicketRequestDTO request) {
        final User user = userService.getUserEntityById(request.userId());
        final Event event = eventService.getEventEntityById(request.eventId());

        validateTicketPurchase(event, request.seatNumber());

        event.incrementSoldTickets();

        final Ticket ticket = ticketMapper.toEntity(request);
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setPricePaid(event.getBasePrice());
        final Ticket savedTicket = ticketRepository.save(ticket);

        return ticketMapper.toResponseDTO(savedTicket);
    }

    @Override
    public TicketResponseDTO getTicketById(final UUID ticketId) {
        return ticketMapper.toResponseDTO(getTicketEntityById(ticketId));
    }

    @Override
    public List<UserTicketResponseDTO> getTicketsByUser(final UUID userId) {
        return ticketRepository.findByUserIdAndDeletedFalse(userId).stream()
                .map(ticketMapper::toUserTicketDTO)
                .toList();
    }

    @Override
    @Transactional
    public void cancelTicket(final UUID ticketId) {
        final Ticket ticket = getTicketEntityById(ticketId);
        final Event event = ticket.getEvent();

        if (event.getSoldTicketsCount() <= 0) {
            throw new BusinessLogicException("Cannot decrement sold tickets below zero");
        }
        event.decrementSoldTickets();

        ticket.setDeleted(true); // Soft Delete
    }

    //Transactional Event Listener method - Explicitly handling Tickets soft deletion AFTER Event soft deletion
    //Used only on Soft Deletion of Event entity
    // Propagation.REQUIRES_NEW - We need a Transaction to update the Tickets
    // the Transaction for Event update is already closed so default value (Propagation.REQUIRED) won't work
    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleEventDeleted(final EventDeletedEvent event) {
        final List<Ticket> tickets = ticketRepository.findByEventIdAndDeletedFalse(event.eventId());
        tickets.forEach(ticket -> ticket.setDeleted(true));

    }

    @Override
    @Transactional
    public void hardDeleteTicket(final UUID ticketId) {
        final Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));
        ticketRepository.delete(ticket);
    }

    @Override
    @Transactional
    public TicketResponseDTO restoreTicket(final UUID ticketId) {
        final Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        if (!ticket.isDeleted()) {
            throw new BusinessLogicException("Ticket is not deleted, nothing to restore!");
        }

        final Event event = ticket.getEvent();

        if (ticketRepository.existsByEventIdAndSeatNumberAndDeletedFalse(event.getId(), ticket.getSeatNumber())) {
            throw new BusinessLogicException("Seat has since been rebooked; cannot restore this ticket!");
        }
        if (event.getSoldTicketsCount() >= event.getVenue().getCapacity()) {
            throw new BusinessLogicException("No capacity available to restore this ticket!");
        }

        event.incrementSoldTickets();
        ticket.setDeleted(false);
        return ticketMapper.toResponseDTO(ticket);
    }

    private Ticket getTicketEntityById(final UUID ticketId) {
        return ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Active ticket not found with ID: " + ticketId));
    }

    //private validation helper
    private void validateTicketPurchase(final Event event, final String seatNumber) {
        if (!event.getDateAndTime().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessLogicException("Cannot buy a ticket for a past event!");
        }
        if (event.getSoldTicketsCount() >= event.getVenue().getCapacity()) {
            throw new BusinessLogicException("No more capacity available for this venue!");
        }
        if (ticketRepository.existsByEventIdAndSeatNumberAndDeletedFalse(event.getId(), seatNumber)) {
            throw new BusinessLogicException("Seat is already booked!");
        }
    }
}
