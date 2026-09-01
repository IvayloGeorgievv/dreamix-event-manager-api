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
import org.example.eventmanagementapi.venue.VenueService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    private final EventMapper eventMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public EventSummaryResponseDTO createEvent(final EventRequestDTO request) {
        final Venue venue = venueService.getVenueEntityById(request.venueId());
        final Event event = eventMapper.toEntity(request);
        event.setVenue(venue);

        if (request.performerIds() != null && !request.performerIds().isEmpty()) {
            final Set<UUID> uniquePerformerIds = new HashSet<>(request.performerIds());
            for (final UUID performerId : uniquePerformerIds) {
                final Performer performer = performerService.getPerformerEntityById(performerId);
                event.addPerformer(performer);
            }
        }

        final Event savedEvent = eventRepository.save(event);
        return eventMapper.toSummaryResponseDTO(savedEvent);
    }


    @Override
    public EventResponseDTO getEventById(final UUID id) {
        final Event event = getEventEntityById(id);
        return eventMapper.toResponseDTO(event);
    }


    @Override
    public Event getEventEntityById(final UUID id) {
        return eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active event not found with ID: " + id));
    }


    @Override
    public List<EventSummaryResponseDTO> getAllEvents(final boolean includeDeleted) {
        final List<Event> events = includeDeleted
                ? eventRepository.findAll()
                : eventRepository.findAllByDeletedFalse();

        return events.stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }


    @Override
    public List<EventSummaryResponseDTO> getUpcomingEvents() {
        return eventRepository.findUpcoming(LocalDateTime.now(ZoneOffset.UTC)).stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }


    @Override
    public List<EventSummaryResponseDTO> getEventsByCity(final String city) {
        return eventRepository.findByCity(city).stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }

    @Override
    public List<EventSummaryResponseDTO> getAvailableEvents() {
        return eventRepository.findAvailable(LocalDateTime.now(ZoneOffset.UTC)).stream()
                .map(eventMapper::toSummaryResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public EventResponseDTO updateEvent(final UUID id, final EventRequestDTO request) {
        final Event event = getEventEntityById(id);

        if (request.dateAndTime().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessLogicException("Cannot update event date to a past date");
        }

        Venue venue = venueService.getVenueEntityById(request.venueId());
        eventMapper.updateEventFromDto(request, event);
        event.setVenue(venue);

        return eventMapper.toResponseDTO(event);
    }

    @Override
    @Transactional
    public void addPerformerToEvent(final UUID eventId, final UUID performerId) {
        final Event event = getEventEntityById(eventId);
        final Performer performer = performerService.getPerformerEntityById(performerId);

        if (event.getPerformers().contains(performer)) {
            throw new BusinessLogicException("Performer is already added to this event!");
        }

        event.addPerformer(performer);
    }

    @Override
    @Transactional
    public void removePerformerFromEvent(final UUID eventId, final UUID performerId) {
        final Event event = getEventEntityById(eventId);
        final Performer performer = performerService.getPerformerEntityById(performerId);

        if (!event.removePerformer(performer)) {
            throw new BusinessLogicException("Performer is not associated with this event!");
        }
    }

    @Override
    public BigDecimal calculateTotalRevenueForEvent(final UUID eventId) {
        getEventEntityById(eventId);
        return eventRepository.calculateTotalRevenueForEvent(eventId);
    }

    @Override
    @Transactional
    public void softDeleteEvent(final UUID eventId) {
        final Event event = getEventEntityById(eventId);
        event.setDeleted(true);

        eventPublisher.publishEvent(new EventDeletedEvent(eventId));
    }

    @Override
    @Transactional
    public void hardDeleteEvent(final UUID eventId) {
        validateEventExists(eventId);

        eventRepository.deleteById(eventId);
    }

    @Override
    @Transactional
    public EventResponseDTO restoreEvent(final UUID eventId) {
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        if (!event.isDeleted()) {
            throw new BusinessLogicException("Event is not deleted, nothing to restore!");
        }

        event.setDeleted(false);
        return eventMapper.toResponseDTO(event);
    }

    private void validateEventExists(final UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with ID: " + eventId);
        }
    }
}
