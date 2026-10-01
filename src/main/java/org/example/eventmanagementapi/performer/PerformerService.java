package org.example.eventmanagementapi.performer;

import org.example.eventmanagementapi.performer.dto.PerformerRequestDTO;
import org.example.eventmanagementapi.performer.dto.PerformerResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface PerformerService {

    PerformerResponseDTO createPerformer(PerformerRequestDTO request);

    PerformerResponseDTO getPerformerById(UUID performerId);

    Performer getPerformerEntityById(UUID performerId);

    Page<PerformerResponseDTO> getPerformers(String search, boolean includeDeleted, Pageable pageable);

    PerformerResponseDTO updatePerformer(UUID performerId, PerformerRequestDTO request);

    void softDeletePerformer(UUID performerId);

    void hardDeletePerformer(UUID performerId);

    PerformerResponseDTO restorePerformer(UUID performerId);
}
