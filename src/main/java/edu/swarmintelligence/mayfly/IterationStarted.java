package edu.swarmintelligence.mayfly;

public record IterationStarted(int iteration, double inertiaWeight) implements MayflyEvent {
}