package org.example.eventmanagementapi.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.event.dto.EventRequestDTO;
import org.example.eventmanagementapi.event.dto.EventSummaryResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/events - Successfully create event")
    void createEvent_ShouldReturnCreated_WhenValidPayload() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID venueId = UUID.randomUUID();
        LocalDateTime eventDate = LocalDateTime.now().plusDays(10);

        EventRequestDTO request = new EventRequestDTO(
                "Event",
                BigDecimal.valueOf(80.0),
                eventDate,
                venueId,
                List.of()
        );

        EventSummaryResponseDTO response = new EventSummaryResponseDTO(
                eventId,
                "Event",
                BigDecimal.valueOf(80.0),
                eventDate,
                venueId,
                "Grand Hall",
                "Sofia",
                List.of()
        );

        when(eventService.createEvent(any(EventRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.title").value("Event"))
                .andExpect(jsonPath("$.basePrice").value(80.0));

    }

    @Test
    @DisplayName("GET /api/events/{id} - Should return 404 when not found")
    void getEventById_ShouldReturnNotFound_WhenEventDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        when(eventService.getEventById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Active event not found with ID: " + nonExistentId));

        mockMvc.perform(get("/api/events/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Active event not found"));
    }
}
