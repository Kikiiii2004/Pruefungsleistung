package edu.swarmintelligence.mayfly;

public record MayflyConfig(
        int dimensions,
        double lowerBound,
        double upperBound,
        int populationSize,
        int maxIterations,
        double wMax,
        double wMin,
        double a1,
        double a2,
        double a3,
        double beta,
        double danceCoeff,
        double flightCoeff,
        double mutationStdDev
) {
    public static MayflyConfig ackley10D() {
        return new MayflyConfig(
                10, -32.768, 32.768,       // bounds
                40, 1000,                  // pop size, iterations
                0.9, 0.4,                  // inertia weights
                1.0, 1.5, 1.5,             // attraction coefficients
                2.0,                       // beta
                0.1, 0.1,                  // dance, flight
                0.01 * (32.768 + 32.768)   // mutation std (0.01 × range) as per paper
        );
    }
}