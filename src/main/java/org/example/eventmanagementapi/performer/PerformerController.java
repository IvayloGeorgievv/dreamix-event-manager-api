package org.example.eventmanagementapi.performer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.performer.dto.PerformerRequestDTO;
import org.example.eventmanagementapi.performer.dto.PerformerResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/performers")
@RequiredArgsConstructor
public class PerformerController {

    private final PerformerService performerService;

    @PostMapping
    public ResponseEntity<PerformerResponseDTO> createPerformer(@Valid @RequestBody PerformerRequestDTO request) {
        PerformerResponseDTO created = performerService.createPerformer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformerResponseDTO> getPerformerById(@PathVariable UUID id) {
        return ResponseEntity.ok(performerService.getPerformerById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PerformerResponseDTO>> getPerformers(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(performerService.getPerformers(search, includeDeleted, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerformerResponseDTO> updatePerformer(@PathVariable UUID id, @Valid @RequestBody PerformerRequestDTO request) {
        return ResponseEntity.ok(performerService.updatePerformer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerformer(@PathVariable UUID id) {

        performerService.softDeletePerformer(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeletePerformer(@PathVariable UUID id) {

        performerService.hardDeletePerformer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<PerformerResponseDTO> restorePerformer(@PathVariable UUID id) {
        return ResponseEntity.ok(performerService.restorePerformer(id));
    }
}
