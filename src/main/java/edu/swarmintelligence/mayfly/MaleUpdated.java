package edu.swarmintelligence.mayfly;


public record MaleUpdated(Mayfly agent, boolean isNuptialDance, double previousFitness) implements MayflyEvent {
}