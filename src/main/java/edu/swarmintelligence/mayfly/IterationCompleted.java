package edu.swarmintelligence.mayfly;

import java.util.List;

public record IterationCompleted(int iteration, double gbestFitness, List<Mayfly> survivors,List<Mayfly> matingMales, List<Mayfly> matingFemales) implements MayflyEvent {

}