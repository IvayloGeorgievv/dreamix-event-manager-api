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

    private final UserRepository customerRepository;

    private final UserMapper customerMapper;


    @Override
    public UserResponseDTO getCustomerById(UUID customerId) {
        return customerMapper.toResponseDTO(getCustomerEntityById(customerId));
    }


    @Override
    public User getCustomerEntityById(UUID customerId) {
        return customerRepository.findByIdAndDeletedFalse(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
    }


    @Override
    public List<UserResponseDTO> getAllCustomers(boolean includeDeleted) {
        List<User> customers = includeDeleted
                ? customerRepository.findAll()
                : customerRepository.findAllByDeletedFalse();

        return customers.stream()
                .map(customerMapper::toResponseDTO)
                .toList();
    }


    @Override
    @Transactional
    public UserResponseDTO updateCustomer(UUID customerId, UserRequestDTO request) {
        User customer = getCustomerEntityById(customerId);
        validateEmailUniquenessForUpdate(customer.getEmail(), request.email());

        customerMapper.updateCustomerFromDto(request, customer);
        return customerMapper.toResponseDTO(customer);
    }


    @Override
    @Transactional
    public void softDeleteCustomer(UUID customerId) {
        User customer = getCustomerEntityById(customerId);
        customer.setDeleted(true);
    }


    @Override
    @Transactional
    public void hardDeleteCustomer(UUID customerId) {
        validateCustomerExists(customerId);
        customerRepository.deleteById(customerId);
    }


    @Override
    @Transactional
    public UserResponseDTO restoreCustomer(UUID customerId) {
        User customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        if (!customer.isDeleted()) {
            throw new BusinessLogicException("Customer is not deleted, nothing to restore!");
        }

        customer.setDeleted(false);
        return customerMapper.toResponseDTO(customer);
    }

    private void validateEmailUniquenessForUpdate(String currentEmail, String newEmail) {
        if (!currentEmail.equalsIgnoreCase(newEmail) && customerRepository.existsByEmailAndDeletedFalse(newEmail)) {
            throw new BusinessLogicException("Customer with this email already exists!");
        }
    }

    private void validateCustomerExists(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }
    }
}
