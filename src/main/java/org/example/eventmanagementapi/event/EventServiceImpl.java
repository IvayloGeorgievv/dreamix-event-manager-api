package org.example.eventmanagementapi.event;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.event.dto.EventFilterDTO;
import org.example.eventmanagementapi.event.dto.EventRequestDTO;
import org.example.eventmanagementapi.event.dto.EventResponseDTO;
import org.example.eventmanagementapi.event.dto.EventSummaryResponseDTO;
import org.example.eventmanagementapi.event.dto.ShowScheduleDTO;
import org.example.eventmanagementapi.performer.Performer;
import org.example.eventmanagementapi.performer.PerformerService;
import org.example.eventmanagementapi.venue.Venue;
import org.example.eventmanagementapi.venue.VenueService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final Path EVENT_UPLOADS_DIR = Paths.get("uploads", "events");

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
        final List<ShowScheduleDTO> schedules = getSchedulesForEvent(id);
        return eventMapper.toResponseDTO(event, schedules);
    }

    @Override
    public Event getEventEntityById(final UUID id) {
        return eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active event not found with ID: " + id));
    }

    @Override
    public Page<EventSummaryResponseDTO> getEvents(final EventFilterDTO filter, final Pageable pageable) {
        final boolean requestIncludeDeleted = filter != null && Boolean.TRUE.equals(filter.includeDeleted());
        final boolean effectiveIncludeDeleted = requestIncludeDeleted && isCurrentUserAdmin();

        return eventRepository.findAll(EventSpecification.withFilter(filter, effectiveIncludeDeleted), pageable)
                .map(eventMapper::toSummaryResponseDTO);
    }

    @Override
    public List<ShowScheduleDTO> getSchedulesForEvent(final UUID eventId) {
        final Event event = getEventEntityById(eventId);
        return eventRepository.findSchedulesByTitle(event.getTitle(), LocalDateTime.now(ZoneOffset.UTC))
                .stream()
                .map(eventMapper::toScheduleDTO)
                .toList();
    }

    @Override
    @Transactional
    public EventResponseDTO uploadEventImage(final UUID eventId, final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessLogicException("Uploaded image file cannot be empty");
        }

        final Event event = getEventEntityById(eventId);

        try {
            Files.createDirectories(EVENT_UPLOADS_DIR);
            final String originalFilename = file.getOriginalFilename();
            final String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                    : ".jpg";

            final String filename = eventId + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;
            final Path targetPath = EVENT_UPLOADS_DIR.resolve(filename);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            event.setImageUrl("/uploads/events/" + filename);
            final List<ShowScheduleDTO> schedules = getSchedulesForEvent(eventId);
            return eventMapper.toResponseDTO(event, schedules);
        } catch (IOException ex) {
            throw new BusinessLogicException("Failed to store event image: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public EventResponseDTO updateEvent(final UUID id, final EventRequestDTO request) {
        final Event event = getEventEntityById(id);

        if (request.dateAndTime().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessLogicException("Cannot update event date to a past date");
        }

        final Venue venue = venueService.getVenueEntityById(request.venueId());
        eventMapper.updateEventFromDto(request, event);
        event.setVenue(venue);

        final List<ShowScheduleDTO> schedules = getSchedulesForEvent(id);
        return eventMapper.toResponseDTO(event, schedules);
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
        final List<ShowScheduleDTO> schedules = getSchedulesForEvent(eventId);
        return eventMapper.toResponseDTO(event, schedules);
    }

    private void validateEventExists(final UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with ID: " + eventId);
        }
    }

    private boolean isCurrentUserAdmin() {
        final org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
