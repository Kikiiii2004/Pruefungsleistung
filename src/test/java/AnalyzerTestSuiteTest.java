import edu.swarmintelligence.mayfly.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Mayfly Behavioral Analyzer Telemetry Suite")
class AnalyzerTestSuiteTest {

    private static final long FIXED_SEED = 42L;
    private static MayflyConfig sharedConfig;

    @BeforeAll
    static void setupGlobalFixtures() {
        // Heavy or immutable test fixture shared across all nested test domains
        sharedConfig = MayflyConfig.ackley10D();
    }

    @Nested
    @DisplayName("Analytics Engine Core Tests")
    class AnalyticsEngineTests {
        private AnalyticsEngine engine;

        @BeforeEach
        void setUp() {
            engine = new AnalyticsEngine();
        }

        @Test
        @DisplayName("Should successfully register analyzers and broadcast incoming events sequentially")
        void testHappyPathEngineRegistrationAndBroadcasting() {
            AgentInteractionAnalyzer mockAnalyzer = new AgentInteractionAnalyzer();
            engine.registerAnalyzer(mockAnalyzer);

            engine.onEvent(new IterationStarted(1, 0.9));

            AnalyticsReport report = engine.generateReport(sharedConfig, FIXED_SEED);
            assertThat(report.byAnalyzer()).containsKey("AgentInteractionAnalyzer");
            assertThat(report.seed()).isEqualTo(FIXED_SEED);
        }

        @Test
        @DisplayName("Should gracefully handle null registrations without breaking pipeline validation")
        void testEngineWithNullRegistrationEdgeCase() {
            engine.registerAnalyzer(null);
            AnalyticsReport report = engine.generateReport(sharedConfig, FIXED_SEED);
            assertThat(report.byAnalyzer()).isEmpty();
        }

        @Test
        @DisplayName("Should process independent analyzer state tables sequentially without side effects")
        void testSequentialExecutionWithoutSideEffects() {
            AgentInteractionAnalyzer a1 = new AgentInteractionAnalyzer();
            GlobalMemoryAnalyzer a2 = new GlobalMemoryAnalyzer(1e-5);
            engine.registerAnalyzer(a1);
            engine.registerAnalyzer(a2);

            engine.onEvent(new IterationStarted(5, 0.4));

            AnalyticsReport report = engine.generateReport(sharedConfig, FIXED_SEED);
            assertThat(report.byAnalyzer()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Agent Interaction Analyzer Tests")
    class AgentInteractionAnalyzerTests {
        private AgentInteractionAnalyzer analyzer;

        @BeforeEach
        void init() {
            analyzer = new AgentInteractionAnalyzer();
        }

        @Test
        @DisplayName("Should correctly distinguish nuptial dances from male tracking attraction steps")
        void testHappyPathInteractionCounting() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            Mayfly agent = new Mayfly(10);

            analyzer.onEvent(new MaleUpdated(agent, true, 10.0));
            analyzer.onEvent(new MaleUpdated(agent, false, 10.0));
            analyzer.onEvent(new FemaleUpdated(agent, true, 15.0));
            analyzer.onEvent(new FemaleUpdated(agent, false, 12.0));

            InteractionResult res = (InteractionResult) analyzer.result();
            assertThat(res.nuptialDanceCount()).isEqualTo(1);
            assertThat(res.attractionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should evaluate the current iteration female attraction rate accurately")
        void testFemaleAttractionRateCalculation() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            Mayfly agent = new Mayfly(10);

            analyzer.onEvent(new FemaleUpdated(agent, true, 20.0));
            analyzer.onEvent(new FemaleUpdated(agent, false, 20.0));

            InteractionResult res = (InteractionResult) analyzer.result();
            assertThat(res.femaleAttractionRate().get(1)).isEqualTo(0.5);
        }

        @Test
        @DisplayName("Should persist interaction profiles across multiple sequential iteration boundaries")
        void testMultiIterationDataPersistence() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationStarted(2, 0.8));

            InteractionResult res = (InteractionResult) analyzer.result();
            assertThat(res.femaleAttractionRate()).containsKey(1).containsKey(2);
        }

        @Test
        @DisplayName("Should yield NaN evaluations for pairing metrics when facing an empty population")
        void testEmptyIterationEdgeCase() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 0.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            InteractionResult res = (InteractionResult) analyzer.result();
            assertThat(res.meanPairDistance().get(1)).isNaN();
        }

        @Test
        @DisplayName("Should calculate a clear zero gap when processing matching constant fitness states")
        void testConstantFitnessEdgeCase() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            Mayfly m = new Mayfly(2); Mayfly f = new Mayfly(2);
            m.fitness = 5.0; f.fitness = 5.0;

