package org.example.eventmanagementapi.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.dto.event.EventRequestDTO;
import org.example.eventmanagementapi.dto.event.EventResponseDTO;
import org.example.eventmanagementapi.dto.event.EventSummaryResponseDTO;
import org.example.eventmanagementapi.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventSummaryResponseDTO> createEvent(@Valid @RequestBody EventRequestDTO request) {
        EventSummaryResponseDTO created = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping
    public ResponseEntity<List<EventSummaryResponseDTO>> getAllEvents(
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        return ResponseEntity.ok(eventService.getAllEvents(includeDeleted));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventSummaryResponseDTO>> getUpcomingEvents() {
        return ResponseEntity.ok(eventService.getUpcomingEvents());
    }

    @GetMapping("/available")
    public ResponseEntity<List<EventSummaryResponseDTO>> getAvailableEvents() {
        return ResponseEntity.ok(eventService.getAvailableEvents());
    }

    @GetMapping("/by-city")
    public ResponseEntity<List<EventSummaryResponseDTO>> getEventsByCity(@RequestParam String city) {
        return ResponseEntity.ok(eventService.getEventsByCity(city));
    }

    @GetMapping("/{eventId}/revenue")
    public ResponseEntity<BigDecimal> getEventRevenue(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.calculateTotalRevenueForEvent(eventId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(@PathVariable UUID id, @Valid @RequestBody EventRequestDTO request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteEvent(@PathVariable UUID id) {
        eventService.softDeleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteEvent(@PathVariable UUID id) {
        eventService.hardDeleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<EventResponseDTO> restoreEvent(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.restoreEvent(id));
    }

    @PostMapping("/{eventId}/performers/{performerId}")
    public ResponseEntity<Void> addPerformerToEvent(@PathVariable UUID eventId, @PathVariable UUID performerId) {
        eventService.addPerformerToEvent(eventId, performerId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{eventId}/performers/{performerId}")
    public ResponseEntity<Void> removePerformerFromEvent(@PathVariable UUID eventId, @PathVariable UUID performerId) {
        eventService.removePerformerFromEvent(eventId, performerId);
        return ResponseEntity.noContent().build();
    }
}
