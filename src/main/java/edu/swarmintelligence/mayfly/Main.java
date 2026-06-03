package edu.swarmintelligence.mayfly;

public class Main {
    public static void main(String[] args) {
        MayflyConfig cfg = MayflyConfig.ackley10D();
        MayflyAlgorithm algo = new MayflyAlgorithm();

        algo.addListener(e -> {
            if (e instanceof GbestUpdated gbestEvent) {
                System.out.println("Neuer globaler Bestwert: " + gbestEvent.newGbestFitness());
            } else if (e instanceof IterationCompleted event) {
                System.out.println("Iteration " + event.iteration() + " beendet.");
            }
        });

        MayflyResult result = algo.run(cfg, 42L);
        System.out.printf("Final Best Fitness: %.10f%n", result.gbestFitness());
    }
}