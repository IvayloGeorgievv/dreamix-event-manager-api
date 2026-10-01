package org.example.eventmanagementapi.performer;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.performer.dto.PerformerRequestDTO;
import org.example.eventmanagementapi.performer.dto.PerformerResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public Page<PerformerResponseDTO> getPerformers(
            final String search,
            final boolean includeDeleted,
            final Pageable pageable
    ) {
        final boolean effectiveIncludeDeleted = includeDeleted && isCurrentUserAdmin();
        final String sanitizedSearch = search != null ? search.trim() : "";

        return performerRepository.findPerformers(sanitizedSearch, effectiveIncludeDeleted, pageable)
                .map(performerMapper::toResponseDTO);
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

    private boolean isCurrentUserAdmin() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}