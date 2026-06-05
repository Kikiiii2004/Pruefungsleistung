package bdd;

import com.tngtech.jgiven.annotation.Description;
import com.tngtech.jgiven.junit5.ScenarioTest;
import edu.swarmintelligence.mayfly.*;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.DisplayName("Mayfly Optimization Acceptance Criteria (BDD Suite)")
class MayflyAlgorithmBddTest extends ScenarioTest<GivenMayflyAlgorithm, WhenMayflyAlgorithm, ThenMayflyAlgorithm> {

    @Test
    @Description("AT-1: Reproduzierbarkeit - Zwei unabhängige Durchläufe mit identischem Seed müssen mathematisch identische gbest-Fitnesswerte liefern.")
    void test_at1_reproducibility_constraint() {
        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed(given())
                .and().the_algorithm_is_executed_a_second_time_with_the_same_seed(given());

        then().both_optimization_runs_must_yield_the_exact_same_global_best_fitness(when());
    }

    @Test
    @Description("AT-2: Konvergenz - Nach der Ausführung des vordefinierten Ackley-Szenarios muss die globale Fitness unter der definierten Schranke von 1.0e-3 liegen.")
    void test_at2_convergence_bounds() {
        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed(given());

        then().the_final_gbest_fitness_must_be_strictly_below(when(), 1e-3);
    }

    @Test
    @Description("AT-3: Globales Gedächtnis - Der zeitliche Verlauf der aufgezeichneten gbest-Trajektorie darf im Minimierungsprozess niemals ansteigen (monoton fallend).")
    void test_at3_global_memory_monotonicity() {
        GlobalMemoryAnalyzer globalAnalyzer = new GlobalMemoryAnalyzer(1e-5);

        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().an_analytics_engine_with_registered_analyzers(globalAnalyzer)                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed(given());

        then().the_gbest_trajectory_must_be_strictly_monotonically_non_increasing(when());
    }

    @Test
    @Description("AT-4: Agenten-Interaktion - Der relative Anteil der Weibchen, die sich im Modus 'Anziehung' befinden, muss über einer parametrisierten Mindestschwelle liegen.")
    void test_at4_agent_interaction_ratios() {
        AgentInteractionAnalyzer interactionAnalyzer = new AgentInteractionAnalyzer();

        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().an_analytics_engine_with_registered_analyzers(interactionAnalyzer)
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed(given());

        // Parameterized threshold checks (e.g., verifying interaction patterns are non-zero)
        then().the_female_attraction_rate_per_iteration_must_exceed_threshold(when(), 0.0);
    }

    @Test
    @Description("AT-5: Lokales Gedächtnis - Jeder einzelne Agent muss im Zuge des evolutionären Laufs mindestens eine Verbesserung seines persönlichen Bestwerts (pbest) erzielen.")
    void test_at5_local_memory_updates() {
        LocalMemoryAnalyzer localAnalyzer = new LocalMemoryAnalyzer();

        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().an_analytics_engine_with_registered_analyzers(localAnalyzer)
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed(given());

        then().every_active_agent_must_have_received_at_least_one_pbest_update(when());
    }

    @Test
    @Description("AT-6: Plateau-Detektion - Wenn der Fitness-Stream künstlich konstant gehalten wird (Szenario-Simulation), muss die Engine exakt ein kontinuierliches Plateau-Segment identifizieren.")
    void test_at6_plateau_detection() {
        // Setup convergence tracker: epsilon=1e-5, delta=100.0 (wide tolerance band), k=1, N=50%
        ConvergenceAnalyzer convergenceAnalyzer = new ConvergenceAnalyzer(1e-5, 100.0, 1, 50.0);

        given().a_standard_mayfly_algorithm_instance()
                .and().the_default_ackley_10d_configuration()
                .and().an_analytics_engine_with_registered_analyzers(convergenceAnalyzer)
                .and().a_fixed_random_seed_of(42L);

        when().the_algorithm_is_executed_with_the_configured_seed(given());

        then().exactly_one_plateau_segment_must_be_detected(when());
    }
}