import edu.swarmintelligence.mayfly.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterizedAnalyzerTest {

    // 1. Source: @ValueSource with Dynamic Display Name
    @ParameterizedTest(name = "Should correctly track first hitting iteration for epsilon threshold = {0}")
    @ValueSource(doubles = {1e-3, 1e-5, 1e-8})
    void testGlobalMemoryAnalyzerThresholdsWithValueSource(double epsilon) {
        GlobalMemoryAnalyzer analyzer = new GlobalMemoryAnalyzer(epsilon);

        analyzer.onEvent(new IterationStarted(1, 0.9));
        // Simulate finding a gbest that strictly undercuts all test epsilons
        analyzer.onEvent(new GbestUpdated(UpdateSource.MALE, 1.0, 1e-9));
        analyzer.onEvent(new IterationCompleted(1, 1e-9, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

        GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();

        // Assert that the threshold tracker triggered successfully
        assertThat(res.firstHittingIteration()).isEqualTo(1);
    }

    // 2. Source: @CsvSource
    @ParameterizedTest
    @CsvSource({
            "10.0, 5.0, 0.5",   // 10 -> 5  means 50% relative improvement
            "100.0, 10.0, 0.9", // 100 -> 10 means 90% relative improvement
            "4.0, 3.0, 0.25"    // 4 -> 3   means 25% relative improvement
    })
    void testLocalMemoryAnalyzerMeanImprovementWithCsvSource(double oldPbest, double newPbest, double expectedImprovement) {
        LocalMemoryAnalyzer analyzer = new LocalMemoryAnalyzer();
        Mayfly agent = new Mayfly(1);

        analyzer.onEvent(new PbestUpdated(agent, oldPbest, newPbest));

        LocalMemoryResult res = (LocalMemoryResult) analyzer.result();

        // Assert precision of mathematical calculation using AssertJ offset tolerance
        assertThat(res.meanPbestImprovement()).isCloseTo(expectedImprovement, org.assertj.core.data.Offset.offset(1e-5));
    }

    // 3. Source: @MethodSource (Fixed Simulation and Complete Implementation)
    @ParameterizedTest
    @MethodSource("provideDeltaAndExpectedPlateaus")
    void testConvergenceAnalyzerPlateausWithMethodSource(PlateauTestData testData) {
        // Setup analyzer with: epsilon=1e-5, delta=testData.delta, k=2, N=50%
        ConvergenceAnalyzer analyzer = new ConvergenceAnalyzer(1e-5, testData.delta(), 2, 50.0);

        // Simulate 3 iterations where fitness values decrease minimally (natural behavior)
        analyzer.onEvent(new IterationStarted(1, 0.9));
        analyzer.onEvent(new IterationCompleted(1, 10.10, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

        analyzer.onEvent(new IterationStarted(2, 0.9));
        analyzer.onEvent(new IterationCompleted(2, 10.05, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

        analyzer.onEvent(new IterationStarted(3, 0.9));
        analyzer.onEvent(new IterationCompleted(3, 10.00, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

        analyzer.onEvent(new RunCompleted(new MayflyResult(new double[0], 10.00)));

        ConvergenceResult res = (ConvergenceResult) analyzer.result();

        if (testData.shouldFindPlateau()) {
            assertThat(res.plateauSegments()).containsExactly("1,3");
        } else {
            assertThat(res.plateauSegments()).isEmpty();
        }
    }

    /**
     * Argument provider method supplying structured data combinations.
     */
    static Stream<PlateauTestData> provideDeltaAndExpectedPlateaus() {
        return Stream.of(
                new PlateauTestData(0.5, true),  // Step changes (0.05) are within tolerance band 0.5 -> Plateau found
                new PlateauTestData(0.01, false) // Step changes (0.05) break out of tolerance band 0.01 -> No plateau
        );
    }

    /**
     * Internal data container record for strongly typed parameter feeding.
     */
    private record PlateauTestData(double delta, boolean shouldFindPlateau) {}
}