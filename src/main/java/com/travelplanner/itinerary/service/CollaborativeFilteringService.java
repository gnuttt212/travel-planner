package com.travelplanner.itinerary.service;

import com.travelplanner.interaction.repository.UserInteractionRepository;
import com.travelplanner.itinerary.domain.Destination;
import com.travelplanner.itinerary.dto.DestinationResponse;
import com.travelplanner.itinerary.mapper.DestinationMapper;
import com.travelplanner.itinerary.repository.DestinationRepository;
import com.travelplanner.user.domain.UserPreference;
import com.travelplanner.user.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaborativeFilteringService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserInteractionRepository interactionRepository;
    private final DestinationRepository destinationRepository;
    private final DestinationMapper destinationMapper;

    public List<DestinationResponse> getCollaborativeRecommendations(String userId) {
        Optional<UserPreference> prefOpt = userPreferenceRepository.findByUserId(userId);
        if (prefOpt.isEmpty() || prefOpt.get().getPreferenceVector() == null) {
            return List.of(); // Need preferences to find similar users
        }

        String userVector = prefOpt.get().getPreferenceVector();
        
        // 1. Find Top 10 similar users using Cosine Similarity (<=>)
        List<String> similarUsers = userPreferenceRepository.findSimilarUsers(userId, userVector, 10);
        if (similarUsers.isEmpty()) {
            return List.of();
        }

        // 2. Find destinations those users interacted with the most
        List<String> topDestIds = interactionRepository.findTopDestinationsBySimilarUsers(similarUsers, 5);

        // 3. Fetch Destination objects
        return destinationRepository.findAllById(topDestIds).stream()
                .map(destinationMapper::toResponse)
                .collect(Collectors.toList());
    }
}
