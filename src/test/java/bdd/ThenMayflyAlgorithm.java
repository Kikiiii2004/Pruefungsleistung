package bdd;

import com.tngtech.jgiven.Stage;
import edu.swarmintelligence.mayfly.*;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class ThenMayflyAlgorithm extends Stage<ThenMayflyAlgorithm> {

    public ThenMayflyAlgorithm both_optimization_runs_must_yield_the_exact_same_global_best_fitness(WhenMayflyAlgorithm when) {
        assertThat(when.firstResult.gbestFitness()).isEqualTo(when.secondResult.gbestFitness());
        return this;
    }

    public ThenMayflyAlgorithm the_final_gbest_fitness_must_be_strictly_below(WhenMayflyAlgorithm when, double threshold) {
        assertThat(when.firstResult.gbestFitness()).isLessThan(threshold);
        return this;
    }

    public ThenMayflyAlgorithm the_gbest_trajectory_must_be_strictly_monotonically_non_increasing(WhenMayflyAlgorithm when) {
        GlobalMemoryResult globalRes = (GlobalMemoryResult) when.report.byAnalyzer().get("GlobalMemoryAnalyzer");
        double lastFitness = Double.POSITIVE_INFINITY;

        for (GbestTrajectoryPoint point : globalRes.gbestTrajectory()) {
            assertThat(point.gbestFitness()).isLessThanOrEqualTo(lastFitness);
            lastFitness = point.gbestFitness();
        }
        return this;
    }

    public ThenMayflyAlgorithm the_female_attraction_rate_per_iteration_must_exceed_threshold(WhenMayflyAlgorithm when, double minThreshold) {
        InteractionResult interactionRes = (InteractionResult) when.report.byAnalyzer().get("AgentInteractionAnalyzer");
        for (double rate : interactionRes.femaleAttractionRate().values()) {
            assertThat(rate).isGreaterThan(minThreshold);
        }
        return this;
    }

    public ThenMayflyAlgorithm every_active_agent_must_have_received_at_least_one_pbest_update(WhenMayflyAlgorithm when) {
        LocalMemoryResult localRes = (LocalMemoryResult) when.report.byAnalyzer().get("LocalMemoryAnalyzer");
        assertThat(localRes.pbestUpdateCountPerAgent().values()).allMatch(count -> count >= 1);
        return this;
    }

    public ThenMayflyAlgorithm exactly_one_plateau_segment_must_be_detected(WhenMayflyAlgorithm when) {
        ConvergenceResult convergenceRes = (ConvergenceResult) when.report.byAnalyzer().get("ConvergenceAnalyzer");
        assertThat(convergenceRes.plateauSegments()).hasSize(1);
        return this;
    }
}