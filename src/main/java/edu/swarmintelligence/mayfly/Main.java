package edu.swarmintelligence.mayfly;

public class Main {
    static void main() {
        MayflyConfig config = MayflyConfig.ackley10D();
        MayflyAlgorithm optimizer = new MayflyAlgorithm();
        double best = optimizer.run();
        System.out.printf("Final Best Fitness: %.10f%n", best);
    }
}