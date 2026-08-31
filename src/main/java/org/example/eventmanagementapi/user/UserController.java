package org.example.eventmanagementapi.user;

import org.example.eventmanagementapi.user.dto.UserRequestDTO;
import org.example.eventmanagementapi.user.dto.UserResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class UserController {

    private final UserService customerService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getCustomerById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllCustomers(
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        return ResponseEntity.ok(customerService.getAllCustomers(includeDeleted));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateCustomer(@PathVariable UUID id, @Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {

        customerService.softDeleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteCustomer(@PathVariable UUID id) {

        customerService.hardDeleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<UserResponseDTO> restoreCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.restoreCustomer(id));
    }
}