package org.example.eventmanagementapi.performer;

import org.example.eventmanagementapi.performer.dto.PerformerRequestDTO;
import org.example.eventmanagementapi.performer.dto.PerformerResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PerformerService {

    PerformerResponseDTO createPerformer(PerformerRequestDTO request);

    PerformerResponseDTO getPerformerById(UUID performerId);

    Performer getPerformerEntityById(UUID performerId);

    List<PerformerResponseDTO> getAllPerformers(boolean includeDeleted);

    PerformerResponseDTO updatePerformer(UUID performerId, PerformerRequestDTO request);

    void softDeletePerformer(UUID performerId);

    void hardDeletePerformer(UUID performerId);

    PerformerResponseDTO restorePerformer(UUID performerId);
}
