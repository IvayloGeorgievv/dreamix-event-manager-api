package org.example.eventmanagementapi.event;

import org.example.eventmanagementapi.event.dto.EventRequestDTO;
import org.example.eventmanagementapi.event.dto.EventResponseDTO;
import org.example.eventmanagementapi.event.dto.EventSummaryResponseDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface EventService {

    EventSummaryResponseDTO createEvent(EventRequestDTO request);

    EventResponseDTO getEventById(UUID id);

    Event getEventEntityById(UUID id);

    List<EventSummaryResponseDTO> getAllEvents(boolean includeDeleted);

    List<EventSummaryResponseDTO> getUpcomingEvents();

    List<EventSummaryResponseDTO> getEventsByCity(String city);

    List<EventSummaryResponseDTO> getAvailableEvents();

    EventResponseDTO updateEvent(UUID id, EventRequestDTO request);

    void addPerformerToEvent(UUID eventId, UUID performerId);

    void removePerformerFromEvent(UUID eventId, UUID performerId);

    BigDecimal calculateTotalRevenueForEvent(UUID eventId);

    void softDeleteEvent(UUID eventId);

    void hardDeleteEvent(UUID eventId);

    EventResponseDTO restoreEvent(UUID eventId);
}
