package edu.swarmintelligence.mayfly;

public record GbestUpdated(UpdateSource source, double previousGbestFitness, double newGbestFitness) implements MayflyEvent {

}
