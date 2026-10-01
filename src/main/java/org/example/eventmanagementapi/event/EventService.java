package org.example.eventmanagementapi.event;

import org.example.eventmanagementapi.event.dto.EventFilterDTO;
import org.example.eventmanagementapi.event.dto.EventRequestDTO;
import org.example.eventmanagementapi.event.dto.EventResponseDTO;
import org.example.eventmanagementapi.event.dto.EventSummaryResponseDTO;
import org.example.eventmanagementapi.event.dto.ShowScheduleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface EventService {

    EventSummaryResponseDTO createEvent(EventRequestDTO request);

    EventResponseDTO getEventById(UUID id);

    Event getEventEntityById(UUID id);

    Page<EventSummaryResponseDTO> getEvents(EventFilterDTO filter, Pageable pageable);

    List<ShowScheduleDTO> getSchedulesForEvent(UUID eventId);

    EventResponseDTO uploadEventImage(UUID eventId, MultipartFile file);

    EventResponseDTO updateEvent(UUID id, EventRequestDTO request);

    void addPerformerToEvent(UUID eventId, UUID performerId);

    void removePerformerFromEvent(UUID eventId, UUID performerId);

    BigDecimal calculateTotalRevenueForEvent(UUID eventId);

    void softDeleteEvent(UUID eventId);

    void hardDeleteEvent(UUID eventId);

    EventResponseDTO restoreEvent(UUID eventId);
}