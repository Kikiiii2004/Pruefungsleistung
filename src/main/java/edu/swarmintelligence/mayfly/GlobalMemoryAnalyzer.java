package edu.swarmintelligence.mayfly;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class GlobalMemoryAnalyzer implements MayflyAnalyzer {

    private final double epsilon;

    private int currentIteration = 0;
    private long totalGbestUpdates = 0;
    private int firstHittingIter = -1;
    private boolean epsilonReached = false;

    // Tracking structures
    private final List<String> gbestTrajectory = new ArrayList<>();
    private final Map<UpdateSource, Long> sourceCounts = new EnumMap<>(UpdateSource.class);
    private final Map<Integer, Double> improvementDelta = new LinkedHashMap<>();

    // Variables for stagnation analysis
    private double lastGbestAtIterEnd = Double.POSITIVE_INFINITY;
    private int currentStagnationLength = 0;
    private final List<Integer> stagnationStreaks = new ArrayList<>();


    public GlobalMemoryAnalyzer(double epsilon) {
        this.epsilon = epsilon;
        for (UpdateSource source : UpdateSource.values()) {
            sourceCounts.put(source, 0L);
        }
    }

    @Override
    public String name() {
        return "GlobalMemoryAnalyzer";
    }

    @Override
    public void onEvent(MayflyEvent e) {
        if (e instanceof IterationStarted startEvent) {
            currentIteration = startEvent.iteration();
            // Ensure every iteration has a default delta of 0.0
            improvementDelta.put(currentIteration, 0.0);
        }
        else if (e instanceof GbestUpdated gbestEvent) {
            totalGbestUpdates++;
            UpdateSource source = gbestEvent.source();
            if (source != null) {
                sourceCounts.put(source, sourceCounts.get(source) + 1);
            }

            // Accumulate improvement delta within the current iteration
            double delta = gbestEvent.previousGbestFitness() - gbestEvent.newGbestFitness();
            improvementDelta.put(currentIteration, improvementDelta.get(currentIteration) + delta);

            // Check first hitting condition (gbest <= epsilon)
            if (!epsilonReached && gbestEvent.newGbestFitness() <= epsilon) {
                firstHittingIter = currentIteration;
                epsilonReached = true;
            }
        }
        else if (e instanceof IterationCompleted completedEvent) {
            double currentGbest = completedEvent.gbestFitness();

            // One trajectory entry per iteration with the gbest value at iteration end
            gbestTrajectory.add(String.format("%d,%.10f", currentIteration, currentGbest));

            // Stagnation evaluation: strictly no improvement (delta == 0.0)
            if (currentGbest == lastGbestAtIterEnd) {
                currentStagnationLength++;
            } else {
                if (currentStagnationLength > 0) {
                    stagnationStreaks.add(currentStagnationLength);
                    currentStagnationLength = 0;
                }
            }
            lastGbestAtIterEnd = currentGbest;
        }
        else if (e instanceof RunCompleted) {
            // Commit any trailing stagnation streak when the algorithm run terminates
            if (currentStagnationLength > 0) {
                stagnationStreaks.add(currentStagnationLength);
                currentStagnationLength = 0;
            }
        }
    }

    @Override
    public AnalyzerResult result() {
        Map<UpdateSource, Double> sourceDistribution = new EnumMap<>(UpdateSource.class);
        for (UpdateSource source : UpdateSource.values()) {
            long count = sourceCounts.get(source);
            double percentage = totalGbestUpdates > 0 ? (double) count / totalGbestUpdates : 0.0;
            sourceDistribution.put(source, percentage);
        }

        return new GlobalMemoryResult(
                List.copyOf(gbestTrajectory),
                totalGbestUpdates,
                sourceDistribution,
                Map.copyOf(improvementDelta),
                List.copyOf(stagnationStreaks),
                firstHittingIter
        );
    }
}