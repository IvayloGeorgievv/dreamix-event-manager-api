package org.example.eventmanagementapi.customer;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.customer.dto.CustomerRequestDTO;
import org.example.eventmanagementapi.customer.dto.CustomerResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;


    @Override
    @Transactional
    public CustomerResponseDTO registerCustomer(CustomerRequestDTO request) {
        validateEmailUniqueness(request.email());

        Customer customer = new Customer(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber(),
                request.addressLine(),
                request.postalCode()
        );
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponseDTO(savedCustomer);
    }


    @Override
    public CustomerResponseDTO getCustomerById(UUID customerId) {
        return customerMapper.toResponseDTO(getCustomerEntityById(customerId));
    }


    @Override
    public Customer getCustomerEntityById(UUID customerId) {
        return customerRepository.findByIdAndDeletedFalse(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
    }


    @Override
    public List<CustomerResponseDTO> getAllCustomers(boolean includeDeleted) {
        List<Customer> customers = includeDeleted
                ? customerRepository.findAll()
                : customerRepository.findAllByDeletedFalse();

        return customers.stream()
                .map(customerMapper::toResponseDTO)
                .toList();
    }


    @Override
    @Transactional
    public CustomerResponseDTO updateCustomer(UUID customerId, CustomerRequestDTO request) {
        Customer customer = getCustomerEntityById(customerId);
        validateEmailUniquenessForUpdate(customer.getEmail(), request.email());

        customerMapper.updateCustomerFromDto(request, customer);
        return customerMapper.toResponseDTO(customer);
    }


    @Override
    @Transactional
    public void softDeleteCustomer(UUID customerId) {
        Customer customer = getCustomerEntityById(customerId);
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
    public CustomerResponseDTO restoreCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        if (!customer.isDeleted()) {
            throw new BusinessLogicException("Customer is not deleted, nothing to restore!");
        }

        customer.setDeleted(false);
        return customerMapper.toResponseDTO(customer);
    }

    private void validateEmailUniqueness(String email) {
        if (customerRepository.existsByEmailAndDeletedFalse(email)) {
            throw new BusinessLogicException("Customer with this email already exists!");
        }
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
