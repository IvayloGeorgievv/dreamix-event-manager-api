package org.example.eventmanagementapi.ticket;

import org.example.eventmanagementapi.ticket.dto.CustomerTicketResponseDTO;
import org.example.eventmanagementapi.ticket.dto.TicketRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketResponseDTO;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponseDTO> buyTicket(@Valid @RequestBody TicketRequestDTO request) {
        TicketResponseDTO ticket = ticketService.buyTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomerTicketResponseDTO>> getTicketsByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ticketService.getTicketsByCustomer(customerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTicket(@PathVariable UUID id) {

        ticketService.cancelTicket(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteTicket(@PathVariable UUID id) {

        ticketService.hardDeleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<TicketResponseDTO> restoreTicket(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.restoreTicket(id));
    }
}