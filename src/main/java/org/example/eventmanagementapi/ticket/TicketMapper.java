package org.example.eventmanagementapi.ticket;

import org.example.eventmanagementapi.ticket.dto.UserTicketResponseDTO;
import org.example.eventmanagementapi.ticket.dto.TicketRequestDTO;
import org.example.eventmanagementapi.ticket.dto.TicketResponseDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface TicketMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(ticket.getUser().getFirstName() + \" \" + ticket.getUser().getLastName())")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    TicketResponseDTO toResponseDTO(Ticket ticket);

    @Mapping(target = "ticketId", source = "id")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    UserTicketResponseDTO toUserTicketDTO(Ticket ticket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTicketFromDto(TicketRequestDTO ticketRequestDTO, @MappingTarget Ticket ticket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "pricePaid", ignore = true)
    Ticket toEntity(TicketRequestDTO ticketRequestDTO);
}
