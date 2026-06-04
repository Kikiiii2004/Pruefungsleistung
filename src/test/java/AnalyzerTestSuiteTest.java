import edu.swarmintelligence.mayfly.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzerTestSuiteTest {

    private static final long FIXED_SEED = 42L;

    @Nested
    class AnalyticsEngineTests {
        private AnalyticsEngine engine;
        private MayflyConfig config;

        @BeforeEach
        void setUp() {
            engine = new AnalyticsEngine();
            config = MayflyConfig.ackley10D();
        }

        @Test
        void testHappyPathEngineRegistrationAndBroadcasting() {
            AgentInteractionAnalyzer mockAnalyzer = new AgentInteractionAnalyzer();
            engine.registerAnalyzer(mockAnalyzer);

            // Broadcast an event through the engine
            engine.onEvent(new IterationStarted(1, 0.9));

            AnalyticsReport report = engine.generateReport(config, FIXED_SEED);
            assertThat(report.byAnalyzer()).containsKey("AgentInteractionAnalyzer");
            assertThat(report.seed()).isEqualTo(FIXED_SEED);
        }

        @Test
        void testEngineWithNullRegistrationEdgeCase() {
            engine.registerAnalyzer(null);
            AnalyticsReport report = engine.generateReport(config, FIXED_SEED);
            assertThat(report.byAnalyzer()).isEmpty();
        }

        @Test
        void testSequentialExecutionWithoutSideEffects() {
            AgentInteractionAnalyzer a1 = new AgentInteractionAnalyzer();
            AgentInteractionAnalyzer a2 = new AgentInteractionAnalyzer();
            engine.registerAnalyzer(a1);
            engine.registerAnalyzer(a2);

            engine.onEvent(new IterationStarted(5, 0.4));

            AnalyticsReport report = engine.generateReport(config, FIXED_SEED);
            assertThat(report.byAnalyzer()).hasSize(2);
        }
    }

    @Nested
    class AgentInteractionAnalyzerTests {
        private AgentInteractionAnalyzer analyzer;

        @BeforeEach
        void init() {
            analyzer = new AgentInteractionAnalyzer();
        }

        @Test
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
        void testFemaleAttractionRateCalculation() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            Mayfly agent = new Mayfly(10);

            analyzer.onEvent(new FemaleUpdated(agent, true, 20.0));
            analyzer.onEvent(new FemaleUpdated(agent, false, 20.0));

            InteractionResult res = (InteractionResult) analyzer.result();
            assertThat(res.femaleAttractionRate().get(1)).isEqualTo(0.5);
        }

        @Test
        void testMultiIterationDataPersistence() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationStarted(2, 0.8));

            InteractionResult res = (InteractionResult) analyzer.result();
            assertThat(res.femaleAttractionRate()).containsKey(1).containsKey(2);
        }

        @Test
        void testEmptyIterationEdgeCase() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 0.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            InteractionResult res = (InteractionResult) analyzer.result();
            assertThat(res.meanPairDistance().get(1)).isNull();
        }

        @Test
        void testConstantFitnessEdgeCase() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            Mayfly m = new Mayfly(2); Mayfly f = new Mayfly(2);
            m.fitness = 5.0; f.fitness = 5.0;

            analyzer.onEvent(new IterationCompleted(1, 5.0, List.of(m, f), List.of(m), List.of(f)));
            InteractionResult res = (InteractionResult) analyzer.result();
            assertThat(res.pairFitnessGap().get(1)).isEqualTo(0.0);
        }

        @Test
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
    class GlobalMemoryAnalyzerTests {
        private GlobalMemoryAnalyzer analyzer;

        @BeforeEach
        void init() {
            analyzer = new GlobalMemoryAnalyzer(1e-5);
        }

        @Test
        void testHappyPathTrajectoryTracking() {
            analyzer.onEvent(new IterationStarted(1, 0.8));
            analyzer.onEvent(new GbestUpdated(UpdateSource.MALE, 100.0, 50.0));
            analyzer.onEvent(new IterationCompleted(1, 50.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.gbestUpdateCount()).isEqualTo(1);
            assertThat(res.improvementDelta().get(1)).isEqualTo(50.0);
        }

        @Test
        void testFirstHittingIterationInInitializationPhase() {
            analyzer.onEvent(new IterationStarted(1, 0.8));
            analyzer.onEvent(new IterationCompleted(1, 1e-6, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.firstHittingIteration()).isEqualTo(0);
        }

        @Test
        void testFirstHittingIterationInLaterStep() {
            analyzer.onEvent(new IterationStarted(1, 0.8));
            analyzer.onEvent(new GbestUpdated(UpdateSource.MALE, 1.0, 1e-7));
            analyzer.onEvent(new IterationCompleted(1, 1e-7, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.firstHittingIteration()).isEqualTo(1);
        }

        @Test
        void testStagnationStreaksAccumulation() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 10.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            analyzer.onEvent(new IterationStarted(2, 0.9));
            analyzer.onEvent(new IterationCompleted(2, 10.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            analyzer.onEvent(new RunCompleted(new MayflyResult(new double[0], 10.0)));

            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.stagnationStreaks()).containsExactly(1);
        }

        @Test
        void testNumericalStabilityWithInfinityDeltas() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new GbestUpdated(UpdateSource.MALE, Double.POSITIVE_INFINITY, 50.0));
            analyzer.onEvent(new IterationCompleted(1, 50.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.improvementDelta().get(1)).isEqualTo(Double.POSITIVE_INFINITY);
        }

        @Test
        void testNoUpdatesDistributionEdgeCase() {
            GlobalMemoryResult res = (GlobalMemoryResult) analyzer.result();
            assertThat(res.gbestUpdateSourceDistribution().get(UpdateSource.MALE)).isEqualTo(0.0);
        }
    }

    @Nested
    class LocalMemoryAnalyzerTests {
        private LocalMemoryAnalyzer analyzer;

        @BeforeEach
        void init() {
            analyzer = new LocalMemoryAnalyzer();
        }

        @Test
        void testHappyPathPbestUpdatesTracking() {
            Mayfly agent = new Mayfly(2);
            analyzer.onEvent(new PbestUpdated(agent, 10.0, 5.0));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.pbestUpdateCountPerAgent()).containsEntry(agent, 1L);
            assertThat(res.meanPbestImprovement()).isEqualTo(0.5);
        }

        @Test
        void testExclusionOfInfinityFromMeanImprovement() {
            Mayfly agent = new Mayfly(2);
            analyzer.onEvent(new PbestUpdated(agent, Double.POSITIVE_INFINITY, 10.0));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.meanPbestImprovement()).isEqualTo(0.0);
        }

        @Test
        void testPbestPositionDiversityCalculation() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            Mayfly a1 = new Mayfly(1); a1.pbestPos[0] = 0.0;
            Mayfly a2 = new Mayfly(1); a2.pbestPos[0] = 10.0;

            analyzer.onEvent(new IterationCompleted(1, 0.0, List.of(a1, a2), Collections.emptyList(), Collections.emptyList()));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.pbestPositionDiversity().get(1)).isEqualTo(5.0);
        }

        @Test
        void testPbestFitnessDistributionQuantiles() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            List<Mayfly> survivors = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                Mayfly m = new Mayfly(1); m.pbestFitness = i;
                survivors.add(m);
            }
            analyzer.onEvent(new IterationCompleted(1, 0.0, survivors, Collections.emptyList(), Collections.emptyList()));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.pbestFitnessDistribution().get(1).get(2)).isEqualTo(3.0); // Median
        }

        @Test
        void testEmptyPopulationEdgeCase() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 0.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.pbestPositionDiversity().get(1)).isNull();
        }

        @Test
        void testNumericalStabilityWithNegativeFitness() {
            Mayfly agent = new Mayfly(1);
            analyzer.onEvent(new PbestUpdated(agent, -2.0, -4.0));

            LocalMemoryResult res = (LocalMemoryResult) analyzer.result();
            assertThat(res.meanPbestImprovement()).isEqualTo(-1.0);
        }
    }

    @Nested
    class ConvergenceAnalyzerTests {
        private ConvergenceAnalyzer analyzer;

        @BeforeEach
        void init() {
            analyzer = new ConvergenceAnalyzer(1e-5, 1.0, 2, 50.0);
        }

        @Test
        void testHappyPathConvergenceCurve() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 42.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            assertThat(res.convergenceCurve()).containsExactly("1,42.0000000000");
        }

        @Test
        void testPopulationDiversityCalculation() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            Mayfly a1 = new Mayfly(1); a1.pos[0] = 0.0;
            Mayfly a2 = new Mayfly(1); a2.pos[0] = 3.0;

            analyzer.onEvent(new IterationCompleted(1, 0.0, List.of(a1, a2), Collections.emptyList(), Collections.emptyList()));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            assertThat(res.populationDiversity().get(1)).isEqualTo(3.0);
        }

        @Test
        void testIterationsToThresholdVelocity() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 1e-6, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            assertThat(res.iterationsToThreshold().get("Iterations")).isEqualTo(1.0);
        }

        @Test
        void testPlateauSegmentDetection() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, 10.0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            analyzer.onEvent(new IterationStarted(2, 0.9));
            analyzer.onEvent(new IterationCompleted(2, 10.2, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            analyzer.onEvent(new RunCompleted(new MayflyResult(new double[0], 10.2)));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            assertThat(res.plateauSegments()).containsExactly("1,2");
        }

        @Test
        void testLinearLogFitEstimation() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, Math.exp(-1), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            analyzer.onEvent(new IterationStarted(2, 0.9));
            analyzer.onEvent(new IterationCompleted(2, Math.exp(-2), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            assertThat(res.convergenceRateEstimate()).isCloseTo(-1.0, org.assertj.core.data.Offset.offset(1e-5));
        }

        @Test
        void testNumericalStabilityWithInfinityAvoidance() {
            analyzer.onEvent(new IterationStarted(1, 0.9));
            analyzer.onEvent(new IterationCompleted(1, Double.POSITIVE_INFINITY, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            analyzer.onEvent(new IterationStarted(2, 0.9));
            analyzer.onEvent(new IterationCompleted(2, Double.POSITIVE_INFINITY, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

            ConvergenceResult res = (ConvergenceResult) analyzer.result();
            // Checking log of infinity should be safely handled/skipped by regression engine
            assertThat(res.convergenceRateEstimate()).isEqualTo(0.0);
        }
    }
}