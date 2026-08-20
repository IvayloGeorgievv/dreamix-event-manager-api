package org.example.eventmanagementapi.venue;

import org.example.eventmanagementapi.building.BuildingService;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.building.Building;
import org.example.eventmanagementapi.venue.dto.VenueRequestDTO;
import org.example.eventmanagementapi.venue.dto.VenueResponseDTO;
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
public class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private BuildingService buildingService;

    @Spy
    private VenueMapper venueMapper = Mappers.getMapper(VenueMapper.class);

    @InjectMocks
    private VenueServiceImpl venueService;

    private UUID venueId;
    private UUID buildingId;
    private Venue venue;

    @BeforeEach
    void setUp() {
        venueId = UUID.randomUUID();
        buildingId = UUID.randomUUID();

        Building building = new Building("Main Hall", "Sofia", "Center 1");
        ReflectionTestUtils.setField(building, "id", buildingId);

        venue = new Venue("Stage A", 100, building);
        ReflectionTestUtils.setField(venue, "id", venueId);
    }

    @Test
    @DisplayName("Throw BusinessLogicException when trying to change the Building of an existing Venue")
    void updateVenue_ShouldThrowException_WhenBuildingIsChanged() {
        UUID differentBuildingId = UUID.randomUUID();
        VenueRequestDTO request = new VenueRequestDTO("Stage B", 150, differentBuildingId);

        when(venueRepository.findByIdAndDeletedFalse(venueId)).thenReturn(Optional.of(venue));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> venueService.updateVenue(venueId, request)
        );

        assertEquals("Cannot change the building of an existing venue!", exception.getMessage());
        verify(venueMapper, never()).updateVenueFromDto(any(), any());
    }

    @Test
    @DisplayName("Successfully create Venue and register it with its Building")
    void createVenue_ShouldSucceed_AndRegisterVenueWithBuilding() {
        Building building = new Building("Main Hall", "Sofia", "Center 1");
        ReflectionTestUtils.setField(building, "id", buildingId);

        Venue savedVenue = new Venue("Stage C", 300, building);
        ReflectionTestUtils.setField(savedVenue, "id", venueId);

        VenueRequestDTO request = new VenueRequestDTO("Stage C", 300, buildingId);

        when(buildingService.getBuildingEntityById(buildingId)).thenReturn(building);
        when(venueRepository.save(any(Venue.class))).thenReturn(savedVenue);

        VenueResponseDTO response = venueService.createVenue(request);

        assertNotNull(response);
        assertEquals("Stage C", response.name());
        assertEquals(300, response.capacity());
        verify(venueRepository, times(1)).save(any(Venue.class));
    }

    @Test
    @DisplayName("Throw BusinessLogicException when restoring a Venue that is not deleted")
    void restoreVenue_ShouldThrowException_WhenNotDeleted() {
        when(venueRepository.findById(venueId)).thenReturn(Optional.of(venue));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> venueService.restoreVenue(venueId)
        );

        assertEquals("Venue is not deleted, nothing to restore!", exception.getMessage());
    }
}

