package org.example.eventmanagementapi.user;

import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.user.dto.UserResponseDTO;
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
@AutoConfigureMockMvc(addFilters = false)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/users/{id} - Successfully retrieve user by ID")
    void getUserById_ShouldReturnUser_WhenFound() throws Exception {
        final UUID userId = UUID.randomUUID();
        final UserResponseDTO responseDTO = new UserResponseDTO(
                userId,
                "Alice",
                "Smith",
                "alice@example.com",
                "555-1234",
                "Elm St 10",
                "9000"
        );

        when(userService.getUserById(userId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return 404 when user not found")
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        final UUID nonExistentId = UUID.randomUUID();

        when(userService.getUserById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("User not found with ID: " + nonExistentId));

        mockMvc.perform(get("/api/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
