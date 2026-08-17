package org.example.eventmanagementapi.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.dto.building.BuildingRequestDTO;
import org.example.eventmanagementapi.dto.building.BuildingResponseDTO;
import org.example.eventmanagementapi.service.BuildingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @PostMapping
    public ResponseEntity<BuildingResponseDTO> createBuilding(@Valid @RequestBody BuildingRequestDTO request) {
        BuildingResponseDTO created = buildingService.createBuilding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuildingResponseDTO> getBuildingById(@PathVariable UUID id) {
        return ResponseEntity.ok(buildingService.getBuildingById(id));
    }

    @GetMapping
    public ResponseEntity<List<BuildingResponseDTO>> getAllBuildings(
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        return ResponseEntity.ok(buildingService.getAllBuildings(includeDeleted));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuildingResponseDTO> updateBuilding(@PathVariable UUID id, @Valid @RequestBody BuildingRequestDTO request) {
        return ResponseEntity.ok(buildingService.updateBuilding(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuilding(@PathVariable UUID id) {

        buildingService.softDeleteBuilding(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteBuilding(@PathVariable UUID id) {

        buildingService.hardDeleteBuilding(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<BuildingResponseDTO> restoreBuilding(@PathVariable UUID id) {
        return ResponseEntity.ok(buildingService.restoreBuilding(id));
    }
}