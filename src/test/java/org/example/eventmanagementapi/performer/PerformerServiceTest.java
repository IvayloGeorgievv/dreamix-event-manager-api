package org.example.eventmanagementapi.performer;

import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.performer.dto.PerformerRequestDTO;
import org.example.eventmanagementapi.performer.dto.PerformerResponseDTO;
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
class PerformerServiceTest {

    @Mock
    private PerformerRepository performerRepository;

    @Spy
    private PerformerMapper performerMapper = Mappers.getMapper(PerformerMapper.class);

    @InjectMocks
    private PerformerServiceImpl performerService;

    private UUID performerId;
    private Performer performer;

    @BeforeEach
    void setUp() {
        performerId = UUID.randomUUID();
        performer = new Performer("The Rolling Notes");
        ReflectionTestUtils.setField(performer, "id", performerId);
    }

    @Test
    @DisplayName("Successfully create Performer and map response DTO correctly using PerformerMapper")
    void createPerformer_ShouldSucceed_AndMapFieldsCorrectly() {
        PerformerRequestDTO request = new PerformerRequestDTO("The Rolling Notes");
        Performer savedPerformer = new Performer("The Rolling Notes");
        ReflectionTestUtils.setField(savedPerformer, "id", performerId);

        when(performerRepository.save(any(Performer.class))).thenReturn(savedPerformer);

        PerformerResponseDTO response = performerService.createPerformer(request);

        assertNotNull(response);
        assertEquals(performerId, response.id());
        assertEquals("The Rolling Notes", response.name());

        verify(performerMapper, times(1)).toEntity(request);
        verify(performerMapper, times(1)).toResponseDTO(savedPerformer);
    }

    @Test
    @DisplayName("Throw ResourceNotFoundException when active Performer is not found by ID")
    void getPerformerEntityById_ShouldThrowException_WhenNotFound() {
        when(performerRepository.findByIdAndDeletedFalse(performerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> performerService.getPerformerById(performerId)
        );

        assertEquals("Active performer not found with ID: " + performerId, exception.getMessage());
    }

    @Test
    @DisplayName("Throw BusinessLogicException when restoring a Performer that is not deleted")
    void restorePerformer_ShouldThrowException_WhenNotDeleted() {
        when(performerRepository.findById(performerId)).thenReturn(Optional.of(performer));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> performerService.restorePerformer(performerId)
        );

        assertEquals("Performer is not deleted, nothing to restore!", exception.getMessage());
    }

    @Test
    @DisplayName("Successfully update Performer name via PerformerMapper")
    void updatePerformer_ShouldSucceed_WhenValidRequest() {
        when(performerRepository.findByIdAndDeletedFalse(performerId)).thenReturn(Optional.of(performer));

        PerformerRequestDTO request = new PerformerRequestDTO("New Stage Name");
        PerformerResponseDTO response = performerService.updatePerformer(performerId, request);

        assertEquals("New Stage Name", response.name());
        assertEquals(performerId, response.id());
    }
}