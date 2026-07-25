package com.travelplanner.recommendation.domain;

public record CompositeScore(
    double total,
    double ratingScore,
    double distanceScore,
    double hoursScore,
    double preferenceScore,
    double budgetScore
) implements Comparable<CompositeScore> {
    @Override
    public int compareTo(CompositeScore other) {
        return Double.compare(other.total, this.total); // descending
    }
}
