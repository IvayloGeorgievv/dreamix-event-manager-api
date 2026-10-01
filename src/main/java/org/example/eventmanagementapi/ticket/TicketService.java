package org.example.eventmanagementapi.ticket;

import org.example.eventmanagementapi.event.EventDeletedEvent;
import org.example.eventmanagementapi.ticket.dto.BatchTicketPurchaseRequestDTO;
import org.example.eventmanagementapi.ticket.dto.UserTicketResponseDTO;
import org.example.eventmanagementapi.ticket.dto.TicketRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TicketService {

    TicketResponseDTO buyTicket(TicketRequestDTO request);

    List<TicketResponseDTO> purchaseTicketsBatch(BatchTicketPurchaseRequestDTO request, String userEmail);

    List<String> getBookedSeatsForEvent(UUID eventId);

    TicketResponseDTO getTicketById(UUID ticketId);

    Page<UserTicketResponseDTO> getTicketsByUser(UUID userId, Pageable pageable);

    Page<UserTicketResponseDTO> getMyTickets(String userEmail, Pageable pageable);

    void cancelTicket(UUID ticketId);

    void hardDeleteTicket(UUID ticketId);

    TicketResponseDTO restoreTicket(UUID ticketId);

    void handleEventDeleted(EventDeletedEvent event);
}