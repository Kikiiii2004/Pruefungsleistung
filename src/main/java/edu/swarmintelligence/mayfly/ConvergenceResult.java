package edu.swarmintelligence.mayfly;

import java.util.List;
import java.util.Map;

public record ConvergenceResult(
        List<String> convergenceCurve,
        Map<Integer, Double> populationDiversity,
        Map<String, Double> iterationsToThreshold,
        List<String> plateauSegments,
        double convergenceRateEstimate
) implements AnalyzerResult {
}