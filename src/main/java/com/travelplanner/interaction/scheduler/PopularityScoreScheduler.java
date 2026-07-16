package com.travelplanner.interaction.scheduler;

import com.travelplanner.interaction.repository.UserInteractionRepository;
import com.travelplanner.itinerary.domain.Destination;
import com.travelplanner.itinerary.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopularityScoreScheduler {

    private final UserInteractionRepository interactionRepository;
    private final DestinationRepository destinationRepository;

    // Run every 5 minutes for demo purposes (in prod, maybe daily @ 2AM: "0 0 2 * * ?")
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void updatePopularityScores() {
        log.info("Starting scheduled job: updatePopularityScores");

        List<Object[]> aggregated = interactionRepository.getAggregatedWeightsPerDestination();
        Map<String, Double> scoreMap = aggregated.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> ((Number) arr[1]).doubleValue()
                ));

        List<Destination> destinations = destinationRepository.findAll();
        for (Destination dest : destinations) {
            Double newScore = scoreMap.getOrDefault(dest.getId(), 0.0);
            dest.setPopularityScore(newScore);
        }

        destinationRepository.saveAll(destinations);
        log.info("Finished updating popularity scores for {} destinations", destinations.size());
    }
}
