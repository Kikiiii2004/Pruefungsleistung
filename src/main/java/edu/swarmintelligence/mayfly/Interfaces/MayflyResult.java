package edu.swarmintelligence.mayfly.Interfaces;

public record MayflyResult(double[] gbestPosition, double[] gbestFitness) {
    public MayflyResult {
        gbestPosition = gbestPosition.clone();
    }
}
