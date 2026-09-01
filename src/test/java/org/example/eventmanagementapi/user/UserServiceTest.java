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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        final UserRequestDTO userRequestDTO = new UserRequestDTO(
                "John",
                "Doe",
                "john@gmail.com",
                "0123456789",
                "Main St 1",
                "1000"
        );

        final User user = new User(
                userRequestDTO.firstName(),
                userRequestDTO.lastName(),
                userRequestDTO.email(),
                "encodedPassword123!",
                Role.ROLE_USER
        );
        ReflectionTestUtils.setField(user, "id", userId);
    }

    @Test
    @DisplayName("Throw ResourceNotFoundException when User entity is not found by ID")
    void getUserEntityById_ShouldThrowException_WhenNotFound() {
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        final ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserEntityById(userId)
        );

        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }
}
