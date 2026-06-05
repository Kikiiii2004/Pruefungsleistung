package edu.swarmintelligence.mayfly;

import java.io.IOException;
import java.io.Writer;

public interface AnalyticsExporter {
    void export(AnalyticsReport report, Writer out) throws IOException;
}