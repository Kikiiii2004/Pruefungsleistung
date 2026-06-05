package edu.swarmintelligence.mayfly;

public class Main {
    public static void main(String[] args) {
        long seed = 42L;
        MayflyConfig cfg = MayflyConfig.ackley10D();
        MayflyAlgorithm algo = new MayflyAlgorithm();

        // 1. Analytics Engine
        AnalyticsEngine engine = new AnalyticsEngine();

        // 2. Analyzer registrieren
        double epsilon = 1e-8; // Schwellwert für firstHittingIteration
        engine.registerAnalyzer(new AgentInteractionAnalyzer());
        engine.registerAnalyzer(new GlobalMemoryAnalyzer(epsilon));

        // 3. Engine als Listener anhängen
        algo.addListener(engine);

        // 4. Optimierung ausführen
        MayflyResult runResult = algo.run(cfg, seed);
        System.out.printf("Optimization Finished. Global Best Fitness: %.10f%n", runResult.gbestFitness());

        // 5. Report erzeugen
        AnalyticsReport report = engine.generateReport(cfg, seed);
        System.out.println("Analytics Report generated.");

        // 6. NEU: Markdown-Report in Datei schreiben
        try (java.io.FileWriter fw = new java.io.FileWriter("Mayfly_Analytics_Report.md")) {
            MarkdownReportGenerator mdGenerator = new MarkdownReportGenerator();
            mdGenerator.generate(report, fw);
            System.out.println("Markdown Report erfolgreich unter 'Mayfly_Analytics_Report.md' gespeichert!");
        } catch (java.io.IOException ex) {
            System.err.println("Fehler beim Schreiben des Markdown-Reports: " + ex.getMessage());
        }
    }
}