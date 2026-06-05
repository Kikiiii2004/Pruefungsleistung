package edu.swarmintelligence.mayfly;

import java.util.Collections;
import java.util.List;

public class MultiRunStatistics {

    private final double mean;
    private final double median;
    private final double stdDev;
    private final double q25;
    private final double q75;
    private final double ciLower;
    private final double ciUpper;

    public MultiRunStatistics(List<Double> fitnessValues) {
        if (fitnessValues == null || fitnessValues.isEmpty()) {
            throw new IllegalArgumentException("Fitness values list cannot be null or empty");
        }

        int n = fitnessValues.size();
        // Sort copy to compute quantiles and median safely
        List<Double> sorted = fitnessValues.stream().sorted().toList();

        // 1. Mean
        double sum = 0.0;
        for (double val : sorted) {
            sum += val;
        }
        this.mean = sum / n;

        // 2. Median
        this.median = calculateQuantile(sorted, 0.50);

        // 3. Quantiles (25% and 75%)
        this.q25 = calculateQuantile(sorted, 0.25);
        this.q75 = calculateQuantile(sorted, 0.75);

        // 4. Standard Deviation
        double varianceSum = 0.0;
        for (double val : sorted) {
            varianceSum += (val - this.mean) * (val - this.mean);
        }
        // Using sample variance (n - 1) for statistical correctness
        this.stdDev = n > 1 ? Math.sqrt(varianceSum / (n - 1)) : 0.0;

        // 5. 95% Confidence Interval using t-distribution table values
        double tCritical = getTCritical95(n);
        double marginOfError = tCritical * (this.stdDev / Math.sqrt(n));
        this.ciLower = this.mean - marginOfError;
        this.ciUpper = this.mean + marginOfError;
    }

    private double calculateQuantile(List<Double> sortedValues, double quantile) {
        int n = sortedValues.size();
        double pos = quantile * (n - 1);
        int index = (int) Math.floor(pos);
        double fraction = pos - index;

        if (index + 1 < n) {
            return sortedValues.get(index) + fraction * (sortedValues.get(index + 1) - sortedValues.get(index));
        } else {
            return sortedValues.get(index);
        }
    }

    /**
     * Standard Student-t critical values table for alpha = 0.05 (Two-Tailed / 95% CI)
     * Maps degrees of freedom (df = n - 1) to t-value.
     */
    private double getTCritical95(int n) {
        int df = n - 1;
        if (df <= 0) return 0.0;

        return switch (df) {
            case 1 -> 12.706; case 2 -> 4.303;   case 3 -> 3.182;   case 4 -> 2.776;
            case 5 -> 2.571;  case 6 -> 2.447;   case 7 -> 2.365;   case 8 -> 2.306;
            case 9 -> 2.262;  // Standard for N=10 runs (df=9)
            case 10 -> 2.228; case 11 -> 2.201;  case 12 -> 2.179;  case 13 -> 2.160;
            case 14 -> 2.145; case 15 -> 2.131;  case 19 -> 2.093;  case 24 -> 2.064;
            case 29 -> 2.045; case 59 -> 2.000;  default -> 1.960;  // Large sample normal distribution limit
        };
    }

    // Getters for Reporting
    public double getMean() { return mean; }
    public double getMedian() { return median; }
    public double getStdDev() { return stdDev; }
    public double getQ25() { return q25; }
    public double getQ75() { return q75; }
    public double getCiLower() { return ciLower; }
    public double getCiUpper() { return ciUpper; }
}