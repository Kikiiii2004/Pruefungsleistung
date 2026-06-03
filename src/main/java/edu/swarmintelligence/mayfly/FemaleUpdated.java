package edu.swarmintelligence.mayfly;


public record FemaleUpdated(Mayfly agent, boolean isAttracted, double previousFitness) implements MayflyEvent {
}
