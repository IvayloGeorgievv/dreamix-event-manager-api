package org.example.eventmanagementapi.mapper;

import org.example.eventmanagementapi.dto.customer.CustomerRequestDTO;
import org.example.eventmanagementapi.dto.customer.CustomerResponseDTO;
import org.example.eventmanagementapi.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponseDTO toResponseDTO(Customer customer);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCustomerFromDto(CustomerRequestDTO customerRequestDTO, @MappingTarget Customer customer);
}