package edu.swarmintelligence.mayfly;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class JsonExporter implements AnalyticsExporter {

    @Override
    public void export(AnalyticsReport report, Writer out) throws IOException {
        if (report == null || out == null) return;

        out.write("{\n");
        out.write("  \"generatedAt\": \"" + report.generatedAt() + "\",\n");
        out.write("  \"seed\": " + report.seed() + ",\n");

        // Konfiguration ausgeben
        out.write("  \"config\": {\n");
        out.write("    \"dimensions\": " + report.config().dimensions() + ",\n");
        out.write("    \"populationSize\": " + report.config().populationSize() + ",\n");
        out.write("    \"maxIterations\": " + report.config().maxIterations() + "\n");
        out.write("  },\n");

        // Ergebnisse der Analyzer mappen
        out.write("  \"byAnalyzer\": {\n");
        Map<String, AnalyzerResult> results = report.byAnalyzer();
        int count = 0;
        int total = results.size();

        for (Map.Entry<String, AnalyzerResult> entry : results.entrySet()) {
            out.write("    \"" + escapeJson(entry.getKey()) + "\": {\n");
            serializeResult(entry.getValue(), out);
            out.write("    }");

            if (count < total - 1) {
                out.write(",");
            }
            out.write("\n");
            count++;
        }

        out.write("  }\n");
        out.write("}\n");
        out.flush();
    }

    private void serializeResult(AnalyzerResult result, Writer out) throws IOException {
        if (result instanceof InteractionResult ir) {
            out.write("      \"nuptialDanceCount\": " + ir.nuptialDanceCount() + ",\n");
            out.write("      \"attractionCount\": " + ir.attractionCount() + ",\n");
            out.write("      \"femaleAttractionRate\": "); writeMap(ir.femaleAttractionRate(), out, "      "); out.write(",\n");
            out.write("      \"meanPairDistance\": "); writeMap(ir.meanPairDistance(), out, "      "); out.write(",\n");
            out.write("      \"pairFitnessGap\": "); writeMap(ir.pairFitnessGap(), out, "      "); out.write(",\n");
            out.write("      \"interactionHistogram\": "); writeMap(ir.interactionHistogram(), out, "      "); out.write("\n");
        }
        else if (result instanceof ConvergenceResult cr) {
            out.write("      \"convergenceCurve\": "); writeList(cr.convergenceCurve(), out); out.write(",\n");
            out.write("      \"populationDiversity\": "); writeMap(cr.populationDiversity(), out, "      "); out.write(",\n");
            out.write("      \"iterationsToThreshold\": "); writeMap(cr.iterationsToThreshold(), out, "      "); out.write(",\n");
            out.write("      \"plateauSegments\": "); writeList(cr.plateauSegments(), out); out.write(",\n");
            out.write("      \"convergenceRateEstimate\": " + (Double.isNaN(cr.convergenceRateEstimate()) ? "null" : cr.convergenceRateEstimate()) + "\n");
        }
        else if (result instanceof GlobalMemoryResult gmr) {
            out.write("      \"gbestUpdateCount\": " + gmr.gbestUpdateCount() + ",\n");
            out.write("      \"firstHittingIteration\": " + gmr.firstHittingIteration() + ",\n");
            out.write("      \"gbestUpdateSourceDistribution\": "); writeMap(gmr.gbestUpdateSourceDistribution(), out, "      "); out.write(",\n");
            out.write("      \"improvementDelta\": "); writeMap(gmr.improvementDelta(), out, "      "); out.write(",\n");
            out.write("      \"stagnationStreaks\": "); writeList(gmr.stagnationStreaks(), out); out.write(",\n");

            out.write("      \"gbestTrajectory\": [\n");
            List<GbestTrajectoryPoint> traj = gmr.gbestTrajectory();
            for (int i = 0; i < traj.size(); i++) {
                GbestTrajectoryPoint p = traj.get(i);
                out.write("        {\"iteration\": " + p.iteration() + ", \"gbestFitness\": " + p.gbestFitness() + "}");
                if (i < traj.size() - 1) out.write(",");
                out.write("\n");
            }
            out.write("      ]\n");
        }
        else if (result instanceof LocalMemoryResult lmr) {
            out.write("      \"meanPbestImprovement\": " + lmr.meanPbestImprovement() + ",\n");
            out.write("      \"pbestPositionDiversity\": "); writeMap(lmr.pbestPositionDiversity(), out, "      "); out.write(",\n");

            out.write("      \"pbestFitnessDistribution\": {\n");
            int cnt = 0;
            for (Map.Entry<Integer, List<Double>> entry : lmr.pbestFitnessDistribution().entrySet()) {
                out.write("        \"" + entry.getKey() + "\": ");
                writeList(entry.getValue(), out);
                if (cnt < lmr.pbestFitnessDistribution().size() - 1) out.write(",");
                out.write("\n");
                cnt++;
            }
            out.write("      }\n");
        }
    }

    private <K, V> void writeMap(Map<K, V> map, Writer out, String indent) throws IOException {
        out.write("{\n");
        int idx = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            String keyStr = entry.getKey() instanceof Enum ? ((Enum<?>) entry.getKey()).name() : entry.getKey().toString();
            String valStr = entry.getValue() == null ? "null" : entry.getValue().toString();
            if (entry.getValue() instanceof Double && Double.isNaN((Double) entry.getValue())) {
                valStr = "null";
            }
            out.write(indent + "  \"" + escapeJson(keyStr) + "\": " + valStr);
            if (idx < map.size() - 1) out.write(",");
            out.write("\n");
            idx++;
        }
        out.write(indent + "}");
    }

    private <T> void writeList(List<T> list, Writer out) throws IOException {
        out.write("[");
        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            if (item instanceof String) {
                out.write("\"" + escapeJson((String) item) + "\"");
            } else {
                out.write(item.toString());
            }
            if (i < list.size() - 1) out.write(", ");
        }
        out.write("]");
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}