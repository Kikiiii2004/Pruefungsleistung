package edu.swarmintelligence.mayfly;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class CsvExporter implements AnalyticsExporter {

    @Override
    public void export(AnalyticsReport report, Writer out) throws IOException {
        if (report == null || out == null) return;

        // Spaltenüberschriften schreiben
        out.write("MetricType;Iteration;KeyIdentifier;Value\n");

        // Metadaten
        out.write("META;0;Seed;" + report.seed() + "\n");
        out.write("META;0;GeneratedAt;" + report.generatedAt().toString() + "\n");

        for (Map.Entry<String, AnalyzerResult> analyzerEntry : report.byAnalyzer().entrySet()) {
            String analyzerName = analyzerEntry.getKey();
            AnalyzerResult res = analyzerEntry.getValue();

            if (res instanceof InteractionResult ir) {
                out.write(analyzerName + ";0;TotalNuptialDance;" + ir.nuptialDanceCount() + "\n");
                out.write(analyzerName + ";0;TotalAttraction;" + ir.attractionCount() + "\n");

                for (var e : ir.femaleAttractionRate().entrySet()) {
                    out.write(analyzerName + ";" + e.getKey() + ";FemaleAttractionRate;" + e.getValue() + "\n");
                }
                for (var e : ir.meanPairDistance().entrySet()) {
                    out.write(analyzerName + ";" + e.getKey() + ";MeanPairDistance;" + e.getValue() + "\n");
                }
                for (var e : ir.pairFitnessGap().entrySet()) {
                    out.write(analyzerName + ";" + e.getKey() + ";PairFitnessGap;" + e.getValue() + "\n");
                }
            }
            else if (res instanceof ConvergenceResult cr) {
                out.write(analyzerName + ";0;ConvergenceRateEstimate;" + cr.convergenceRateEstimate() + "\n");

                for (var e : cr.populationDiversity().entrySet()) {
                    out.write(analyzerName + ";" + e.getKey() + ";PopulationDiversity;" + e.getValue() + "\n");
                }
                for (var e : cr.iterationsToThreshold().entrySet()) {
                    out.write(analyzerName + ";0;Threshold_" + e.getKey() + ";" + e.getValue() + "\n");
                }
            }
            else if (res instanceof GlobalMemoryResult gmr) {
                out.write(analyzerName + ";0;GbestUpdateCount;" + gmr.gbestUpdateCount() + "\n");
                out.write(analyzerName + ";0;FirstHittingIteration;" + gmr.firstHittingIteration() + "\n");

                for (GbestTrajectoryPoint p : gmr.gbestTrajectory()) {
                    out.write(analyzerName + ";" + p.iteration() + ";GbestFitness;" + p.gbestFitness() + "\n");
                }
                for (var e : gmr.improvementDelta().entrySet()) {
                    out.write(analyzerName + ";" + e.getKey() + ";ImprovementDelta;" + e.getValue() + "\n");
                }
            }
            else if (res instanceof LocalMemoryResult lmr) {
                out.write(analyzerName + ";0;MeanPbestImprovement;" + lmr.meanPbestImprovement() + "\n");

                for (var e : lmr.pbestPositionDiversity().entrySet()) {
                    out.write(analyzerName + ";" + e.getKey() + ";PbestPositionDiversity;" + e.getValue() + "\n");
                }
            }
        }
        out.flush();
    }
}