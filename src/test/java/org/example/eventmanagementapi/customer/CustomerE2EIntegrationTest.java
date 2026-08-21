package org.example.eventmanagementapi.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.eventmanagementapi.customer.dto.CustomerRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CustomerE2EIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("E2E: Register Customer -> Retrieve Customer by ID -> Prevent Duplicate Email")
    void customerLifecycle_E2E() throws Exception {
        CustomerRequestDTO request = new CustomerRequestDTO(
                "Maria",
                "Ivanova",
                "maria.e2e@example.com",
                "0888123456",
                "Vitosha Blvd 10",
                "1000"
        );

        MvcResult result = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("maria.e2e@example.com"))
                .andReturn();

        String customerId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        // 2. Retrieve the persisted customer (HTTP GET -> DB read)
        mockMvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.firstName").value("Maria"));

        // 3. Attempt duplicate email registration -> Expect 409 Conflict
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Customer with this email already exists!"));
    }

}