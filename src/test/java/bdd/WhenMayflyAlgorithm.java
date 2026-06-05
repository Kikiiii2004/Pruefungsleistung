package bdd;

import com.tngtech.jgiven.Stage;
import edu.swarmintelligence.mayfly.AnalyticsReport;
import edu.swarmintelligence.mayfly.MayflyResult;

public class WhenMayflyAlgorithm extends Stage<WhenMayflyAlgorithm> {

    // These fields are automatically injected by JGiven's scenario state framework
    protected MayflyResult firstResult;
    protected MayflyResult secondResult;
    protected AnalyticsReport report;

    public WhenMayflyAlgorithm the_algorithm_is_executed_with_the_configured_seed(GivenMayflyAlgorithm given) {
        this.firstResult = given.algorithm.run(given.config, given.seed);
        if (given.engine != null) {
            this.report = given.engine.generateReport(given.config, given.seed);
        }
        return this;
    }

    public WhenMayflyAlgorithm the_algorithm_is_executed_a_second_time_with_the_same_seed(GivenMayflyAlgorithm given) {
        this.secondResult = given.algorithm.run(given.config, given.seed);
        return this;
    }
}