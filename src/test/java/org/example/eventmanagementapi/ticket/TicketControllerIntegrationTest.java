package org.example.eventmanagementapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.eventmanagementapi.ticket.TicketRequestDTO;
import org.example.eventmanagementapi.ticket.TicketResponseDTO;
import org.example.eventmanagementapi.ticket.TicketService;
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
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
// Autoconfigures MockMvc to simulate HTTP requests without starting a web server
@AutoConfigureMockMvc
public class TicketControllerIntegrationTest {
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
        UUID customerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        String seatNumber = "A-10";

        TicketRequestDTO requestDTO = new TicketRequestDTO(customerId, eventId, seatNumber);
        TicketResponseDTO responseDTO = new TicketResponseDTO(
                ticketId,
                customerId,
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
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.pricePaid").value(60.0));
    }
}
