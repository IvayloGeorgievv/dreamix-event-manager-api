package org.example.eventmanagementapi.ticket;

import org.example.eventmanagementapi.event.EventDeletedEvent;
import org.example.eventmanagementapi.ticket.dto.CustomerTicketResponseDTO;
import org.example.eventmanagementapi.ticket.dto.TicketRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TicketService {

    TicketResponseDTO buyTicket(TicketRequestDTO request);

    TicketResponseDTO getTicketById(UUID ticketId);

    List<CustomerTicketResponseDTO> getTicketsByCustomer(UUID customerId);

    void cancelTicket(UUID ticketId);

    void hardDeleteTicket(UUID ticketId);

    TicketResponseDTO restoreTicket(UUID ticketId);

    void handleEventDeleted(EventDeletedEvent event);
}