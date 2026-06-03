package edu.swarmintelligence.mayfly;

public record MayflyResult(double[] gbestPosition, double gbestFitness) {
    public MayflyResult {
        if (gbestPosition != null) {
            gbestPosition = gbestPosition.clone();
        }
    }
}
