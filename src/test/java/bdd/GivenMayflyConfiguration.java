package bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import edu.swarmintelligence.mayfly.AnalyticsEngine;
import edu.swarmintelligence.mayfly.MayflyAlgorithm;
import edu.swarmintelligence.mayfly.MayflyAnalyzer;
import edu.swarmintelligence.mayfly.MayflyConfig;

import java.util.List;

public class GivenMayflyConfiguration extends Stage<GivenMayflyConfiguration> {

    @ProvidedScenarioState
    protected MayflyAlgorithm algorithm;

    @ProvidedScenarioState
    protected MayflyConfig config;

    @ProvidedScenarioState
    protected AnalyticsEngine engine;

    @ProvidedScenarioState
    protected long seed;

    @ProvidedScenarioState
    protected List<Long> multipleSeeds;

    public GivenMayflyConfiguration a_standard_mayfly_algorithm_instance() {
        this.algorithm = new MayflyAlgorithm();
        return self(); // Fluent Pattern über die eingebaute self() Methode von JGiven
    }

    public GivenMayflyConfiguration the_default_ackley_10d_configuration() {
        this.config = MayflyConfig.ackley10D();
        return self();
    }

    public GivenMayflyConfiguration an_analytics_engine_with_registered_analyzers(MayflyAnalyzer... analyzers) {
        this.engine = new AnalyticsEngine();
        for (MayflyAnalyzer analyzer : analyzers) {
            this.engine.registerAnalyzer(analyzer);
        }
        this.algorithm.addListener(this.engine);
        return self();
    }

    public GivenMayflyConfiguration a_fixed_random_seed_of(long seed) {
        this.seed = seed;
        return self();
    }

    public GivenMayflyConfiguration a_list_of_distinct_random_seeds(int count) {
        this.multipleSeeds = new java.util.ArrayList<>();
        // Generiere deterministische, aber unterschiedliche Seeds basierend auf einer Basis
        for (int i = 1; i <= count; i++) {
            this.multipleSeeds.add((long) (i * 100));
        }
        return self();
    }
}