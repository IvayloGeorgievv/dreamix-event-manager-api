package org.example.eventmanagementapi.user;

import org.example.eventmanagementapi.user.dto.UserRequestDTO;
import org.example.eventmanagementapi.user.dto.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDTO getCustomerById(UUID customerId);

    User getCustomerEntityById(UUID customerId);

    List<UserResponseDTO> getAllCustomers(boolean includeDeleted);

    UserResponseDTO updateCustomer(UUID customerId, UserRequestDTO request);

    void softDeleteCustomer(UUID customerId);

    void hardDeleteCustomer(UUID customerId);

    UserResponseDTO restoreCustomer(UUID customerId);
}