package org.example.eventmanagementapi.customer;

import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.customer.dto.CustomerResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    @DisplayName("GET /api/customers/{id} - Successfully retrieve customer by ID")
    void getCustomerById_ShouldReturnCustomer_WhenFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerResponseDTO responseDTO = new CustomerResponseDTO(
                customerId,
                "Alice",
                "Smith",
                "alice@example.com",
                "555-1234",
                "Elm St 10",
                "9000"
        );

        when(customerService.getCustomerById(customerId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

    }

    @Test
    @DisplayName("GET /api/customers/{id} - Should return 404 when customer not found")
    void getCustomerById_ShouldReturnNotFound_WhenCustomerDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        when(customerService.getCustomerById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Customer not found with ID: " + nonExistentId));

        mockMvc.perform(get("/api/customers/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }
}
