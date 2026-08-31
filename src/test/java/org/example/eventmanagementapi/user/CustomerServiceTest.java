package org.example.eventmanagementapi.user;

import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.user.dto.UserRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private UserRepository customerRepository;

    @Spy
    private UserMapper customerMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserServiceImpl customerService;

    private UUID customerId;
    private UserRequestDTO customerRequestDTO;
    private User customer;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        customerRequestDTO = new UserRequestDTO(
                "John",
                "Doe",
                "john@gmail.com",
                "0123456789",
                "Main St 1",
                "1000"
        );

        customer = new User(
                customerRequestDTO.firstName(),
                customerRequestDTO.lastName(),
                customerRequestDTO.email(),
                "encodedPassword123!",
                Role.ROLE_CUSTOMER
        );
        ReflectionTestUtils.setField(customer, "id", customerId);
    }

    @Test
    @DisplayName("Throw ResourceNotFoundException when Customer entity is not found by ID")
    void getCustomerEntityById_ShouldThrowException_WhenNotFound() {
        when(customerRepository.findByIdAndDeletedFalse(customerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.getCustomerEntityById(customerId)
        );

        assertEquals("Customer not found with ID: " + customerId, exception.getMessage());
    }
}
