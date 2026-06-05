package edu.swarmintelligence.mayfly;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class MarkdownReportGenerator {

    // Unicode block characters from U+2581 to U+2588 for sparkline rendering
    private static final char[] SPARKLINE_BLOCKS = {' ', '▂', '▃', '▄', '▅', '▆', '▇', '█'};

    public void generate(AnalyticsReport report, Writer out) throws IOException {
        if (report == null || out == null) return;

        // 1. HEADER SECTION
        out.write("# 📑 Mayfly Algorithm Optimization Report\n\n");
        out.write("## ⚙️ Meta-Information & Configuration\n\n");
        out.write("| Property | Value |\n");
        out.write("| :--- | :--- |\n");
        out.write("| **Execution Timestamp** | " + report.generatedAt().toString() + " |\n");
        out.write("| **Random Seed** | " + report.seed() + " |\n");
        out.write("| **Dimensions (D)** | " + report.config().dimensions() + " |\n");
        out.write("| **Population Size** | " + report.config().populationSize() + " |\n");
        out.write("| **Max Iterations** | " + report.config().maxIterations() + " |\n");
        out.write("| **Tooling Versions** | Java 25, IntelliJ 2024.1.1, GraalVM |\n\n");

        // 2. UNICODE SPARKLINE (Extract gbest curve from GlobalMemoryAnalyzer)
        out.write("## 📉 Visual gbest Convergence (Sparkline)\n\n");
        GlobalMemoryResult gmr = (GlobalMemoryResult) report.byAnalyzer().get("GlobalMemoryAnalyzer");
        if (gmr != null && !gmr.gbestTrajectory().isEmpty()) {
            out.write("Trajectory: `" + buildSparkline(gmr.gbestTrajectory()) + "`\n\n");
        } else {
            out.write("*No trajectory data available for sparkline rendering.*\n\n");
        }

        // 3. ANALYZER METRICS SECTIONS (Dynamic table generation per analyzer)
        out.write("## 📊 Analyzer Key Metrics\n\n");

        for (Map.Entry<String, AnalyzerResult> entry : report.byAnalyzer().entrySet()) {
            String name = entry.getKey();
            AnalyzerResult res = entry.getValue();

            out.write("### 🔍 " + name + "\n\n");

            if (res instanceof GlobalMemoryResult g) {
                out.write("| Metric | Value |\n");
                out.write("| :--- | :--- |\n");
                out.write("| Gbest Update Count | " + g.gbestUpdateCount() + " |\n");
                out.write("| First Hitting Iteration | " + (g.firstHittingIteration() != -1 ? g.firstHittingIteration() : "Not reached") + " |\n\n");
            }
            else if (res instanceof InteractionResult ir) {
                out.write("| Interaction Type | Count |\n");
                out.write("| :--- | :--- |\n");
                out.write("| Nuptial Dance (Male) | " + ir.nuptialDanceCount() + " |\n");
                out.write("| Attraction Count | " + ir.attractionCount() + " |\n\n");
            }
            else if (res instanceof ConvergenceResult cr) {
                out.write("| Metric | Value |\n");
                out.write("| :--- | :--- |\n");
                out.write("| Convergence Rate (Linear Log Fit) | " + String.format("%.6f", cr.convergenceRateEstimate()) + " |\n");
                out.write("| Detected Plateau Segments | " + cr.plateauSegments().size() + " |\n\n");
            }
            else if (res instanceof LocalMemoryResult lmr) {
                out.write("| Metric | Value |\n");
                out.write("| :--- | :--- |\n");
                out.write("| Mean pbest Improvement | " + String.format("%.6f", lmr.meanPbestImprovement()) + " |\n\n");
            }
        }

        // 4. MERMAID DIAGRAM GENERATION (Plateau Gantt chart or Convergence Line chart)
        out.write("## 🗺️ Convergence & Plateau Diagram\n\n");
        ConvergenceResult cr = (ConvergenceResult) report.byAnalyzer().get("ConvergenceAnalyzer");
        if (cr != null && !cr.plateauSegments().isEmpty()) {
            out.write("```mermaid\n");
            out.write("gantt\n");
            out.write("    title Detected Plateau Segments (Stagnation Phases)\n");
            out.write("    dateFormat X\n");
            out.write("    axisFormat %s\n\n");
            out.write("    section Phases\n");

            for (int i = 0; i < cr.plateauSegments().size(); i++) {
                String[] parts = cr.plateauSegments().get(i).split(",");
                if (parts.length == 2) {
                    out.write("    Plateau " + (i + 1) + " : " + parts[0] + ", " + parts[1] + "\n");
                }
            }
            out.write("```\n");
        } else if (gmr != null && !gmr.gbestTrajectory().isEmpty()) {
            // Fallback to a Mermaid Line Chart if no specific plateaus were committed
            out.write("```mermaid\n");
            out.write("xychart-beta\n");
            out.write("    title \"gbest Convergence Curve (Sampled)\"\n");

            List<GbestTrajectoryPoint> traj = gmr.gbestTrajectory();
            // Downsample to max 10 data points to keep the Mermaid chart clean and legible
            int step = Math.max(1, traj.size() / 10);

            StringBuilder xAxis = new StringBuilder("    x-axis [");
            StringBuilder yAxis = new StringBuilder("    line [");

            for (int i = 0; i < traj.size(); i += step) {
                GbestTrajectoryPoint p = traj.get(i);
                xAxis.append("\"Iter ").append(p.iteration()).append("\", ");
                yAxis.append(String.format("%.4f", p.gbestFitness())).append(", ");
            }

            // Trim trailing commas
            if (xAxis.length() > 12) {
                xAxis.setLength(xAxis.length() - 2);
                yAxis.setLength(yAxis.length() - 2);
            }
            xAxis.append("]\n");
            yAxis.append("]\n");

            out.write(xAxis.toString());
            out.write(yAxis.toString());
            out.write("```\n");
        }

        out.flush();
    }

    /**
     * Mathematically maps gbest fitness values onto the 8 Unicode block heights.
     * Better (lower) fitness values map to lower blocks, preserving intuitive visualization.
     */
    private String buildSparkline(List<GbestTrajectoryPoint> trajectory) {
        if (trajectory == null || trajectory.isEmpty()) return "";

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (GbestTrajectoryPoint p : trajectory) {
            if (p.gbestFitness() < min) min = p.gbestFitness();
            if (p.gbestFitness() > max) max = p.gbestFitness();
        }

        double range = max - min;
        StringBuilder sparkline = new StringBuilder();

        for (GbestTrajectoryPoint p : trajectory) {
            int index;
            if (range == 0) {
                index = 0; // Flat line fallback
            } else {
                // Normalize and calculate block index
                double normalized = (p.gbestFitness() - min) / range;
                index = (int) Math.round(normalized * (SPARKLINE_BLOCKS.length - 1));
                // Clamp within array bounds to prevent IndexOutOfBoundsException
                index = Math.clamp(index, 0, SPARKLINE_BLOCKS.length - 1);
            }
            sparkline.append(SPARKLINE_BLOCKS[index]);
        }
        return sparkline.toString();
    }

    /**
     * Overloaded generate method to optionally embed multi-run statistical aggregates.
     */
    public void generateWithStatistics(AnalyticsReport report, MultiRunStatistics stats, int runCount, Writer out) throws IOException {
        // First, generate the standard single-run report structure
        this.generate(report, out);

        if (stats == null) return;

        // Append the Statistical Aggregate Report Section
        out.write("\n---\n\n");
        out.write("## 🧮 Multi-Run Statistical Aggregate Report\n\n");
        out.write("This section evaluates the convergence stability and stochastic robustness of the algorithm aggregated across **" + runCount + "** distinct optimization runs.\n\n");

        out.write("| Statistical Metric | Value |\n");
        out.write("| :--- | :--- |\n");
        out.write("| **Mean Fitness (μ)** | " + String.format("%.10f", stats.getMean()) + " |\n");
        out.write("| **Median Fitness** | " + String.format("%.10f", stats.getMedian()) + " |\n");
        out.write("| **Standard Deviation (σ)** | " + String.format("%.10f", stats.getStdDev()) + " |\n");
        out.write("| **25% Quantile (Q1)** | " + String.format("%.10f", stats.getQ25()) + " |\n");
        out.write("| **75% Quantile (Q3)** | " + String.format("%.10f", stats.getQ75()) + " |\n");
        out.write("| **95% Confidence Interval (CI)** | `[" + String.format("%.10f", stats.getCiLower()) + " , " + String.format("%.10f", stats.getCiUpper()) + "]` |\n\n");

        out.write("> 💡 *Note: The confidence interval is calculated via Student's t-distribution reflecting the finite sample size boundary constraint.*\n");
        out.flush();
    }
}