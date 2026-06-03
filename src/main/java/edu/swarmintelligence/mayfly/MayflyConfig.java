package edu.swarmintelligence.mayfly;

public record MayflyConfig(
        // Zulässiger Wertebereich | Default-Wert | Ergänzende Infos
        int dimensions,         // 10                      | 10           |
        double lowerBound,      // -32.768                 | -32.768      |
        double upperBound,      // 32.768                  | 32.768       |
        int populationSize,     // 20 - 200                | 50           | normalerweise zwischen 30 und 50
        int maxIterations,      // 200 - 5000              | 500          |
        double wMax,            // 0.2 - 0.9               | -            | bedingt durch wMin
        double wMin,            // 0.2 - 0.9               | -            | bedingt durch wMax
        double a1,              // 0.2 - 2.5               | 1.0          | kognitiv
        double a2,              // 0.5 - 2.5               | 1.5          | sozial
        double a3,              // 0.01 - 0.5              | 0.025        | Random-Flight-Koeffizient
        double beta,            // 0.7 - 0.99              | 0.8          |
        double danceCoeff,      // 0.1 - 10                | 5            |
        double flightCoeff,     // 0.1 - 2                 | 1            |
        double mutationStdDev   // 0.01 - 0.3              | 0.05         |
) {
    public MayflyConfig {
        if (dimensions <= 0) throw new IllegalArgumentException("Dimensions must be > 0");
        if (populationSize <= 1) throw new IllegalArgumentException("Population size must be > 1");
        if (lowerBound >= upperBound) throw new IllegalArgumentException("lowerBound must be < upperBound");
        if (wMin >= wMax) throw new IllegalArgumentException("wMin must be < wMax");
        if (a1 <= 0 || a2 <= 0 || a3 <= 0) throw new IllegalArgumentException("Coefficients must be > 0");
        if (beta <= 0 || danceCoeff <= 0 || flightCoeff <= 0 || mutationStdDev <= 0) {
            throw new IllegalArgumentException("Beta, danceCoeff, flightCoeff, and mutationStdDev must be > 0");
        }
    }

    public static MayflyConfig ackley10D() {
        return new MayflyConfig(
                10, -32.768, 32.768,        // bounds
                40, 1000,                                       // pop size, iterations
                0.9, 0.4,                                            // inertia weights
                1.0, 1.5, 1.5,                                     // attraction coefficients
                2.0,                                                       // beta
                0.1, 0.1,                                        // dance, flight
                0.01 * (32.768 + 32.768)                                   // mutation std (0.01 × range) as per paper
        );
    }
}
