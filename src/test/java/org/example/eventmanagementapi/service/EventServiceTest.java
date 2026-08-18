package org.example.eventmanagementapi.service;

import org.example.eventmanagementapi.dto.event.EventRequestDTO;
import org.example.eventmanagementapi.event.EventDeletedEvent;
import org.example.eventmanagementapi.exception.BusinessLogicException;
import org.example.eventmanagementapi.mapper.EventMapper;
import org.example.eventmanagementapi.model.Building;
import org.example.eventmanagementapi.model.Event;
import org.example.eventmanagementapi.model.Venue;
import org.example.eventmanagementapi.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueService venueService;

    @Mock
    private PerformerService performerService;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EventService eventService;

    private UUID eventId;
    private UUID venueId;
    private Event event;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        venueId = UUID.randomUUID();

        Building building = new Building("Main Hall", "Sofia", "Center 1");
        Venue venue = new Venue("Hall A", 200, building);
        ReflectionTestUtils.setField(venue, "id", venueId);

        event = new Event(venue, "Concert", BigDecimal.valueOf(40.0), LocalDateTime.now().plusDays(2));
        ReflectionTestUtils.setField(event, "id", eventId);
    }

    @Test
    @DisplayName("Throw BusinessLogicException when Updating Event date to a past date")
    void updateEvent_ShouldThrowException_WhenDateIsInPast() {

        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        EventRequestDTO request = new EventRequestDTO(
                "New Title",
                BigDecimal.valueOf(50.0),
                pastDate,
                venueId,
                Collections.emptyList()
        );


        when(eventRepository.findByIdAndDeletedFalse(eventId)).thenReturn(Optional.of(event));

        //Act & Assert
        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> eventService.updateEvent(eventId, request)
        );

        assertEquals("Cannot update event date to a past date", exception.getMessage());
        verify(venueService, never()).getVenueEntityById(any());
        verify(eventMapper, never()).updateEventFromDto(any(), any());
    }

    @Test
    @DisplayName("Publish EventDeletedEvent and hard delete from repository")
    void hardDeleteEvent_shouldPublishEventAndCallDelete() {

        //Arrange
        when(eventRepository.existsById(eventId)).thenReturn(true);
        ArgumentCaptor<EventDeletedEvent> eventCaptor = ArgumentCaptor.forClass(EventDeletedEvent.class);

        //Act
        eventService.hardDeleteEvent(eventId);

        //Assert
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        EventDeletedEvent publishedEvent = eventCaptor.getValue();

        assertEquals(eventId, publishedEvent.eventId());
        assertTrue(publishedEvent.hardDelete());
        verify(eventRepository, times(1)).deleteById(eventId);
    }
}
