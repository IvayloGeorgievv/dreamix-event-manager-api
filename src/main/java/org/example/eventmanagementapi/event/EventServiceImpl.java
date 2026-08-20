package org.example.eventmanagementapi.event;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.event.dto.EventRequestDTO;
import org.example.eventmanagementapi.event.dto.EventResponseDTO;
import org.example.eventmanagementapi.event.dto.EventSummaryResponseDTO;
import org.example.eventmanagementapi.performer.Performer;
import org.example.eventmanagementapi.venue.Venue;
import org.example.eventmanagementapi.performer.PerformerService;
import org.example.eventmanagementapi.ticket.TicketService;
import org.example.eventmanagementapi.venue.VenueService;
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
public class EventServiceImpl implements EventService{

    private final EventRepository eventRepository;

    private final VenueService venueService;
    private final PerformerService performerService;
    private final TicketService ticketService;

    private final EventMapper eventMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Override
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


    @Override
    public EventResponseDTO getEventById(UUID id) {
        Event event = getEventEntityById(id);
        return eventMapper.toResponseDTO(event);
    }


    @Override
    public Event getEventEntityById(UUID id) {
        return eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active event not found with ID: " + id));
    }


    @Override
    public List<EventSummaryResponseDTO> getAllEvents(boolean includeDeleted) {
        List<Event> events = includeDeleted
                ? eventRepository.findAll()
                : eventRepository.findAllByDeletedFalse();

        return events.stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }


    @Override
    public List<EventSummaryResponseDTO> getUpcomingEvents() {
        return eventRepository.findUpcoming(LocalDateTime.now()).stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }


    @Override
    public List<EventSummaryResponseDTO> getEventsByCity(String city) {
        return eventRepository.findByCity(city).stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }

    @Override
    public List<EventSummaryResponseDTO> getAvailableEvents() {
        return eventRepository.findAvailable(LocalDateTime.now()).stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }

    @Override
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

    @Override
    @Transactional
    public void addPerformerToEvent(UUID eventId, UUID performerId) {
        Event event = getEventEntityById(eventId);
        Performer performer = performerService.getPerformerEntityById(performerId);

        if (event.getPerformers().contains(performer)) {
            throw new BusinessLogicException("Performer is already added to this event!");
        }

        event.addPerformer(performer);
    }

    @Override
    @Transactional
    public void removePerformerFromEvent(UUID eventId, UUID performerId) {
        Event event = getEventEntityById(eventId);
        Performer performer = performerService.getPerformerEntityById(performerId);

        if (!event.removePerformer(performer)) {
            throw new BusinessLogicException("Performer is not associated with this event!");
        }
    }

    @Override
    public BigDecimal calculateTotalRevenueForEvent(UUID eventId) {
        getEventEntityById(eventId);
        return eventRepository.calculateTotalRevenueForEvent(eventId);
    }

    @Override
    @Transactional
    public void softDeleteEvent(UUID eventId) {
        Event event = getEventEntityById(eventId);
        event.setDeleted(true);

        eventPublisher.publishEvent(new EventDeletedEvent(eventId));
    }

    @Override
    @Transactional
    public void hardDeleteEvent(UUID eventId) {
        validateEventExists(eventId);
        ticketService.deleteTicketsByEventId(eventId);

        eventRepository.deleteById(eventId);
    }

    @Override
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

    private void validateEventExists(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with ID: " + eventId);
        }
    }
}
