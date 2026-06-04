package edu.swarmintelligence.mayfly;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConvergenceAnalyzer implements MayflyAnalyzer {

    private final double epsilon;
    private final double delta;
    private final int minPlateauLength; // This represents 'k' from the specification
    private final double lastPercentForFit; // This represents 'N' from the specification

    private int currentIteration = 0;
    private int hittingIteration = -1;
    private boolean thresholdReached = false;

    // Time-series collection
    private final List<Double> gbestHistory = new ArrayList<>();
    private final List<String> convergenceCurve = new ArrayList<>();
    private final Map<Integer, Double> populationDiversity = new LinkedHashMap<>();
    private final List<String> plateauSegments = new ArrayList<>();


    public ConvergenceAnalyzer(double epsilon, double delta, int minPlateauLength, double lastPercentForFit) {
        this.epsilon = epsilon;
        this.delta = delta;
        this.minPlateauLength = minPlateauLength;
        this.lastPercentForFit = lastPercentForFit;
    }

    @Override
    public String name() {
        return "ConvergenceAnalyzer";
    }

    @Override
    public void onEvent(MayflyEvent e) {
        if (e instanceof IterationStarted startEvent) {
            currentIteration = startEvent.iteration();
        }
        else if (e instanceof IterationCompleted completedEvent) {
            double currentGbest = completedEvent.gbestFitness();
            gbestHistory.add(currentGbest);

            // Metric 1: convergenceCurve in "iteration,fitness" format
            convergenceCurve.add(String.format("%d,%.10f", currentIteration, currentGbest));

            // Track threshold condition for iterationsToThreshold
            if (!thresholdReached && currentGbest <= epsilon) {
                hittingIteration = currentIteration;
                thresholdReached = true;
            }

            // Metric 2: populationDiversity [t] of the 2 * populationSize survivors
            List<Mayfly> survivors = completedEvent.survivors();
            double pairwiseDistanceSum = 0.0;
            long pairCount = 0;

            for (int i = 0; i < survivors.size(); i++) {
                for (int j = i + 1; j < survivors.size(); j++) {
                    pairwiseDistanceSum += calculateEuclideanDistance(survivors.get(i).pos, survivors.get(j).pos);
                    pairCount++;
                }
            }
            populationDiversity.put(currentIteration, pairCount > 0 ? pairwiseDistanceSum / pairCount : 0.0);
        }
        else if (e instanceof RunCompleted) {
            // Post-process complex sequential metrics upon execution completion
            processPlateauSegments();
        }
    }

    @Override
    public AnalyzerResult result() {
        // Metric 3: iterationsToThreshold(epsilon) with NormalizedVelocity (Delta t / total iterations)
        Map<String, Double> thresholdMetrics = new LinkedHashMap<>();
        thresholdMetrics.put("Iterations", (double) hittingIteration);

        int totalIterations = gbestHistory.size();
        double velocity = thresholdReached ? (double) hittingIteration / totalIterations : -1.0;
        thresholdMetrics.put("NormalizedVelocity", velocity);

        // Metric 5: convergenceRateEstimate (Linear Fit on log(gbest))
        double fitSlope = calculateLinearLogFit();

        return new ConvergenceResult(
                List.copyOf(convergenceCurve),
                Map.copyOf(populationDiversity),
                thresholdMetrics,
                List.copyOf(plateauSegments),
                fitSlope
        );
    }


    private void processPlateauSegments() {
        if (gbestHistory.size() < 2) return;

        int n = gbestHistory.size();
        int currentStreak = 0;
        int startIter = -1;

        for (int i = 1; i < n; i++) {
            double prevGbest = gbestHistory.get(i - 1);
            double currGbest = gbestHistory.get(i);

            // Delta calculation from specification: Delta gbest < delta
            if (Math.abs(prevGbest - currGbest) < delta) {
                if (currentStreak == 0) {
                    startIter = i; // Map to 1-based iteration index of the start step
                }
                currentStreak++;
            } else {
                // Check if the accumulated streak satisfies the minimum length constraint 'k'
                if (currentStreak >= minPlateauLength) {
                    plateauSegments.add(String.format("%d,%d", startIter, i));
                }
                currentStreak = 0;
            }
        }

        // Commit trailing segment if active when the run ended
        if (currentStreak >= minPlateauLength) {
            plateauSegments.add(String.format("%d,%d", startIter, n));
        }
    }

    private double calculateLinearLogFit() {
        int totalPoints = gbestHistory.size();
        int fitPointsCount = (int) Math.ceil(totalPoints * (lastPercentForFit / 100.0));
        if (fitPointsCount < 2) return 0.0;

        int startIndex = totalPoints - fitPointsCount;
        double sumX = 0.0, sumY = 0.0, sumXX = 0.0, sumXY = 0.0;
        int validCount = 0;

        for (int i = startIndex; i < totalPoints; i++) {
            double x = i + 1; // 1-based iteration index t
            double fitness = gbestHistory.get(i);

            // Numerical safety check for log computation
            if (fitness <= 0) continue;

            double y = Math.log(fitness);

            sumX += x;
            sumY += y;
            sumXX += x * x;
            sumXY += x * y;
            validCount++;
        }

        if (validCount < 2) return 0.0;

        double xMean = sumX / validCount;
        double yMean = sumY / validCount;

        double numerator = sumXY - validCount * xMean * yMean;
        double denominator = sumXX - validCount * xMean * xMean;

        return denominator != 0.0 ? numerator / denominator : 0.0;
    }

    private double calculateEuclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}