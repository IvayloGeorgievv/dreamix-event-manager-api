package org.example.eventmanagementapi.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.ticket.dto.TicketRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Boots the complete Spring Application Context for end-to-end
@SpringBootTest
// Autoconfigures MockMvc to simulate HTTP requests without starting a web server
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerIntegrationTest {
    // Autowired is needed to inject the Spring Beans directly from application context into the test
    // Executes and tests mock HTTP requests against controllers without starting a real network server.
    @Autowired
    private MockMvc mockMvc;

    // Serializes Java objects into JSON strings and deserializes JSON responses back into objects.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Replaces a real Spring Bean in the context with a Mockito mock
    @MockitoBean
    private TicketService ticketService;

    @Test
    @DisplayName("POST /api/tickets - Successfully buy ticket Integration Flow")
    void buyTicket_ShouldReturnCreated_WhenValidPayload() throws Exception {
        //Arrange
        final UUID userId = UUID.randomUUID();
        final UUID eventId = UUID.randomUUID();
        final UUID ticketId = UUID.randomUUID();
        final String seatNumber = "A-10";

        final TicketRequestDTO requestDTO = new TicketRequestDTO(userId, eventId, seatNumber);
        final TicketResponseDTO responseDTO = new TicketResponseDTO(
                ticketId,
                userId,
                "John Doe",
                eventId,
                "Rock Fest",
                seatNumber,
                BigDecimal.valueOf(60.0)
        );

        when(ticketService.buyTicket(any(TicketRequestDTO.class))).thenReturn(responseDTO);

        //Act & Assert (HTTP call)
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.seatNumber").value(seatNumber))
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.pricePaid").value(60.0));
    }

    @Test
    @DisplayName("POST /api/tickets - Should return 409 Conflict when venue capacity is exhausted")
    void buyTicket_ShouldReturnConflict_WhenCapacityExhausted() throws Exception {
        // Arrange
        final UUID userId = UUID.randomUUID();
        final UUID eventId = UUID.randomUUID();
        final String seatNumber = "A-15";

        final TicketRequestDTO ticketRequestDTO = new TicketRequestDTO(userId, eventId, seatNumber);

        when(ticketService.buyTicket(any(TicketRequestDTO.class)))
                .thenThrow(new BusinessLogicException("No more capacity available for this venue!"));

        // Act & Assert
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("No more capacity available for this venue!"))
                .andExpect(jsonPath("$.path").value("/api/tickets"));
    }
}
