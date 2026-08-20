package org.example.eventmanagementapi.service;

import org.example.eventmanagementapi.customer.*;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Spy
    private CustomerMapper customerMapper = Mappers.getMapper(CustomerMapper.class);

    @InjectMocks
    private CustomerService customerService;

    private UUID customerId;
    private CustomerRequestDTO customerRequestDTO;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        customerRequestDTO = new CustomerRequestDTO(
                "John",
                "Doe",
                "john@gmail.com",
                "0123456789",
                "Main St 1",
                "1000"
        );

        customer = new Customer(
                customerRequestDTO.firstName(),
                customerRequestDTO.lastName(),
                customerRequestDTO.email(),
                customerRequestDTO.phoneNumber(),
                customerRequestDTO.addressLine(),
                customerRequestDTO.postalCode()
        );
        ReflectionTestUtils.setField(customer, "id", customerId);
    }

    @Test
    @DisplayName("Successfully register Customer and map response DTO correctly using CustomerMapper")
    void registerCustomer_ShouldSucceed_WhenValidRequest() {
        when(customerRepository.existsByEmailAndDeletedFalse(customerRequestDTO.email())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        //Act
        CustomerResponseDTO actualResponse = customerService.registerCustomer(customerRequestDTO);

        //Assert
        assertNotNull(actualResponse);
        assertEquals(customerId, actualResponse.id());
        assertEquals("John", actualResponse.firstName());
        assertEquals("Doe", actualResponse.lastName());
        assertEquals("john@gmail.com", actualResponse.email());
        assertEquals("0123456789", actualResponse.phoneNumber());

        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Throw BusinessLogicException when registering customer with duplicate email")
    void registerCustomer_ShouldThrowException_WhenEmailAlreadyUsed() {
        when(customerRepository.existsByEmailAndDeletedFalse(customerRequestDTO.email())).thenReturn(true);

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> customerService.registerCustomer(customerRequestDTO)
        );

        assertEquals("Customer with this email already exists!", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
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
