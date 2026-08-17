package org.example.eventmanagementapi.mapper;

import org.example.eventmanagementapi.dto.performer.PerformerRequestDTO;
import org.example.eventmanagementapi.dto.performer.PerformerResponseDTO;
import org.example.eventmanagementapi.model.Performer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface PerformerMapper {

    PerformerResponseDTO toResponseDTO(Performer performer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePerformerFromDto(PerformerRequestDTO performerRequestDTO, @MappingTarget Performer performer);

}
