package org.example.eventmanagementapi.ticket;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.ticket.dto.BatchTicketPurchaseRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketResponseDTO;
import org.example.eventmanagementapi.ticket.dto.UserTicketResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/purchase")
    public ResponseEntity<List<TicketResponseDTO>> purchaseTicketsBatch(
            @Valid @RequestBody BatchTicketPurchaseRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<TicketResponseDTO> purchased = ticketService.purchaseTicketsBatch(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(purchased);
    }

    @GetMapping("/event/{eventId}/booked-seats")
    public ResponseEntity<List<String>> getBookedSeatsForEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(ticketService.getBookedSeatsForEvent(eventId));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<UserTicketResponseDTO>> getMyTickets(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ticketService.getMyTickets(userDetails.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<UserTicketResponseDTO>> getTicketsByUserId(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ticketService.getTicketsByUser(userId, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTicket(@PathVariable UUID id) {
        ticketService.cancelTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteTicket(@PathVariable UUID id) {
        ticketService.hardDeleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<TicketResponseDTO> restoreTicket(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.restoreTicket(id));
    }
}