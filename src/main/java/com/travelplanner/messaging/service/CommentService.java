package com.travelplanner.messaging.service;

import com.travelplanner.messaging.domain.Comment;
import com.travelplanner.messaging.dto.CommentDto;
import com.travelplanner.messaging.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    @Transactional
    public CommentDto addComment(String tripId, String authorEmail, String content) {
        Comment c = Comment.builder()
                .id(UUID.randomUUID().toString())
                .tripId(tripId)
                .authorEmail(authorEmail)
                .content(content)
                .createdAt(Instant.now())
                .build();
        Comment saved = commentRepository.save(c);
        return new CommentDto(saved.getId(), saved.getTripId(), saved.getAuthorEmail(), saved.getContent(), saved.getCreatedAt());
    }

    public List<CommentDto> listComments(String tripId) {
        return commentRepository.findAllByTripIdOrderByCreatedAtAsc(tripId).stream()
                .map(c -> new CommentDto(c.getId(), c.getTripId(), c.getAuthorEmail(), c.getContent(), c.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
