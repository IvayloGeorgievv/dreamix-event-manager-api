package org.example.eventmanagementapi.ticket;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.event.Event;
import org.example.eventmanagementapi.event.EventDeletedEvent;
import org.example.eventmanagementapi.event.EventService;
import org.example.eventmanagementapi.ticket.dto.BatchTicketPurchaseRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketResponseDTO;
import org.example.eventmanagementapi.ticket.dto.UserTicketResponseDTO;
import org.example.eventmanagementapi.user.User;
import org.example.eventmanagementapi.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private static final BigDecimal VIP_MULTIPLIER = new BigDecimal("1.50");
    private static final BigDecimal PARTERRE_MULTIPLIER = new BigDecimal("1.20");
    private static final BigDecimal BALCONY_MULTIPLIER = BigDecimal.ONE;

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final EventService eventService;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public TicketResponseDTO buyTicket(final TicketRequestDTO request) {
        final User user = userService.getUserEntityById(request.userId());
        final Event event = eventService.getEventEntityById(request.eventId());
        final String normalizedSeat = request.seatNumber().trim().toUpperCase();

        validateEventTimingAndCapacity(event);

        final BigDecimal calculatedPrice = calculateSeatPrice(event.getBasePrice(), normalizedSeat);
        final Ticket ticket = createOrReuseCanceledTicket(user, event, normalizedSeat, calculatedPrice);

        event.incrementSoldTickets();
        final Ticket savedTicket = ticketRepository.save(ticket);

        return ticketMapper.toResponseDTO(savedTicket);
    }

    @Override
    @Transactional
    public List<TicketResponseDTO> purchaseTicketsBatch(
            final BatchTicketPurchaseRequestDTO request,
            final String userEmail
    ) {
        final User user = userService.getUserEntityByEmail(userEmail);
        final Event event = eventService.getEventEntityById(request.eventId());

        // Update optional billing details on user profile if provided at checkout
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            user.setPhoneNumber(request.phoneNumber().trim());
        }
        if (request.address() != null && !request.address().isBlank()) {
            user.setAddress(request.address().trim());
        }
        if (request.postalCode() != null && !request.postalCode().isBlank()) {
            user.setPostalCode(request.postalCode().trim());
        }

        final Set<String> uniqueSeats = new LinkedHashSet<>();
        for (final String rawSeat : request.seatNumbers()) {
            final String normalized = rawSeat.trim().toUpperCase();
            if (!uniqueSeats.add(normalized)) {
                throw new BusinessLogicException("Duplicate seat selected in the same order: " + normalized);
            }
        }

        final List<Ticket> ticketsToSave = new ArrayList<>();
        for (final String seatNumber : uniqueSeats) {
            validateEventTimingAndCapacity(event);


            final BigDecimal seatPrice = calculateSeatPrice(event.getBasePrice(), seatNumber);
            Ticket ticket = createOrReuseCanceledTicket(user, event, seatNumber, seatPrice);

            event.incrementSoldTickets();
            ticketsToSave.add(ticket);
        }

        return ticketRepository.saveAll(ticketsToSave).stream()
                .map(ticketMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<String> getBookedSeatsForEvent(final UUID eventId) {
        eventService.getEventEntityById(eventId);
        return ticketRepository.findBookedSeatNumbersByEventId(eventId);
    }

    @Override
    public TicketResponseDTO getTicketById(final UUID ticketId) {
        return ticketMapper.toResponseDTO(getTicketEntityById(ticketId));
    }

    @Override
    public Page<UserTicketResponseDTO> getTicketsByUser(final UUID userId, final Pageable pageable) {
        return ticketRepository.findByUserIdAndDeletedFalse(userId, pageable)
                .map(ticketMapper::toUserTicketDTO);
    }

    @Override
    public Page<UserTicketResponseDTO> getMyTickets(final String userEmail, final Pageable pageable) {
        final User user = userService.getUserEntityByEmail(userEmail);
        return ticketRepository.findByUserIdAndDeletedFalse(user.getId(), pageable)
                .map(ticketMapper::toUserTicketDTO);
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
        ticket.setDeleted(true);
    }

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

    private void validateEventTimingAndCapacity(final Event event) {
        if (!event.getDateAndTime().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessLogicException("Cannot buy ta ticket for a past event!");
        }

        if (event.getSoldTicketsCount() >= event.getVenue().getCapacity()) {
            throw new BusinessLogicException("No more capacity available for this venue!");
        }
    }

    private Ticket createOrReuseCanceledTicket(final User user, final Event event, final String seatNumber, final BigDecimal seatPrice) {
        return ticketRepository.findByEventIdAndSeatNumber(event.getId(), seatNumber)
                .map(existingTicket -> {
                    if (!existingTicket.isDeleted()) {
                        throw new BusinessLogicException("Seat" + seatNumber + " is already booked!");
                    }
                    existingTicket.setUser(user);
                    existingTicket.setPricePaid(seatPrice);
                    existingTicket.setDeleted(false);
                    return existingTicket;
                })
                .orElseGet(() -> new Ticket(user, event, seatNumber, seatPrice));
    }

    private BigDecimal calculateSeatPrice(final BigDecimal basePrice, final String seatNumber) {
        if (seatNumber == null || seatNumber.isBlank()) {
            return basePrice;
        }
        final char row = Character.toUpperCase(seatNumber.trim().charAt(0));
        final BigDecimal multiplier;
        if (row == 'A' || row == 'B') {
            multiplier = VIP_MULTIPLIER;
        } else if (row >= 'C' && row <= 'F') {
            multiplier = PARTERRE_MULTIPLIER;
        } else {
            multiplier = BALCONY_MULTIPLIER;
        }
        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
