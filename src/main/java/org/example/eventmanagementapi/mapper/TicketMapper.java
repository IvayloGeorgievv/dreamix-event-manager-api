package org.example.eventmanagementapi.mapper;

import org.example.eventmanagementapi.dto.ticket.CustomerTicketResponseDTO;
import org.example.eventmanagementapi.dto.ticket.TicketRequestDTO;
import org.example.eventmanagementapi.dto.ticket.TicketResponseDTO;
import org.example.eventmanagementapi.model.Customer;
import org.example.eventmanagementapi.model.Event;
import org.example.eventmanagementapi.model.Ticket;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(ticket.getCustomer().getFirstName() + \" \" + ticket.getCustomer().getLastName())")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    TicketResponseDTO toResponseDTO(Ticket ticket);

    @Mapping(target = "ticketId", source = "id")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    CustomerTicketResponseDTO toCustomerTicketDTO(Ticket ticket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTicketFromDto(TicketRequestDTO ticketRequestDTO, @MappingTarget Ticket ticket);
}
