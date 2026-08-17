package org.example.eventmanagementapi.service;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.dto.performer.PerformerRequestDTO;
import org.example.eventmanagementapi.dto.performer.PerformerResponseDTO;
import org.example.eventmanagementapi.exception.BusinessLogicException;
import org.example.eventmanagementapi.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.mapper.PerformerMapper;
import org.example.eventmanagementapi.model.Performer;
import org.example.eventmanagementapi.repository.PerformerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PerformerService {

    private final PerformerRepository performerRepository;
    private final PerformerMapper performerMapper;


    @Transactional
    public PerformerResponseDTO createPerformer(PerformerRequestDTO request) {
        Performer performer = new Performer(request.name());
        Performer savedPerformer = performerRepository.save(performer);
        return performerMapper.toResponseDTO(savedPerformer);
    }

    public PerformerResponseDTO getPerformerById(UUID performerId) {
        return performerMapper.toResponseDTO(getPerformerEntityById(performerId));
    }

    public Performer getPerformerEntityById(UUID performerId) {
        return performerRepository.findByIdAndDeletedFalse(performerId)
                .orElseThrow(() -> new ResourceNotFoundException("Active performer not found with ID: " + performerId));
    }

    public List<PerformerResponseDTO> getAllPerformers(boolean includeDeleted) {
        List<Performer> performers = includeDeleted
                ? performerRepository.findAll()
                : performerRepository.findAllByDeletedFalse();

        return performers.stream()
                .map(performerMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public PerformerResponseDTO updatePerformer(UUID performerId, PerformerRequestDTO request) {
        Performer performer = getPerformerEntityById(performerId);
        performerMapper.updatePerformerFromDto(request, performer);
        return performerMapper.toResponseDTO(performer);
    }

    @Transactional
    public void softDeletePerformer(UUID performerId) {
        Performer performer = getPerformerEntityById(performerId);
        performer.setDeleted(true);
    }

    @Transactional
    public void hardDeletePerformer(UUID performerId) {
        validatePerformerExists(performerId);
        performerRepository.deleteById(performerId);
    }

    @Transactional
    public PerformerResponseDTO restorePerformer(UUID performerId) {
        Performer performer = performerRepository.findById(performerId)
                .orElseThrow(() -> new ResourceNotFoundException("Performer not found with ID: " + performerId));

        if (!performer.isDeleted()) {
            throw new BusinessLogicException("Performer is not deleted, nothing to restore!");
        }

        performer.setDeleted(false);
        return performerMapper.toResponseDTO(performer);
    }

    // Helper validation methods
    private void validatePerformerExists(UUID performerId) {
        if (!performerRepository.existsById(performerId)) {
            throw new ResourceNotFoundException("Performer not found");
        }
    }
}