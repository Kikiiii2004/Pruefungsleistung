package edu.swarmintelligence.mayfly;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int numberOfRuns = 10;
        MayflyConfig cfg = MayflyConfig.ackley10D();
        MayflyAlgorithm algo = new MayflyAlgorithm();

        List<Double> multiRunFitnessValues = new ArrayList<>();
        AnalyticsReport finalReport = null;
        AnalyticsEngine engine = null;

        System.out.println("Executing " + numberOfRuns + " multi-run optimization benchmarks...");

        for (int i = 1; i <= numberOfRuns; i++) {
            long currentSeed = i * 100L; // Distinct deterministic seeds

            // Re-create engine/analyzers per run to fresh capture telemetry
            engine = new AnalyticsEngine();
            double epsilon = 1e-8;
            engine.registerAnalyzer(new AgentInteractionAnalyzer());
            engine.registerAnalyzer(new GlobalMemoryAnalyzer(epsilon));
            engine.registerAnalyzer(new ConvergenceAnalyzer(epsilon, 10.0, 5, 20.0));

            algo = new MayflyAlgorithm();
            algo.addListener(engine);

            MayflyResult runResult = algo.run(cfg, currentSeed);
            multiRunFitnessValues.add(runResult.gbestFitness());

            // Save the last run's structural telemetry report as our documentation baseline
            if (i == numberOfRuns) {
                finalReport = engine.generateReport(cfg, currentSeed);
            }
        }

        // Compute statistical aggregates
        MultiRunStatistics stats = new MultiRunStatistics(multiRunFitnessValues);

        // Generate combined Markdown report
        try (FileWriter fw = new FileWriter("Mayfly_Analytics_Report.md")) {
            MarkdownReportGenerator mdGenerator = new MarkdownReportGenerator();
            mdGenerator.generateWithStatistics(finalReport, stats, numberOfRuns, fw);
            System.out.println("Markdown Report successfully stored under 'Mayfly_Analytics_Report.md' including Multi-Run Statistics!");
        } catch (IOException ex) {
            System.err.println("Error writing Markdown Report: " + ex.getMessage());
        }
    }
}