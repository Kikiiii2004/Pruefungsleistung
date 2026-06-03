package edu.swarmintelligence.mayfly;

import java.util.List;
import java.util.Map;


public record LocalMemoryResult(
        Map<Mayfly, Long> pbestUpdateCountPerAgent,
        double meanPbestImprovement,
        Map<Integer, Double> pbestPositionDiversity,
        Map<Integer, List<Double>> pbestFitnessDistribution
) implements AnalyzerResult {
}