            analyzer.onEvent(new IterationCompleted(1, 5.0, List.of(m, f), List.of(m), List.of(f)));
            InteractionResult res = (InteractionResult) analyzer.result();
            assertThat(res.pairFitnessGap().get(1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should robustly capture infinite fitness boundaries during distance gap processing")
        void testNumericalStabilityWithInfinity() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            Mayfly m = new Mayfly(2); Mayfly f = new Mayfly(2);
            m.fitness = Double.POSITIVE_INFINITY; f.fitness = 5.0;

            analyzer.onEvent(new IterationCompleted(1, 5.0, List.of(m, f), List.of(m), List.of(f)));
            InteractionResult res = (InteractionResult) analyzer.result();
            assertThat(res.pairFitnessGap().get(1)).isEqualTo(Double.POSITIVE_INFINITY);
        }
    }

    @Nested
    @DisplayName("Global Memory Analyzer Tests")
    class GlobalMemoryAnalyzerTests {
        private GlobalMemoryAnalyzer analyzer;
        private final double testEpsilon = 1e-5;

        @BeforeEach
        void init() {
            analyzer = new GlobalMemoryAnalyzer(testEpsilon);
        }

        @Test
        @DisplayName("Should log a consistent path trace mapping standard evolutionary developments")
        void testHappyPathTrajectoryTracking() {
            analyzer.onEvent(new IterationStarted(1, 0.8));
            analyzer.onEvent(new GbestUpdated(UpdateSource.MALE, 100.0, 50.0));
            analyzer.onEvent(new IterationCompleted(1, 50.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.gbestUpdateCount()).isEqualTo(1);
            assertThat(res.gbestTrajectory()).isNotEmpty();
        }

        @Test
        @DisplayName("Should detect hitting iteration zero when initialized below target limits")
        void testFirstHittingIterationInInitializationPhase() {
            GlobalMemoryAnalyzer initAnalyzer = new GlobalMemoryAnalyzer(1e-5);
            initAnalyzer.onEvent(new GbestUpdated(UpdateSource.MALE, 1.0, 1e-9));
            initAnalyzer.onEvent(new IterationCompleted(1, 1e-9, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            GlobalMemoryResult res = (GlobalMemoryResult) initAnalyzer.result();
            assertThat(res.firstHittingIteration()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should correctly tag the active step iteration index on late hitting bounds")
        void testFirstHittingIterationInLaterStep() {
            analyzer.onEvent(new IterationStarted(1, 0.8));
            analyzer.onEvent(new GbestUpdated(UpdateSource.MALE, 1.0, 1e-6));
            analyzer.onEvent(new IterationCompleted(1, 1e-6, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.firstHittingIteration()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should build accurate length profiles for sequential stagnant iterations")
        void testStagnationStreaksAccumulation() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new GbestUpdated(UpdateSource.MALE, 20.0, 10.0));
            analyzer.onEvent(new IterationCompleted(1, 10.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            analyzer.onEvent(new IterationStarted(2, 0.9));
            analyzer.onEvent(new IterationCompleted(2, 10.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            analyzer.onEvent(new RunCompleted(new MayflyResult(new double[0], 10.0)));

            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.stagnationStreaks()).containsExactly(1);
        }

        @Test
        @DisplayName("Should maintain numerical stability when receiving infinite baseline deltas")
        void testNumericalStabilityWithInfinityDeltas() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new GbestUpdated(UpdateSource.MALE, Double.POSITIVE_INFINITY, 50.0));
            analyzer.onEvent(new IterationCompleted(1, 50.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.improvementDelta().get(1)).isEqualTo(Double.POSITIVE_INFINITY);
        }

        @Test
        @DisplayName("Should yield an empty distribution template if zero best value updates arrive")
        void testNoUpdatesDistributionEdgeCase() {
            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.gbestUpdateSourceDistribution().get(UpdateSource.MALE)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Local Memory Analyzer Tests")
    class LocalMemoryAnalyzerTests {
        private LocalMemoryAnalyzer analyzer;

        @BeforeEach
        void init() {
            analyzer = new LocalMemoryAnalyzer();
        }

        @Test
        @DisplayName("Should record personal improvements and link tracking operations to individual instances")
        void testHappyPathPbestUpdatesTracking() {
            Mayfly agent = new Mayfly(2);
            analyzer.onEvent(new PbestUpdated(agent, 10.0, 5.0));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.pbestUpdateCountPerAgent()).containsEntry(agent, 1L);
            assertThat(res.meanPbestImprovement()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("Should drop initial assignments out of infinity to keep statistical averages clean")
        void testExclusionOfInfinityFromMeanImprovement() {
            Mayfly agent = new Mayfly(2);
            analyzer.onEvent(new PbestUpdated(agent, Double.POSITIVE_INFINITY, 10.0));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.meanPbestImprovement()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should compute coordinate component standard deviations to track personal best spatial layout")
        void testPbestPositionDiversityCalculation() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            Mayfly a1 = new Mayfly(1); a1.pbestPos[0] = 0.0;
            Mayfly a2 = new Mayfly(1); a2.pbestPos[0] = 10.0;

            analyzer.onEvent(new IterationCompleted(1, 0.0, List.of(a1, a2), Collections.emptyList(), Collections.emptyList()));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.pbestPositionDiversity().get(1)).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Should partition fitness pools into sorted quantile segments at the iteration end step")
        void testPbestFitnessDistributionQuantiles() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            List<Mayfly> survivors = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                Mayfly m = new Mayfly(1); m.pbestFitness = i;
                survivors.add(m);
            }
            analyzer.onEvent(new IterationCompleted(1, 0.0, survivors, Collections.emptyList(), Collections.emptyList()));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.pbestFitnessDistribution().get(1).get(2)).isEqualTo(3.0);
        }

        @Test
        @DisplayName("Should omit tracking steps and yield null keys if populations vanish completely")
        void testEmptyPopulationEdgeCase() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 0.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.pbestPositionDiversity().get(1)).isNull();
        }

        @Test
        @DisplayName("Should handle mathematical operations accurately when evaluating negative objective dimensions")
        void testNumericalStabilityWithNegativeFitness() {
            Mayfly agent = new Mayfly(1);
            analyzer.onEvent(new PbestUpdated(agent, -2.0, -4.0));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.meanPbestImprovement()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("Convergence Analyzer Tests")
    class ConvergenceAnalyzerTests {
        private ConvergenceAnalyzer analyzer;

        @BeforeEach
        void init() {
            analyzer = new ConvergenceAnalyzer(1e-5, 0.5, 1, 100.0);
        }

        @Test
        @DisplayName("Should log objective values inside a curve format separated by standard commas")
        void testHappyPathConvergenceCurve() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 42.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            String actual = res.convergenceCurve().get(0);
            assertThat(actual).contains("1,").contains("42");
        }

        @Test
        @DisplayName("Should extract pairwise coordinate distances among verified survivors exclusively")
        void testPopulationDiversityCalculation() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            Mayfly a1 = new Mayfly(1); a1.pos[0] = 0.0;
            Mayfly a2 = new Mayfly(1); a2.pos[0] = 3.0;

            analyzer.onEvent(new IterationCompleted(1, 0.0, List.of(a1, a2), Collections.emptyList(), Collections.emptyList()));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            assertThat(res.populationDiversity().get(1)).isEqualTo(3.0);
        }

        @Test
        @DisplayName("Should yield correct structural counts for milestones tracking hitting operations")
        void testIterationsToThresholdVelocity() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 1e-6, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            assertThat(res.iterationsToThreshold().get("Iterations")).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should filter out continuous steps stalled inside the active parameter tolerance band")
        void testPlateauSegmentDetection() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 10.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            analyzer.onEvent(new IterationStarted(2, 0.9));
            analyzer.onEvent(new IterationCompleted(2, 10.1, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            analyzer.onEvent(new RunCompleted(new MayflyResult(new double[0], 10.1)));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            assertThat(res.plateauSegments()).containsExactly("1,2");
        }

        @Test
        @DisplayName("Should perform ordinary least squares linear log regressions on historical trajectories")
        void testLinearLogFitEstimation() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, Math.exp(-1), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            analyzer.onEvent(new IterationStarted(2, 0.9));
            analyzer.onEvent(new IterationCompleted(2, Math.exp(-2), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            analyzer.onEvent(new IterationStarted(3, 0.9));
            analyzer.onEvent(new IterationCompleted(3, Math.exp(-3), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            analyzer.onEvent(new RunCompleted(new MayflyResult(new double[0], Math.exp(-3))));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            assertThat(res.convergenceRateEstimate()).isCloseTo(-1.0, org.assertj.core.data.Offset.offset(1e-5));
        }

        @Test
        @DisplayName("Should evaluate regression results safely to a flat zero when stalling continuously")
        void testNumericalStabilityWithInfinityAvoidance() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 1.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            analyzer.onEvent(new IterationStarted(2, 0.9));
            analyzer.onEvent(new IterationCompleted(2, 1.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            analyzer.onEvent(new RunCompleted(new MayflyResult(new double[0], 1.0)));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            assertThat(res.convergenceRateEstimate()).isEqualTo(0.0);
        }
    }
}