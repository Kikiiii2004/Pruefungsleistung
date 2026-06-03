package edu.swarmintelligence.mayfly;

public interface MayflyAnalyzer extends MayflyEventListener {
    String name();

    AnalyzerResult result();
}
