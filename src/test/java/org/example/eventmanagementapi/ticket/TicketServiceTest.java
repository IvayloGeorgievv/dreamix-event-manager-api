package org.example.eventmanagementapi.ticket;

import org.example.eventmanagementapi.customer.CustomerService;
import org.example.eventmanagementapi.customer.Role;
import org.example.eventmanagementapi.event.EventDeletedEvent;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.event.EventService;
import org.example.eventmanagementapi.building.Building;
import org.example.eventmanagementapi.customer.Customer;
import org.example.eventmanagementapi.event.Event;
import org.example.eventmanagementapi.ticket.dto.TicketRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketResponseDTO;
import org.example.eventmanagementapi.venue.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private EventService eventService;

    @Spy
    private TicketMapper ticketMapper = Mappers.getMapper(TicketMapper.class);

    @InjectMocks
    private TicketServiceImpl ticketService;

    // Field-Level to be reachable for each Test
    private UUID customerId;
    private UUID eventId;
    private UUID ticketId;
    private String seatNumber;
    private TicketRequestDTO ticketRequestDTO;
    private Customer customer;
    private Event event;

    @BeforeEach
    void setUp() {
        //Arrange
        customerId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        seatNumber = "A-12";

        ticketRequestDTO = new TicketRequestDTO(customerId, eventId, seatNumber);

        customer = new Customer(
                "John",
                "Doe",
                "john@gmail.com",
                "password123!",
                Role.ROLE_CUSTOMER
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
        ReflectionTestUtils.setField(savedTicket, "id", ticketId);

        when(customerService.getCustomerEntityById(customerId)).thenReturn(customer);
        when(eventService.getEventEntityById(eventId)).thenReturn(event);
        when(ticketRepository.existsByEventIdAndSeatNumberAndDeletedFalse(eventId, seatNumber)).thenReturn(false);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);


        //Act
        TicketResponseDTO actualResponse = ticketService.buyTicket(ticketRequestDTO);

        //Assert
        assertNotNull(actualResponse);
        assertEquals(ticketId, actualResponse.id());
        assertEquals(customerId, actualResponse.customerId());
        assertEquals(eventId, actualResponse.eventId());
        assertEquals("John Doe", actualResponse.customerName());
        assertEquals("Rock Concert", actualResponse.eventTitle());
        assertEquals("A-12", actualResponse.seatNumber());
        assertEquals(BigDecimal.valueOf(50.0), actualResponse.pricePaid());

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
        EventDeletedEvent deletedEvent = new EventDeletedEvent(eventId);

        //Act
        ticketService.handleEventDeleted(deletedEvent);

        //Assert
        assertTrue(ticket1.isDeleted());
        assertTrue(ticket2.isDeleted());
    }

    @ParameterizedTest(name = "Run {index} -> Buy ticket for seat: {0}")
    @ValueSource(strings = {"A-01", "VIP-12", "BALCONY-5", "SECTOR-C-99"})
    @DisplayName("Successfully purchase ticket for various seat formats")
    void buyTicket_ShouldSucceed_ForDifferentSeatNumbers(String candidateSeat) {
        // Arrange
        TicketRequestDTO request = new TicketRequestDTO(customerId, eventId, candidateSeat);
        Ticket candidateTicket = new Ticket(customer, event, candidateSeat);
        ReflectionTestUtils.setField(candidateTicket, "id", UUID.randomUUID());

        when(customerService.getCustomerEntityById(customerId)).thenReturn(customer);
        when(eventService.getEventEntityById(eventId)).thenReturn(event);
        when(ticketRepository.existsByEventIdAndSeatNumberAndDeletedFalse(eventId, candidateSeat)).thenReturn(false);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(candidateTicket);

        // Act
        TicketResponseDTO response = ticketService.buyTicket(request);

        // Assert
        assertNotNull(response);
        assertEquals(candidateSeat, response.seatNumber());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Throw BusinessLogicException when trying to buy a Ticket for past Event")
    void buyTicket_ShouldThrowException_WhenPastEvent() {
        event.setDateAndTime(LocalDateTime.now().minusDays(5));

        when(customerService.getCustomerEntityById(customerId)).thenReturn(customer);
        when(eventService.getEventEntityById(eventId)).thenReturn(event);



        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> ticketService.buyTicket(ticketRequestDTO)
        );

        assertEquals("Cannot buy a ticket for a past event!", exception.getMessage());
        assertEquals(0, event.getSoldTicketsCount());
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(ticketRepository, never()).existsByEventIdAndSeatNumberAndDeletedFalse(any(), any());
    }

    @Test
    @DisplayName("Cancel Ticket decrements Tickets Sold count in Event")
    void cancelTicket_ShouldDecrementSoldTickets() {
        Ticket ticket = new Ticket(customer, event, seatNumber);
        ReflectionTestUtils.setField(ticket, "id", ticketId);
        event.incrementSoldTickets();

        when(ticketRepository.findByIdAndDeletedFalse(ticketId)).thenReturn(Optional.of(ticket));

        ticketService.cancelTicket(ticketId);

        assertEquals(0, event.getSoldTicketsCount());
        assertTrue(ticket.isDeleted());
        verify(ticketRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Throw BusinessLogicException when restoring a Ticket whose seat is tacken")
    void restoreTicket_ShouldThrowException_WhenSeatIsRebooked() {
        Ticket ticket = new Ticket(customer, event, seatNumber);
        ReflectionTestUtils.setField(ticket, "id", ticketId);
        ticket.setDeleted(true);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.existsByEventIdAndSeatNumberAndDeletedFalse(eventId, seatNumber)).thenReturn(true);

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> ticketService.restoreTicket(ticketId)
        );

        assertEquals("Seat has since been rebooked; cannot restore this ticket!", exception.getMessage());
        assertEquals(0, event.getSoldTicketsCount());
        assertTrue(ticket.isDeleted());
    }
}
