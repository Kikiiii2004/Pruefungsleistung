package bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.lang.junit5.SimpleScenarioTest; // FIX: lang.junit5 statt integration.junit5
import edu.swarmintelligence.mayfly.AnalyticsEngine;
import edu.swarmintelligence.mayfly.MayflyAlgorithm;
import edu.swarmintelligence.mayfly.MayflyConfig;
import edu.swarmintelligence.mayfly.MayflyAnalyzer;

public class GivenMayflyAlgorithm extends Stage<GivenMayflyAlgorithm> {

    protected MayflyAlgorithm algorithm;
    protected MayflyConfig config;
    protected AnalyticsEngine engine;
    protected long seed;

    public GivenMayflyAlgorithm a_standard_mayfly_algorithm_instance() {
        this.algorithm = new MayflyAlgorithm();
        return this;
    }

    public GivenMayflyAlgorithm the_default_ackley_10d_configuration() {
        this.config = MayflyConfig.ackley10D();
        return this;
    }

    // FIX: Den ungenutzten Parameter entfernt und den Typ korrigiert
    public GivenMayflyAlgorithm an_analytics_engine_with_registered_analyzers(MayflyAnalyzer... analyzers) {
        this.engine = new AnalyticsEngine();
        for (MayflyAnalyzer analyzer : analyzers) {
            this.engine.registerAnalyzer(analyzer);
        }
        this.algorithm.addListener(this.engine);
        return this;
    }

    public GivenMayflyAlgorithm a_fixed_random_seed_of(long seed) {
        this.seed = seed;
        return this;
    }
}