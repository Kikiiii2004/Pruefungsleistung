package edu.swarmintelligence.mayfly;

public class Main {
    public static void main(String[] args) {
        long seed = 42L;
        MayflyConfig cfg = MayflyConfig.ackley10D();
        MayflyAlgorithm algo = new MayflyAlgorithm();

        // 1. Initialize the Analytics Engine
        AnalyticsEngine engine = new AnalyticsEngine();

        // 2. Register concrete analyzers here once implemented, e.g.:
        // engine.registerAnalyzer(new GbestTrajectoryAnalyzer());

        // 3. Attach the engine as a listener to the algorithm
        algo.addListener(engine);

        // 4. Run the optimization process
        MayflyResult runResult = algo.run(cfg, seed);
        System.out.printf("Optimization Finished. Global Best Fitness: %.10f%n", runResult.gbestFitness());

        // 5. Generate and evaluate the telemetry report
        AnalyticsReport report = engine.generateReport(cfg, seed);
        System.out.println("Analytics Report generated at: " + report.generatedAt());
        System.out.println("Analyzers executed: " + report.byAnalyzer().keySet());
    }
}