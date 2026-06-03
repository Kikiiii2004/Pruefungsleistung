package edu.swarmintelligence.mayfly;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgentInteractionAnalyzer implements MayflyAnalyzer {

    private int currentIteration = 0;
    private long nuptialDanceCount = 0;
    private long maleAttractionCount = 0;

    private final Map<Integer, Long> femaleAttractionsPerIter = new HashMap<>();
    private final Map<Integer, Long> femaleTotalUpdatesPerIter = new HashMap<>();

    private final Map<Integer, Double> meanPairDistance = new LinkedHashMap<>();
    private final Map<Integer, Double> pairFitnessGap = new LinkedHashMap<>();

    private long totalFemaleAttraction = 0;
    private long totalFemaleRandomWalk = 0;

    @Override
    public String name() {
        return "AgentInteractionAnalyzer";
    }

    @Override
    public void onEvent(MayflyEvent e) {
        if (e instanceof IterationStarted startEvent) {
            currentIteration = startEvent.iteration();
            femaleAttractionsPerIter.put(currentIteration, 0L);
            femaleTotalUpdatesPerIter.put(currentIteration, 0L);
        }
        else if (e instanceof MaleUpdated maleEvent) {
            if (maleEvent.isNuptialDance()) {
                nuptialDanceCount++;
            } else {
                maleAttractionCount++;
            }
        }
        else if (e instanceof FemaleUpdated femaleEvent) {
            femaleTotalUpdatesPerIter.merge(currentIteration, 1L, Long::sum);
            if (femaleEvent.isAttracted()) {
                femaleAttractionsPerIter.merge(currentIteration, 1L, Long::sum);
                totalFemaleAttraction++;
            } else {
                totalFemaleRandomWalk++;
            }
        }
        else if (e instanceof IterationCompleted completedEvent) {
            int iter = completedEvent.iteration();
            List<Mayfly> males = completedEvent.matingMales();
            List<Mayfly> females = completedEvent.matingFemales();
            int popSize = males.size();

            double totalDistance = 0.0;
            double totalFitnessGap = 0.0;

            for (int i = 0; i < popSize; i++) {
                Mayfly male = males.get(i);
                Mayfly female = females.get(i);
                totalDistance += Math.sqrt(euclideanDistanceSq(male.pos, female.pos));
                totalFitnessGap += Math.abs(male.fitness - female.fitness);
            }

            meanPairDistance.put(iter, totalDistance / popSize);
            pairFitnessGap.put(iter, totalFitnessGap / popSize);
        }
    }

    @Override
    public AnalyzerResult result() {
        Map<Integer, Double> femaleAttractionRate = new LinkedHashMap<>();
        femaleTotalUpdatesPerIter.forEach((iter, total) -> {
            long attracted = femaleAttractionsPerIter.getOrDefault(iter, 0L);
            femaleAttractionRate.put(iter, total > 0 ? (double) attracted / total : 0.0);
        });

        Map<String, Long> interactionHistogram = new LinkedHashMap<>();
        interactionHistogram.put("Nuptial Dance", nuptialDanceCount);
        interactionHistogram.put("Male Attraction", maleAttractionCount);
        interactionHistogram.put("Female Attraction", totalFemaleAttraction);
        interactionHistogram.put("Female Random Walk", totalFemaleRandomWalk);

        return new InteractionResult(
                nuptialDanceCount,
                maleAttractionCount,
                femaleAttractionRate,
                meanPairDistance,
                pairFitnessGap,
                interactionHistogram
        );
    }

    private double euclideanDistanceSq(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }
}