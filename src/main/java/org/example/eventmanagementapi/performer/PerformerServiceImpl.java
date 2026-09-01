package org.example.eventmanagementapi.performer;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.performer.dto.PerformerRequestDTO;
import org.example.eventmanagementapi.performer.dto.PerformerResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PerformerServiceImpl implements PerformerService {

    private final PerformerRepository performerRepository;
    private final PerformerMapper performerMapper;

    @Override
    @Transactional
    public PerformerResponseDTO createPerformer(final PerformerRequestDTO request) {
        final Performer performer = performerMapper.toEntity(request);
        final Performer savedPerformer = performerRepository.save(performer);
        return performerMapper.toResponseDTO(savedPerformer);
    }

    @Override
    public PerformerResponseDTO getPerformerById(final UUID performerId) {
        return performerMapper.toResponseDTO(getPerformerEntityById(performerId));
    }

    @Override
    public Performer getPerformerEntityById(final UUID performerId) {
        return performerRepository.findByIdAndDeletedFalse(performerId)
                .orElseThrow(() -> new ResourceNotFoundException("Active performer not found with ID: " + performerId));
    }

    @Override
    public List<PerformerResponseDTO> getAllPerformers(final boolean includeDeleted) {
        final List<Performer> performers = includeDeleted
                ? performerRepository.findAll()
                : performerRepository.findAllByDeletedFalse();

        return performers.stream()
                .map(performerMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PerformerResponseDTO updatePerformer(final UUID performerId, final PerformerRequestDTO request) {
        final Performer performer = getPerformerEntityById(performerId);
        performerMapper.updatePerformerFromDto(request, performer);
        return performerMapper.toResponseDTO(performer);
    }

    @Override
    @Transactional
    public void softDeletePerformer(final UUID performerId) {
        final Performer performer = getPerformerEntityById(performerId);
        performer.setDeleted(true);
    }

    @Override
    @Transactional
    public void hardDeletePerformer(final UUID performerId) {
        validatePerformerExists(performerId);
        performerRepository.deleteById(performerId);
    }

    @Override
    @Transactional
    public PerformerResponseDTO restorePerformer(final UUID performerId) {
        final Performer performer = performerRepository.findById(performerId)
                .orElseThrow(() -> new ResourceNotFoundException("Performer not found with ID: " + performerId));

        if (!performer.isDeleted()) {
            throw new BusinessLogicException("Performer is not deleted, nothing to restore!");
        }

        performer.setDeleted(false);
        return performerMapper.toResponseDTO(performer);
    }

    // Helper validation methods
    private void validatePerformerExists(final UUID performerId) {
        if (!performerRepository.existsById(performerId)) {
            throw new ResourceNotFoundException("Performer not found");
        }
    }
}