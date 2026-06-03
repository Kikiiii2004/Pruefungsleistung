package edu.swarmintelligence.mayfly;

import java.util.List;
import java.util.Map;

public record GlobalMemoryResult(
        List<GbestTrajectoryPoint> gbestTrajectory,
        long gbestUpdateCount,
        Map<UpdateSource, Double> gbestUpdateSourceDistribution,
        Map<Integer, Double> improvementDelta,
        List<Integer> stagnationStreaks,
        int firstHittingIteration
) implements AnalyzerResult {
}