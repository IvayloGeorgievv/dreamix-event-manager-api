package org.example.eventmanagementapi.service;

import org.example.eventmanagementapi.building.*;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
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
public class BuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Spy
    private BuildingMapper buildingMapper = Mappers.getMapper(BuildingMapper.class);

    @InjectMocks
    private BuildingService buildingService;

    private UUID buildingId;
    private Building building;

    @BeforeEach
    void setUp() {
        buildingId = UUID.randomUUID();
        building = new Building("Main Hall", "Sofia", "Center 1");
        ReflectionTestUtils.setField(building, "id", buildingId);
    }

    @Test
    @DisplayName("Throw BusinessLogicException when restoring a Buidling that is not Soft Deleted")
    void restoreBuilding_ShouldThrowException_WhenNotDeleted() {
        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> buildingService.restoreBuilding(buildingId)
        );

        assertEquals("Building is not deleted, nothing to restore!", exception.getMessage());
    }

    @Test
    @DisplayName("Throw ResourceNotFoundException when hard deleting a non-existent Building")
    void hardDeleteBuilding_ShouldThrowException_WhenNotFound() {
        when(buildingRepository.existsById(buildingId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> buildingService.hardDeleteBuilding(buildingId)
        );

        assertEquals("Building not found with ID: " + buildingId, exception.getMessage());
        verify(buildingRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Successfully update Building fields via BuildingMapper")
    void updateBuilding_ShouldSucceed_WhenValidRequest() {
        when(buildingRepository.findByIdAndDeletedFalse(buildingId)).thenReturn(Optional.of(building));

        BuildingRequestDTO request = new BuildingRequestDTO(
                "New Name",
                "Plovdiv",
                "Center 5"
        );

        BuildingResponseDTO response = buildingService.updateBuilding(buildingId, request);

        assertEquals("New Name", response.name());
        assertEquals("Plovdiv", response.city());
        assertEquals("Center 5", response.address());
    }
}
