package bdd;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import edu.swarmintelligence.mayfly.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ThenAnalyticsReport extends Stage<ThenAnalyticsReport> {

    @ExpectedScenarioState
    protected MayflyResult firstResult;

    @ExpectedScenarioState
    protected MayflyResult secondResult;

    @ExpectedScenarioState
    protected AnalyticsReport report;

    public ThenAnalyticsReport both_optimization_runs_must_yield_the_exact_same_global_best_fitness() {
        assertThat(this.firstResult.gbestFitness()).isEqualTo(this.secondResult.gbestFitness());
        return self();
    }

    public ThenAnalyticsReport the_final_gbest_fitness_must_be_strictly_below(double threshold) {
        assertThat(this.firstResult.gbestFitness()).isLessThan(threshold);
        return self();
    }

    public ThenAnalyticsReport the_gbest_trajectory_must_be_strictly_monotonically_non_increasing() {
        GlobalMemoryResult globalRes = (GlobalMemoryResult) this.report.byAnalyzer().get("GlobalMemoryAnalyzer");
        double lastFitness = Double.POSITIVE_INFINITY;

        for (GbestTrajectoryPoint point : globalRes.gbestTrajectory()) {
            assertThat(point.gbestFitness()).isLessThanOrEqualTo(lastFitness);
            lastFitness = point.gbestFitness();
        }
        return self();
    }

    public ThenAnalyticsReport the_female_attraction_rate_per_iteration_must_exceed_threshold(double minThreshold) {
        InteractionResult interactionRes = (InteractionResult) this.report.byAnalyzer().get("AgentInteractionAnalyzer");
        for (double rate : interactionRes.femaleAttractionRate().values()) {
            assertThat(rate).isGreaterThan(minThreshold);
        }
        return self();
    }

    public ThenAnalyticsReport every_active_agent_must_have_received_at_least_one_pbest_update() {
        LocalMemoryResult localRes = (LocalMemoryResult) this.report.byAnalyzer().get("LocalMemoryAnalyzer");
        assertThat(localRes.pbestUpdateCountPerAgent().values()).allMatch(count -> count >= 1);
        return self();
    }

    public ThenAnalyticsReport exactly_one_plateau_segment_must_be_detected() {
        ConvergenceResult convergenceRes = (ConvergenceResult) this.report.byAnalyzer().get("ConvergenceAnalyzer");
        assertThat(convergenceRes.plateauSegments()).hasSize(1);
        return self();
    }
}