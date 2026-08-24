package org.example.eventmanagementapi.customer;

import org.example.eventmanagementapi.customer.dto.CustomerRequestDTO;
import org.example.eventmanagementapi.customer.dto.CustomerResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponseDTO getCustomerById(UUID customerId);

    Customer getCustomerEntityById(UUID customerId);

    List<CustomerResponseDTO> getAllCustomers(boolean includeDeleted);

    CustomerResponseDTO updateCustomer(UUID customerId, CustomerRequestDTO request);

    void softDeleteCustomer(UUID customerId);

    void hardDeleteCustomer(UUID customerId);

    CustomerResponseDTO restoreCustomer(UUID customerId);
}