package edu.swarmintelligence.mayfly;

import java.time.Instant;
import java.util.Map;


public record AnalyticsReport(
        Map<String, AnalyzerResult> byAnalyzer,
        Instant generatedAt,
        MayflyConfig config,
        long seed
) {

    public AnalyticsReport {
        if (byAnalyzer != null) {
            byAnalyzer = Map.copyOf(byAnalyzer);
        }
    }
}