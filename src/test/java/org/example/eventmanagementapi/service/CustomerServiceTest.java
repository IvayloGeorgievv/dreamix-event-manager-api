package org.example.eventmanagementapi.service;

import org.example.eventmanagementapi.dto.customer.CustomerRequestDTO;
import org.example.eventmanagementapi.exception.BusinessLogicException;
import org.example.eventmanagementapi.mapper.CustomerMapper;
import org.example.eventmanagementapi.model.Customer;
import org.example.eventmanagementapi.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    @DisplayName("Throw BusinessLogicException when registering customer with duplicate email")
    void registerCustomer_ShouldThrowException_WhenEmailAlreadyUsed() {
        //Arrange
        String duplicateEmail = "john@gmail.com";
        CustomerRequestDTO customerRequestDTO = new CustomerRequestDTO(
                "John",
                "Doe",
                duplicateEmail,
                "0123456789",
                "Main St 1",
                "1000"
        );

        when(customerRepository.existsByEmailAndDeletedFalse(duplicateEmail)).thenReturn(true);

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> customerService.registerCustomer(customerRequestDTO)
        );

        assertEquals("Customer with this email already exists!", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
