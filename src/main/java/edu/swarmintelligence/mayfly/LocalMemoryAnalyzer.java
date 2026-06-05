package edu.swarmintelligence.mayfly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class LocalMemoryAnalyzer implements MayflyAnalyzer {

    private int currentIteration = 0;

    // Agent-specific tracking
    private final Map<Mayfly, Long> pbestUpdateCountPerAgent = new HashMap<>();

    // Accumulators for mean improvement calculation
    private double totalRelativeImprovement = 0.0;
    private long validImprovementUpdatesCount = 0;

    // Iteration-based time series datasets
    private final Map<Integer, Double> pbestPositionDiversity = new LinkedHashMap<>();
    private final Map<Integer, List<Double>> pbestFitnessDistribution = new LinkedHashMap<>();

    @Override
    public String name() {
        return "LocalMemoryAnalyzer";
    }

    @Override
    public void onEvent(MayflyEvent e) {
        if (e instanceof IterationStarted startEvent) {
            currentIteration = startEvent.iteration();
        }
        else if (e instanceof PbestUpdated pbestEvent) {
            Mayfly agent = pbestEvent.agent();

            // 1. Increment update counter for the specific agent
            pbestUpdateCountPerAgent.put(agent, pbestUpdateCountPerAgent.getOrDefault(agent, 0L) + 1);

            // 2. Compute relative improvement (exclude initial assignments from infinity)
            double oldPbest = pbestEvent.previousPbestFitness();
            double newPbest = pbestEvent.newPbestFitness();

            if (Double.isFinite(oldPbest) && oldPbest != 0.0) {
                double relativeImprovement = (oldPbest - newPbest) / Math.abs(oldPbest);
                totalRelativeImprovement += relativeImprovement;
                validImprovementUpdatesCount++;
            }
        }
        else if (e instanceof IterationCompleted completedEvent) {
            int iter = completedEvent.iteration();
            List<Mayfly> survivors = completedEvent.survivors();
            if (survivors.isEmpty()) return;

            int numAgents = survivors.size();
            int dimensions = survivors.get(0).pbestPos.length;

            // ----- Metric 3: Calculate pbestPositionDiversity -----
            double sumOfDimensionStdDevs = 0.0;

            for (int d = 0; d < dimensions; d++) {
                // Collect coordinate component for dimension d across all agents
                double[] components = new double[numAgents];
                double sum = 0.0;
                for (int i = 0; i < numAgents; i++) {
                    components[i] = survivors.get(i).pbestPos[d];
                    sum += components[i];
                }
                double mean = sum / numAgents;

                // Calculate standard deviation for this dimension
                double varianceSum = 0.0;
                for (double val : components) {
                    varianceSum += (val - mean) * (val - mean);
                }
                double stdDev = Math.sqrt(varianceSum / numAgents);
                sumOfDimensionStdDevs += stdDev;
            }
            // Arithmetic mean over all D dimensional standard deviations
            pbestPositionDiversity.put(iter, sumOfDimensionStdDevs / dimensions);

            // ----- Metric 4: Calculate pbestFitnessDistribution -----
            List<Double> fitnessValues = new ArrayList<>(numAgents);
            for (Mayfly agent : survivors) {
                fitnessValues.add(agent.pbestFitness);
            }
            Collections.sort(fitnessValues);

            double min = fitnessValues.get(0);
            double q25 = calculateQuantile(fitnessValues, 0.25);
            double median = calculateQuantile(fitnessValues, 0.50);
            double q75 = calculateQuantile(fitnessValues, 0.75);
            double max = fitnessValues.get(numAgents - 1);

            pbestFitnessDistribution.put(iter, Arrays.asList(min, q25, median, q75, max));
        }
    }

    @Override
    public AnalyzerResult result() {
        double meanImprovement = validImprovementUpdatesCount > 0
                ? totalRelativeImprovement / validImprovementUpdatesCount
                : 0.0;

        return new LocalMemoryResult(
                Map.copyOf(pbestUpdateCountPerAgent),
                meanImprovement,
                Map.copyOf(pbestPositionDiversity),
                Map.copyOf(pbestFitnessDistribution)
        );
    }

    /**
     * Helper method to compute precise quantile boundaries using linear interpolation.
     */
    private double calculateQuantile(List<Double> sortedValues, double quantile) {
        int n = sortedValues.size();
        double pos = quantile * (n - 1);
        int index = (int) Math.floor(pos);
        double fraction = pos - index;

        if (index + 1 < n) {
            return sortedValues.get(index) + fraction * (sortedValues.get(index + 1) - sortedValues.get(index));
        } else {
            return sortedValues.get(index);
        }
    }
}