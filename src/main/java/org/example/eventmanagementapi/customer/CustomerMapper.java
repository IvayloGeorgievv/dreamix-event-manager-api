package org.example.eventmanagementapi.customer;

import org.example.eventmanagementapi.customer.dto.CustomerRequestDTO;
import org.example.eventmanagementapi.customer.dto.CustomerResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
interface CustomerMapper {

    CustomerResponseDTO toResponseDTO(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateCustomerFromDto(CustomerRequestDTO customerRequestDTO, @MappingTarget Customer customer);
}