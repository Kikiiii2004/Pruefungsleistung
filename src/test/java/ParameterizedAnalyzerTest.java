import edu.swarmintelligence.mayfly.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Mayfly Parameterized Telemetry Constraints Suite")
class ParameterizedAnalyzerTest {

    @ParameterizedTest(name = "Should correctly track first hitting iteration for epsilon threshold = {0}")
    @ValueSource(doubles = {1e-3, 1e-5, 1e-8})
    @DisplayName("Verify first hitting thresholds using diverse ValueSource structures")
    void testGlobalMemoryAnalyzerThresholdsWithValueSource(double epsilon) {
        GlobalMemoryAnalyzer analyzer = new GlobalMemoryAnalyzer(epsilon);

        analyzer.onEvent(new IterationStarted(1, 0.9));
        analyzer.onEvent(new GbestUpdated(UpdateSource.MALE, 1.0, 1e-9));
        analyzer.onEvent(new IterationCompleted(1, 1e-9, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

        GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
        assertThat(res.firstHittingIteration()).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({
            "10.0, 5.0, 0.5",
            "100.0, 10.0, 0.9",
            "4.0, 3.0, 0.25"
    })
    @DisplayName("Verify mean pbest relative improvements via structured CsvSource definitions")
    void testLocalMemoryAnalyzerMeanImprovementWithCsvSource(double oldPbest, double newPbest, double expectedImprovement) {
        LocalMemoryAnalyzer analyzer = new LocalMemoryAnalyzer();
        Mayfly agent = new Mayfly(1);

        analyzer.onEvent(new PbestUpdated(agent, oldPbest, newPbest));

        LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
        assertThat(res.meanPbestImprovement()).isCloseTo(expectedImprovement, org.assertj.core.data.Offset.offset(1e-5));
    }

    @ParameterizedTest
    @MethodSource("provideDeltaAndExpectedPlateaus")
    @DisplayName("Verify complex plateau tracking boundaries via dynamic MethodSource stream bindings")
    void testConvergenceAnalyzerPlateausWithMethodSource(PlateauTestData testData) {
        ConvergenceAnalyzer analyzer = new ConvergenceAnalyzer(1e-5, testData.delta(), 2, 50.0);

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

    static Stream<PlateauTestData> provideDeltaAndExpectedPlateaus() {
        return Stream.of(
                new PlateauTestData(0.5, true),
                new PlateauTestData(0.01, false)
        );
    }

    private record PlateauTestData(double delta, boolean shouldFindPlateau) {}
}