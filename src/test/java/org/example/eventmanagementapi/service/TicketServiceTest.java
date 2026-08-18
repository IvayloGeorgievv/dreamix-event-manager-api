package org.example.eventmanagementapi.service;

import org.example.eventmanagementapi.dto.ticket.TicketRequestDTO;
import org.example.eventmanagementapi.dto.ticket.TicketResponseDTO;
import org.example.eventmanagementapi.event.EventDeletedEvent;
import org.example.eventmanagementapi.exception.BusinessLogicException;
import org.example.eventmanagementapi.mapper.TicketMapper;
import org.example.eventmanagementapi.model.Building;
import org.example.eventmanagementapi.model.Customer;
import org.example.eventmanagementapi.model.Event;
import org.example.eventmanagementapi.model.Ticket;
import org.example.eventmanagementapi.model.Venue;
import org.example.eventmanagementapi.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private EventService eventService;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketService ticketService;

    // Field-Level to be reachable for each Test
    private UUID customerId;
    private UUID eventId;
    private String seatNumber;
    private TicketRequestDTO ticketRequestDTO;
    private Customer customer;
    private Event event;

    @BeforeEach
    void setUp() {
        //Arrange
        customerId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        seatNumber = "A-12";

        ticketRequestDTO = new TicketRequestDTO(customerId, eventId, seatNumber);

        customer = new Customer(
                "John",
                "Doe",
                "john@gmail.com",
                "123456",
                "Street 1",
                "1000"
        );
        ReflectionTestUtils.setField(customer, "id", customerId);

        Building building = new Building(
                "Main Hall",
                "Sofia",
                "Center 1"
        );

        Venue venue = new Venue(
                "Stage A",
                100,
                building
        );

        event = new Event(
                venue,
                "Rock Concert",
                BigDecimal.valueOf(50.0),
                LocalDateTime.now().plusDays(5)
        );

        ReflectionTestUtils.setField(event, "id", eventId);
    }

    @Test
    @DisplayName("Successfully purchase a Ticket when data is valid and there is available capacity")
    void buyTicket_ShouldSucceed() {

        //Arrange for current Test

        Ticket savedTicket = new Ticket(customer, event, seatNumber);

        TicketResponseDTO expectedResponse = new TicketResponseDTO(
                UUID.randomUUID(),
                customer.getId(),
                customer.getFirstName() + " " + customer.getLastName(),
                event.getId(),
                event.getTitle(),
                seatNumber,
                BigDecimal.valueOf(50.0)
        );


        when(customerService.getCustomerEntityById(customerId)).thenReturn(customer);
        when(eventService.getEventEntityById(eventId)).thenReturn(event);
        when(ticketRepository.existsByEventIdAndSeatNumberAndDeletedFalse(eventId, seatNumber)).thenReturn(false);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketMapper.toResponseDTO(savedTicket)).thenReturn(expectedResponse);


        //Act
        TicketResponseDTO actualResponse = ticketService.buyTicket(ticketRequestDTO);

        //Assert
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        assertEquals(1, event.getSoldTicketsCount());

        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Throw a BusinessLogicException when seat is already booked")
    void buyTicket_ShouldThrowException_WhenSeatAlreadyBooked() {

        when(customerService.getCustomerEntityById(customerId)).thenReturn(customer);
        when(eventService.getEventEntityById(eventId)).thenReturn(event);
        when(ticketRepository.existsByEventIdAndSeatNumberAndDeletedFalse(eventId, seatNumber)).thenReturn(true);


        //Act & Assert
        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> ticketService.buyTicket(ticketRequestDTO)
        );

        assertEquals("Seat is already booked!", exception.getMessage());
        assertEquals(0, event.getSoldTicketsCount());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }


    @Test
    @DisplayName("Should soft delete Tickets on Soft Deleting Event")
    void handleEventDeleted_ShouldSoftDeleteTickets_WhenSoftDeletingEvent() {

        //Arrange for current Test

        Ticket ticket1 = new Ticket(customer, event, "B-01");
        Ticket ticket2 = new Ticket(customer, event, "B-02");
        List<Ticket> activeTickets = List.of(ticket1, ticket2);

        when(ticketRepository.findByEventIdAndDeletedFalse(eventId)).thenReturn(activeTickets);

        //Creating the Event triggering Tickets soft deletion
        EventDeletedEvent deletedEvent = new EventDeletedEvent(eventId, false);

        //Act
        ticketService.handleEventDeleted(deletedEvent);

        //Assert
        assertTrue(ticket1.isDeleted());
        assertTrue(ticket2.isDeleted());
        verify(ticketRepository, never()).deleteByEventId(any());
    }
}
