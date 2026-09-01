package org.example.eventmanagementapi.user;

import org.example.eventmanagementapi.user.dto.UserRequestDTO;
import org.example.eventmanagementapi.user.dto.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDTO getUserById(UUID userId);

    User getUserEntityById(UUID userId);

    List<UserResponseDTO> getAllUsers(boolean includeDeleted);

    UserResponseDTO updateUser(UUID userId, UserRequestDTO request);

    void softDeleteUser(UUID userId);

    void hardDeleteUser(UUID userId);

    UserResponseDTO restoreUser(UUID userId);
}