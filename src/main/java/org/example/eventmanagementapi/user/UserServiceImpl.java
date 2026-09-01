package org.example.eventmanagementapi.user;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.user.dto.UserRequestDTO;
import org.example.eventmanagementapi.user.dto.UserResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;


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
    public List<UserResponseDTO> getAllUsers(final boolean includeDeleted) {
        final List<User> users = includeDeleted
                ? userRepository.findAll()
                : userRepository.findAllByDeletedFalse();

        return users.stream()
                .map(userMapper::toResponseDTO)
                .toList();
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
    public void softDeleteUser(final UUID userId) {
        final User user = getUserEntityById(userId);
        user.setDeleted(true);
    }


    @Override
    @Transactional
    public void hardDeleteUser(final UUID userId) {
        validateUserExists(userId);
        userRepository.deleteById(userId);
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

    private void validateUserExists(final UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }
}
