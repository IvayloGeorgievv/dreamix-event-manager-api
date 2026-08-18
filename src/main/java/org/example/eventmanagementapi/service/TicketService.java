package org.example.eventmanagementapi.service;

import org.example.eventmanagementapi.dto.ticket.CustomerTicketResponseDTO;
import org.example.eventmanagementapi.dto.ticket.TicketRequestDTO;
import org.example.eventmanagementapi.dto.ticket.TicketResponseDTO;
import org.example.eventmanagementapi.event.EventDeletedEvent;
import org.example.eventmanagementapi.exception.BusinessLogicException;
import org.example.eventmanagementapi.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.mapper.TicketMapper;
import org.example.eventmanagementapi.model.Customer;
import org.example.eventmanagementapi.model.Event;
import org.example.eventmanagementapi.model.Ticket;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;

    private final CustomerService customerService;
    private final EventService eventService;

    private final TicketMapper ticketMapper;

    @Transactional
    public TicketResponseDTO buyTicket(TicketRequestDTO request) {
        Customer customer = customerService.getCustomerEntityById(request.customerId());
        Event event = eventService.getEventEntityById(request.eventId());

        validateTicketPurchase(event, request.seatNumber());

        event.incrementSoldTickets();

        Ticket ticket = new Ticket(customer, event, request.seatNumber());
        Ticket savedTicket = ticketRepository.save(ticket);

        return ticketMapper.toResponseDTO(savedTicket);
    }

    public TicketResponseDTO getTicketById(UUID ticketId) {
        return ticketMapper.toResponseDTO(getTicketEntityById(ticketId));
    }

    public List<CustomerTicketResponseDTO> getTicketsByCustomer(UUID customerId) {
        return ticketRepository.findByCustomerIdAndDeletedFalse(customerId).stream()
                .map(ticketMapper::toCustomerTicketDTO)
                .toList();
    }

    @Transactional
    public void cancelTicket(UUID ticketId) {
        Ticket ticket = getTicketEntityById(ticketId);
        Event event = ticket.getEvent();

        if (event.getSoldTicketsCount() <= 0) {
            throw new BusinessLogicException("Cannot decrement sold tickets below zero");
        }
        event.decrementSoldTickets();

        ticket.setDeleted(true); // Soft Delete
    }

    @Transactional
    public void hardDeleteTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));
        ticketRepository.delete(ticket);
    }

    @Transactional
    public TicketResponseDTO restoreTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        if (!ticket.isDeleted()) {
            throw new BusinessLogicException("Ticket is not deleted, nothing to restore!");
        }

        Event event = ticket.getEvent();

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

    @Transactional
    public void deleteTicketsByEventId(UUID eventId) {
        ticketRepository.deleteByEventId(eventId);
    }

    private Ticket getTicketEntityById(UUID ticketId) {
        return ticketRepository.findByIdAndDeletedFalse(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Active ticket not found with ID: " + ticketId));
    }

    //Event Listener method - Handling Event Deletion and how to handle tickets
    @Transactional
    @EventListener
    public void handleEventDeleted(EventDeletedEvent event) {
        if(event.hardDelete()) {

            ticketRepository.deleteByEventId(event.eventId());
        } else {
            List<Ticket> tickets = ticketRepository.findByEventIdAndDeletedFalse(event.eventId());
            tickets.forEach(ticket -> ticket.setDeleted(true));
        }
    }

    //private validation helper
    private void validateTicketPurchase(Event event, String seatNumber) {
        if (!event.getDateAndTime().isAfter(LocalDateTime.now())) {
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
