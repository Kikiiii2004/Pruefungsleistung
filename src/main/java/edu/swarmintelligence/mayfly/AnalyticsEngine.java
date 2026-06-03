package edu.swarmintelligence.mayfly;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AnalyticsEngine implements MayflyEventListener {
    private final List<MayflyAnalyzer> analyzers = new ArrayList<>();

    public void registerAnalyzer(MayflyAnalyzer analyzer) {
        if (analyzer != null) {
            analyzers.add(analyzer);
        }
    }

    @Override
    public void onEvent(MayflyEvent e) {
        for (MayflyAnalyzer analyzer : analyzers) {
            analyzer.onEvent(e);
        }
    }

    public AnalyticsReport generateReport(MayflyConfig config, long seed) {
        Map<String, AnalyzerResult> reportData = new LinkedHashMap<>();

        for (MayflyAnalyzer analyzer : analyzers) {
            reportData.put(analyzer.name(), analyzer.result());
        }

        return new AnalyticsReport(reportData, Instant.now(), config, seed);
    }
}