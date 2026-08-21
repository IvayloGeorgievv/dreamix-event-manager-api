package org.example.eventmanagementapi.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.eventmanagementapi.building.BuildingService;
import org.example.eventmanagementapi.building.dto.BuildingRequestDTO;
import org.example.eventmanagementapi.building.dto.BuildingResponseDTO;
import org.example.eventmanagementapi.customer.CustomerService;
import org.example.eventmanagementapi.customer.dto.CustomerRequestDTO;
import org.example.eventmanagementapi.customer.dto.CustomerResponseDTO;
import org.example.eventmanagementapi.event.EventService;
import org.example.eventmanagementapi.event.dto.EventRequestDTO;
import org.example.eventmanagementapi.event.dto.EventSummaryResponseDTO;
import org.example.eventmanagementapi.ticket.dto.TicketRequestDTO;
import org.example.eventmanagementapi.venue.VenueService;
import org.example.eventmanagementapi.venue.dto.VenueRequestDTO;
import org.example.eventmanagementapi.venue.dto.VenueResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TicketE2EIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private VenueService venueService;

    @Autowired
    private EventService eventService;

    @Autowired
    private CustomerService customerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("E2E: Buy Ticket flow -> Updates Event sold count -> Rejects duplicate seat booking")
    void buyTicketFlow_E2E() throws Exception {
        // 1. Arrange DB entities през техните public Services
        BuildingResponseDTO building = buildingService.createBuilding(
                new BuildingRequestDTO("Arena Center", "Sofia", "Main Ave 1")
        );

        VenueResponseDTO venue = venueService.createVenue(
                new VenueRequestDTO("Hall 1", 50, building.id())
        );

        EventSummaryResponseDTO event = eventService.createEvent(
                new EventRequestDTO("Rock Odyssey", BigDecimal.valueOf(75.0), LocalDateTime.now().plusDays(10), venue.id(), List.of())
        );

        CustomerResponseDTO customer = customerService.registerCustomer(
                new CustomerRequestDTO("Petar", "Dimitrov", "petar.e2e@example.com", "0899112233", "Shipka 5", "1000")
        );
        TicketRequestDTO ticketRequest = new TicketRequestDTO(customer.id(), event.id(), "A-1");

        // 2. Buy ticket via HTTP POST (E2E изпълнение през пълен стек)
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.seatNumber").value("A-1"))
                .andExpect(jsonPath("$.eventTitle").value("Rock Odyssey"))
                .andExpect(jsonPath("$.customerName").value("Petar Dimitrov"))
                .andExpect(jsonPath("$.pricePaid").value(75.0));

        // 3. Опит за повторна покупка на същото място -> 409 Conflict
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Seat is already booked!"));
    }
}