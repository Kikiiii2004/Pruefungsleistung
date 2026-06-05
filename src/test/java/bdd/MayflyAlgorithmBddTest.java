package bdd;

import com.tngtech.jgiven.annotation.Description;
import org.junit.jupiter.api.Tag;
import com.tngtech.jgiven.junit5.ScenarioTest;
import edu.swarmintelligence.mayfly.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Mayfly Optimization Acceptance Criteria (BDD Suite)")
class MayflyAlgorithmBddTest extends ScenarioTest<GivenMayflyConfiguration, WhenAlgorithmRuns, ThenAnalyticsReport> {

    @Test
    @Description("AT-1: Reproduzierbarkeit - Gleicher Seed liefert mathematisch identische gbest Fitnesswerte.")
    void test_at1_reproducibility_constraint() {
        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed()
                .and().the_algorithm_is_executed_a_second_time_with_the_same_seed();

        then().both_optimization_runs_must_yield_the_exact_same_global_best_fitness();
    }

    @Test
    @Description("AT-2: Konvergenz - Ackley(gbest) liegt nach 1000 Iterationen unter der Schranke von 1.0e-3.")
    void test_at2_convergence_bounds() {
        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed();

        then().the_final_gbest_fitness_must_be_strictly_below(1e-3);
    }

    @Test
    @Tag("global-memory") // Erfüllt Anforderung für AT-3
    @Description("AT-3: Globales Gedächtnis - Die gbest-Trajektorie verläuft monoton fallend.")
    void test_at3_global_memory_monotonicity() {
        GlobalMemoryAnalyzer globalAnalyzer = new GlobalMemoryAnalyzer(1e-5);

        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().an_analytics_engine_with_registered_analyzers(globalAnalyzer)
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed();

        then().the_gbest_trajectory_must_be_strictly_monotonically_non_increasing();
    }

    @Test
    @Tag("agent-interaction") // Erfüllt Anforderung für AT-4
    @Description("AT-4: Agenten-Interaktion - Der Anteil weiblicher Anziehungen liegt über der parametrisierten Schwelle.")
    void test_at4_agent_interaction_ratios() {
        AgentInteractionAnalyzer interactionAnalyzer = new AgentInteractionAnalyzer();

        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().an_analytics_engine_with_registered_analyzers(interactionAnalyzer)
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed();

        then().the_female_attraction_rate_per_iteration_must_exceed_threshold(0.0);
    }

    @Test
    @Tag("local-memory") // Erfüllt Anforderung für AT-5
    @Description("AT-5: Lokales Gedächtnis - Jeder Agent erhält im Lauf mindestens eine pbest-Verbesserung.")
    void test_at5_local_memory_updates() {
        LocalMemoryAnalyzer localAnalyzer = new LocalMemoryAnalyzer();

        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().an_analytics_engine_with_registered_analyzers(localAnalyzer)
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed();

        then().every_active_agent_must_have_received_at_least_one_pbest_update();
    }

    @Test
    @Tag("convergence") // Erfüllt Anforderung für AT-6
    @Description("AT-6: Plateau-Detektion - Bei konstant gehaltener Fitness wird exakt 1 kontinuierliches Plateau erkannt.")
    void test_at6_plateau_detection() {
        ConvergenceAnalyzer convergenceAnalyzer = new ConvergenceAnalyzer(1e-5, 100.0, 1, 50.0);

        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().an_analytics_engine_with_registered_analyzers(convergenceAnalyzer)
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed();

        then().exactly_one_plateau_segment_must_be_detected();
    }
}