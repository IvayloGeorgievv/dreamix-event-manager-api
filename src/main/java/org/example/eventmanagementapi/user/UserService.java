package org.example.eventmanagementapi.user;

import org.example.eventmanagementapi.user.dto.UserRequestDTO;
import org.example.eventmanagementapi.user.dto.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface UserService {

    UserResponseDTO getUserById(UUID userId);

    User getUserEntityById(UUID userId);

    User getUserEntityByEmail(String email);

    Page<UserResponseDTO> getUsers(String search, boolean includeDeleted, Pageable pageable);

    UserResponseDTO updateUser(UUID userId, UserRequestDTO request);

    UserResponseDTO updateUserRole(UUID userId, Role newRole);

    void softDeleteUser(UUID userId);

    void hardDeleteUser(UUID userId);

    UserResponseDTO restoreUser(UUID userId);
}