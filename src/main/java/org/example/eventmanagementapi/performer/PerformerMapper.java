package org.example.eventmanagementapi.performer;

import org.example.eventmanagementapi.performer.dto.PerformerRequestDTO;
import org.example.eventmanagementapi.performer.dto.PerformerResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
interface PerformerMapper {

    PerformerResponseDTO toResponseDTO(Performer performer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePerformerFromDto(PerformerRequestDTO performerRequestDTO, @MappingTarget Performer performer);

}
