package com.travelplanner.messaging.service;

import com.travelplanner.messaging.domain.Reaction;
import com.travelplanner.messaging.dto.ReactionDto;
import com.travelplanner.messaging.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionRepository reactionRepository;

    @Transactional
    public ReactionDto react(String author, String targetType, String targetId, String type) {
        // if exists, update type
        Reaction r = reactionRepository.findByAuthorEmailAndTargetTypeAndTargetId(author, targetType, targetId)
                .map(existing -> {
                    existing.setType(type);
                    existing.setCreatedAt(Instant.now());
                    return reactionRepository.save(existing);
                })
                .orElseGet(() -> reactionRepository.save(Reaction.builder()
                        .id(UUID.randomUUID().toString())
                        .authorEmail(author)
                        .targetType(targetType)
                        .targetId(targetId)
                        .type(type)
                        .createdAt(Instant.now())
                        .build()));

        return new ReactionDto(r.getId(), r.getAuthorEmail(), r.getTargetType(), r.getTargetId(), r.getType(), r.getCreatedAt());
    }

    @Transactional
    public void removeReaction(String author, String targetType, String targetId) {
        reactionRepository.findByAuthorEmailAndTargetTypeAndTargetId(author, targetType, targetId)
                .ifPresent(reactionRepository::delete);
    }

    public List<ReactionDto> listForTarget(String targetType, String targetId) {
        return reactionRepository.findAllByTargetTypeAndTargetId(targetType, targetId).stream()
                .map(r -> new ReactionDto(r.getId(), r.getAuthorEmail(), r.getTargetType(), r.getTargetId(), r.getType(), r.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
