package edu.swarmintelligence.mayfly;

public record PbestUpdated(Mayfly agent, double previousPbestFitness, double newPbestFitness) implements MayflyEvent {
}
