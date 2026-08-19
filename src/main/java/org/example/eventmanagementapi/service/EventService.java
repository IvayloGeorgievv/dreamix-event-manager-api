package org.example.eventmanagementapi.service;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.dto.event.EventRequestDTO;
import org.example.eventmanagementapi.dto.event.EventResponseDTO;
import org.example.eventmanagementapi.dto.event.EventSummaryResponseDTO;
import org.example.eventmanagementapi.event.EventDeletedEvent;
import org.example.eventmanagementapi.exception.BusinessLogicException;
import org.example.eventmanagementapi.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.mapper.EventMapper;
import org.example.eventmanagementapi.model.Event;
import org.example.eventmanagementapi.model.Performer;
import org.example.eventmanagementapi.model.Venue;
import org.example.eventmanagementapi.repository.EventRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final VenueService venueService;
    private final PerformerService performerService;
    private final TicketService ticketService;

    private final EventMapper eventMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public EventSummaryResponseDTO createEvent(EventRequestDTO request) {
        Venue venue = venueService.getVenueEntityById(request.venueId());
        Event event = new Event(venue, request.title(), request.basePrice(), request.dateAndTime());

        if (request.performerIds() != null && !request.performerIds().isEmpty()) {
            Set<UUID> uniquePerformerIds = new HashSet<>(request.performerIds());
            for (UUID performerId : uniquePerformerIds) {
                Performer performer = performerService.getPerformerEntityById(performerId);
                event.addPerformer(performer);
            }
        }

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toSummaryResponseDTO(savedEvent);
    }

    public EventResponseDTO getEventById(UUID id) {
        Event event = getEventEntityById(id);
        return eventMapper.toResponseDTO(event);
    }

    protected Event getEventEntityById(UUID id) {
        return eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active event not found with ID: " + id));
    }

    public List<EventSummaryResponseDTO> getAllEvents(boolean includeDeleted) {
        List<Event> events = includeDeleted
                ? eventRepository.findAll()
                : eventRepository.findAllByDeletedFalse();

        return events.stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }

    public List<EventSummaryResponseDTO> getUpcomingEvents() {
        return eventRepository.findUpcoming(LocalDateTime.now()).stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }

    public List<EventSummaryResponseDTO> getEventsByCity(String city) {
        return eventRepository.findByCity(city).stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }

    public List<EventSummaryResponseDTO> getAvailableEvents() {
        return eventRepository.findAvailable(LocalDateTime.now()).stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }

    @Transactional
    public EventResponseDTO updateEvent(UUID id, EventRequestDTO request) {
        Event event = getEventEntityById(id);

        if (request.dateAndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException("Cannot update event date to a past date");
        }

        Venue venue = venueService.getVenueEntityById(request.venueId());
        eventMapper.updateEventFromDto(request, event);
        event.setVenue(venue);

        return eventMapper.toResponseDTO(event);
    }

    @Transactional
    public void addPerformerToEvent(UUID eventId, UUID performerId) {
        Event event = getEventEntityById(eventId);
        Performer performer = performerService.getPerformerEntityById(performerId);

        if (event.getPerformers().contains(performer)) {
            throw new BusinessLogicException("Performer is already added to this event!");
        }
        event.addPerformer(performer);
    }

    @Transactional
    public void removePerformerFromEvent(UUID eventId, UUID performerId) {
        Event event = getEventEntityById(eventId);
        Performer performer = performerService.getPerformerEntityById(performerId);

        if (!event.getPerformers().contains(performer)) {
            throw new BusinessLogicException("Performer is not associated with this event!");
        }
        event.removePerformer(performer);
    }

    public BigDecimal calculateTotalRevenueForEvent(UUID eventId) {
        getEventEntityById(eventId);
        return eventRepository.calculateTotalRevenueForEvent(eventId);
    }

    @Transactional
    public void softDeleteEvent(UUID eventId) {
        Event event = getEventEntityById(eventId);
        event.setDeleted(true);

        eventPublisher.publishEvent(new EventDeletedEvent(eventId));
    }

    @Transactional
    public void hardDeleteEvent(UUID eventId) {
        validateEventExists(eventId);
        ticketService.deleteTicketsByEventId(eventId);

        eventRepository.deleteById(eventId);
    }

    @Transactional
    public EventResponseDTO restoreEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        if (!event.isDeleted()) {
            throw new BusinessLogicException("Event is not deleted, nothing to restore!");
        }

        event.setDeleted(false);
        return eventMapper.toResponseDTO(event);
    }

    public void validateEventExists(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with ID: " + eventId);
        }
    }
}
