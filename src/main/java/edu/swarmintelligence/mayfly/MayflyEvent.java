package edu.swarmintelligence.mayfly;

public sealed interface MayflyEvent permits
        IterationStarted, MaleUpdated, FemaleUpdated, OffspringCreated,
        PbestUpdated, GbestUpdated, IterationCompleted, RunCompleted {
}
