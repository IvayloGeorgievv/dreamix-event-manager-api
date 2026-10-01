package org.example.eventmanagementapi.user;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.auth.RefreshTokenService;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.user.dto.UserRequestDTO;
import org.example.eventmanagementapi.user.dto.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserResponseDTO getUserById(final UUID userId) {
        return userMapper.toResponseDTO(getUserEntityById(userId));
    }

    @Override
    public User getUserEntityById(final UUID userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public User getUserEntityByEmail(final String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Active user not found with email: " + email));
    }

    @Override
    public Page<UserResponseDTO> getUsers(
            final String search,
            final boolean includeDeleted,
            final Pageable pageable
    ) {
        final boolean effectiveIncludeDeleted = includeDeleted && isCurrentUserAdmin();
        final String sanitizedSearch = search != null ? search.trim() : "";

        return userRepository.findUsers(sanitizedSearch, effectiveIncludeDeleted, pageable)
                .map(userMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(final UUID userId, final UserRequestDTO request) {
        final User user = getUserEntityById(userId);
        validateEmailUniquenessForUpdate(user.getEmail(), request.email());

        userMapper.updateUserFromDto(request, user);
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserRole(final UUID userId, final Role newRole) {
        final User user = getUserEntityById(userId);
        if (user.getRole() != newRole) {
            user.setRole(newRole);
            refreshTokenService.incrementUserTokenVersion(user.getEmail());
        }
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public void softDeleteUser(final UUID userId) {
        final User user = getUserEntityById(userId);
        user.setDeleted(true);
        refreshTokenService.incrementUserTokenVersion(user.getEmail());
    }

    @Override
    @Transactional
    public void hardDeleteUser(final UUID userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        refreshTokenService.incrementUserTokenVersion(user.getEmail());
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponseDTO restoreUser(final UUID userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!user.isDeleted()) {
            throw new BusinessLogicException("User is not deleted, nothing to restore!");
        }

        user.setDeleted(false);
        return userMapper.toResponseDTO(user);
    }

    private void validateEmailUniquenessForUpdate(final String currentEmail, final String newEmail) {
        if (!currentEmail.equalsIgnoreCase(newEmail) && userRepository.existsByEmailAndDeletedFalse(newEmail)) {
            throw new BusinessLogicException("User with this email already exists!");
        }
    }

    private boolean isCurrentUserAdmin() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
