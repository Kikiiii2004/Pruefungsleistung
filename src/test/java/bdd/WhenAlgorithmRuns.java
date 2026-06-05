package bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import edu.swarmintelligence.mayfly.AnalyticsEngine;
import edu.swarmintelligence.mayfly.AnalyticsReport;
import edu.swarmintelligence.mayfly.MayflyAlgorithm;
import edu.swarmintelligence.mayfly.MayflyConfig;
import edu.swarmintelligence.mayfly.MayflyResult;

import java.util.List;

public class WhenAlgorithmRuns extends Stage<WhenAlgorithmRuns> {

    // Zustand aus der Given-Stage einlesen
    @ExpectedScenarioState
    protected MayflyAlgorithm algorithm;

    @ExpectedScenarioState
    protected MayflyConfig config;

    @ExpectedScenarioState
    protected AnalyticsEngine engine;

    @ExpectedScenarioState
    protected long seed;

    @com.tngtech.jgiven.annotation.ProvidedScenarioState(resolution = com.tngtech.jgiven.annotation.ScenarioState.Resolution.NAME)
    protected MayflyResult firstResult;

    @com.tngtech.jgiven.annotation.ProvidedScenarioState(resolution = com.tngtech.jgiven.annotation.ScenarioState.Resolution.NAME)
    protected MayflyResult secondResult;

    @ProvidedScenarioState
    protected AnalyticsReport report;

    @ExpectedScenarioState
    protected List<Long> multipleSeeds;

    @ProvidedScenarioState
    protected List<Double> multiRunFitnessResults;

    public WhenAlgorithmRuns the_algorithm_is_executed_with_the_configured_seed() {
        this.firstResult = this.algorithm.run(this.config, this.seed);
        if (this.engine != null) {
            this.report = this.engine.generateReport(this.config, this.seed);
        }
        return self();
    }

    public WhenAlgorithmRuns the_algorithm_is_executed_a_second_time_with_the_same_seed() {
        this.secondResult = this.algorithm.run(this.config, this.seed);
        return self();
    }

    public WhenAlgorithmRuns the_algorithm_is_executed_for_all_provided_seeds() {
        this.multiRunFitnessResults = new java.util.ArrayList<>();
        for (long currentSeed : this.multipleSeeds) {
            MayflyResult runResult = this.algorithm.run(this.config, currentSeed);
            this.multiRunFitnessResults.add(runResult.gbestFitness());
        }
        return self();
    }
}