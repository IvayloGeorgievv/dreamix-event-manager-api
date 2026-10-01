package org.example.eventmanagementapi.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.event.dto.EventFilterDTO;
import org.example.eventmanagementapi.event.dto.EventRequestDTO;
import org.example.eventmanagementapi.event.dto.EventResponseDTO;
import org.example.eventmanagementapi.event.dto.EventSummaryResponseDTO;
import org.example.eventmanagementapi.event.dto.ShowScheduleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping
    public ResponseEntity<Page<EventSummaryResponseDTO>> getEvents(
            @ModelAttribute EventFilterDTO filter,
            @PageableDefault(size = 12, sort = "dateAndTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(eventService.getEvents(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<ShowScheduleDTO>> getEventSchedules(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getSchedulesForEvent(id));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponseDTO> uploadEventImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(eventService.uploadEventImage(id, file));
    }

    @GetMapping("/{eventId}/revenue")
    public ResponseEntity<BigDecimal> getEventRevenue(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.calculateTotalRevenueForEvent(eventId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody EventRequestDTO request
    ) {
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
    public ResponseEntity<Void> addPerformerToEvent(
            @PathVariable UUID eventId,
            @PathVariable UUID performerId
    ) {
        eventService.addPerformerToEvent(eventId, performerId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{eventId}/performers/{performerId}")
    public ResponseEntity<Void> removePerformerFromEvent(
            @PathVariable UUID eventId,
            @PathVariable UUID performerId
    ) {
        eventService.removePerformerFromEvent(eventId, performerId);
        return ResponseEntity.noContent().build();
    }
}
