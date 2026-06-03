package edu.swarmintelligence.mayfly;

import java.util.Map;

public record InteractionResult(
        long nuptialDanceCount,
        long attractionCount,
        Map<Integer, Double> femaleAttractionRate,
        Map<Integer, Double> meanPairDistance,
        Map<Integer, Double> pairFitnessGap,
        Map<String, Long> interactionHistogram
) implements AnalyzerResult {
}