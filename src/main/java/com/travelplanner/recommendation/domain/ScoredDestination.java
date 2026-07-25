package com.travelplanner.recommendation.domain;

public record ScoredDestination(
    Destination destination,
    CompositeScore score
) implements Comparable<ScoredDestination> {
    @Override
    public int compareTo(ScoredDestination other) {
        return score.compareTo(other.score);
    }
}
