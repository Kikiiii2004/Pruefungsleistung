package edu.swarmintelligence.mayfly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class MayflyAlgorithm {
    private final List<MayflyEventListener> listeners = new ArrayList<>();

    public void addListener(MayflyEventListener l) {
        listeners.add(l);
    }

    private void fireEvent(MayflyEvent e) {
        for (MayflyEventListener l : listeners) {
            l.onEvent(e);
        }
    }

    /**
     * Runs the algorithm and returns the best fitness found.
     */
    public MayflyResult run(MayflyConfig cfg, long seed) {
        RandomGenerator rng = RandomGeneratorFactory.of("Xoshiro256PlusPlus").create(seed);

        double[] gbestPosition = new double[cfg.dimensions()];
        double[] gbestFitness = { Double.POSITIVE_INFINITY };

        List<Mayfly> males = initializePopulation(cfg, rng);
        List<Mayfly> females = initializePopulation(cfg, rng);

        // Synchronous global best from initial populations
        males.forEach(m -> updateGlobalBest(m.fitness, m.pos, gbestFitness, gbestPosition, cfg, null));
        females.forEach(f -> updateGlobalBest(f.fitness, f.pos, gbestFitness, gbestPosition, cfg, null));

        Comparator<Mayfly> byFitness = Comparator.comparingDouble(m -> m.fitness);

        for (int iter = 1; iter <= cfg.maxIterations(); iter++) {
            double w = inertiaWeight(iter, cfg);

            fireEvent(new IterationStarted(iter, w));

            // ----- 1. Male movement (synchronous) -----
            moveMales(males, w, cfg, rng, gbestPosition, gbestFitness);

            // Re‑rank males for female movement and mating
            List<Mayfly> sortedMales = sortByFitness(males);

            // ----- 2. Female movement (synchronous) -----
            moveFemales(females, sortedMales, w, cfg, rng, gbestPosition, gbestFitness);

            // Re‑sort both populations after female movement
            sortedMales = sortByFitness(males);
            List<Mayfly> sortedFemales = sortByFitness(females);

            // ----- 3. Mating -----
            List<Mayfly> offspring = mate(sortedMales, sortedFemales, cfg, rng, gbestPosition, gbestFitness);

            // ----- 4. Global selection (Zervoudakis & Tsafarakis) -----
            List<Mayfly> pool = new ArrayList<>(cfg.populationSize() * 4);
            pool.addAll(males);
            pool.addAll(females);
            pool.addAll(offspring);
            pool.sort(byFitness);

            males = new ArrayList<>(pool.subList(0, cfg.populationSize()));
            females = new ArrayList<>(pool.subList(cfg.populationSize(), 2 * cfg.populationSize()));

            List<Mayfly> survivors = new ArrayList<>();
            survivors.addAll(males);
            survivors.addAll(females);

            fireEvent(new IterationCompleted(iter, gbestFitness[0], survivors));
        }
        MayflyResult result = new MayflyResult(gbestPosition, gbestFitness[0]);
        fireEvent(new RunCompleted(result));

        return result;
    }

            // ---------- Initialization ----------
    private List<Mayfly> initializePopulation(MayflyConfig cfg, RandomGenerator rng) {
        List<Mayfly> pop = new ArrayList<>(cfg.populationSize());
        for (int i = 0; i < cfg.populationSize(); i++) {
            Mayfly m = new Mayfly(cfg.dimensions());
            for (int d = 0; d < cfg.dimensions(); d++) {
                m.pos[d] = rng.nextDouble(cfg.lowerBound(), cfg.upperBound());
            }
            m.fitness = evaluate(m.pos, cfg);
            m.updatePersonalBest();
            pop.add(m);
        }
        return pop;
    }

    // ---------- Movement ----------
    private void moveMales(List<Mayfly> males, double w, MayflyConfig cfg, RandomGenerator rng, double[] gbestPosition, double[] gbestFitness) {
        Mayfly bestMale = males.stream().min(Comparator.comparingDouble(m -> m.fitness)).orElseThrow();

        // Compute new positions/velocities in temporary arrays (synchronous update)
        double[][] newPos = new double[males.size()][cfg.dimensions()];
        double[][] newVel = new double[males.size()][cfg.dimensions()];

        for (int i = 0; i < males.size(); i++) {
            Mayfly male = males.get(i);
            double dP2 = squaredDistance(male.pos, male.pbestPos);
            double dG2 = squaredDistance(male.pos, gbestPosition);

            for (int d = 0; d < cfg.dimensions(); d++) {
                double vel;
                if (male == bestMale) {
                    vel = w * male.vel[d] + cfg.danceCoeff() * rng.nextDouble(-1.0, 1.0);
                } else {
                    vel = w * male.vel[d]
                            + cfg.a1() * Math.exp(-cfg.beta() * dP2) * (male.pbestPos[d] - male.pos[d])
                            + cfg.a2() * Math.exp(-cfg.beta() * dG2) * (gbestPosition[d] - male.pos[d]);
                }
                newVel[i][d] = vel;
                newPos[i][d] = clamp(male.pos[d] + vel, cfg);
            }
        }

        // Apply and evaluate
        for (int i = 0; i < males.size(); i++) {
            Mayfly male = males.get(i);
            double prevFitness = male.fitness;
            boolean isDance = (male == bestMale);

            System.arraycopy(newPos[i], 0, male.pos, 0, cfg.dimensions());
            System.arraycopy(newVel[i], 0, male.vel, 0, cfg.dimensions());
            male.fitness = evaluate(male.pos, cfg);

            fireEvent(new MaleUpdated(male, isDance, prevFitness));

            double prevPbest = male.pbestFitness;
            male.updatePersonalBest();
            if (male.pbestFitness < prevPbest) {
                fireEvent(new PbestUpdated(male, prevPbest, male.pbestFitness));
            }

            updateGlobalBest(male.fitness, male.pos, gbestFitness, gbestPosition, cfg, UpdateSource.MALE);
        }
    }

    private void moveFemales(List<Mayfly> females, List<Mayfly> sortedMales, double w, MayflyConfig cfg, RandomGenerator rng, double[] gbestPosition, double[] gbestFitness) {
        // Sort females by current fitness (ascending)
        List<Mayfly> sortedFemales = sortByFitness(females);

        double[][] newPos = new double[cfg.populationSize()][cfg.dimensions()];
        double[][] newVel = new double[cfg.populationSize()][cfg.dimensions()];

        for (int i = 0; i < cfg.populationSize(); i++) {
            Mayfly female = sortedFemales.get(i);
            Mayfly male = sortedMales.get(i);   // pairing by rank
            boolean attracted = male.fitness < female.fitness;

            double dMF2 = attracted ? squaredDistance(female.pos, male.pos) : 0;

            for (int d = 0; d < cfg.dimensions(); d++) {
                double vel;
                if (attracted) {
                    vel = w * female.vel[d]
                            + cfg.a3() * Math.exp(-cfg.beta() * dMF2) * (male.pos[d] - female.pos[d]);
                } else {
                    vel = w * female.vel[d] + cfg.flightCoeff() * rng.nextDouble(-1.0, 1.0);
                }
                newVel[i][d] = vel;
                newPos[i][d] = clamp(female.pos[d] + vel, cfg);
            }
        }

        for (int i = 0; i < cfg.populationSize(); i++) {
            Mayfly female = sortedFemales.get(i);
            double previousFitness = female.fitness;

            Mayfly male = sortedMales.get(i);
            boolean isAttracted =male.fitness < female.fitness;

            System.arraycopy(newPos[i], 0, female.pos, 0, cfg.dimensions());
            System.arraycopy(newVel[i], 0, female.vel, 0, cfg.dimensions());
            female.fitness = evaluate(female.pos, cfg);

            fireEvent(new FemaleUpdated(female, isAttracted, previousFitness));

            double prevPbest = female.pbestFitness;
            female.updatePersonalBest();
            if (female.pbestFitness < prevPbest) {
                fireEvent(new PbestUpdated(female, prevPbest, female.pbestFitness));
            }

            updateGlobalBest(female.fitness, female.pos, gbestFitness, gbestPosition, cfg, UpdateSource.FEMALE);
        }
    }

    // ---------- Mating ----------
    private List<Mayfly> mate(List<Mayfly> sortedMales, List<Mayfly> sortedFemales, MayflyConfig cfg, RandomGenerator rng, double[] gbestPosition, double[] gbestFitness) {
        List<Mayfly> offspring = new ArrayList<>(cfg.populationSize() * 2);

        for (int i = 0; i < cfg.populationSize(); i++) {
            Mayfly p1 = sortedMales.get(i);
            Mayfly p2 = sortedFemales.get(i);
            double L = rng.nextDouble();

            Mayfly child1 = new Mayfly(cfg.dimensions());
            Mayfly child2 = new Mayfly(cfg.dimensions());

            for (int d = 0; d < cfg.dimensions(); d++) {
                child1.pos[d] = L * p1.pos[d] + (1.0 - L) * p2.pos[d];
                child2.pos[d] = L * p2.pos[d] + (1.0 - L) * p1.pos[d];
            }

            // Mutation: always applied to all dimensions of all offspring
            applyMutation(child1, cfg, rng);
            applyMutation(child2, cfg, rng);

            updateGlobalBest(child1.fitness, child1.pos, gbestFitness, gbestPosition, cfg, UpdateSource.OFFSPRING);
            updateGlobalBest(child2.fitness, child2.pos, gbestFitness, gbestPosition, cfg, UpdateSource.OFFSPRING);
            offspring.add(child1);
            offspring.add(child2);
        }
        return offspring;
    }

    private void applyMutation(Mayfly m, MayflyConfig cfg, RandomGenerator rng) {
        for (int d = 0; d < cfg.dimensions(); d++) {
            m.pos[d] += rng.nextGaussian() * cfg.mutationStdDev();
            m.pos[d] = clamp(m.pos[d], cfg);
        }
        m.fitness = evaluate(m.pos, cfg);
        m.updatePersonalBest();
    }

    // ---------- Helpers ----------
    private double inertiaWeight(int iter, MayflyConfig cfg) {
        return cfg.wMax() - (cfg.wMax() - cfg.wMin()) * ((double) iter / cfg.maxIterations());
    }

    private double clamp(double value, MayflyConfig cfg) {
        return Math.clamp(value, cfg.lowerBound(), cfg.upperBound());
    }

    private double squaredDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }

    private double evaluate(double[] x, MayflyConfig cfg) {
        double s1 = 0, s2 = 0;
        for (double v : x) {
            s1 += v * v;
            s2 += Math.cos(2 * Math.PI * v);
        }
        return -20.0 * Math.exp(-0.2 * Math.sqrt(s1 / cfg.dimensions()))
                - Math.exp(s2 / cfg.dimensions()) + 20.0 + Math.E;
    }

    private void updateGlobalBest(double fitness, double[] pos, double[] gbestFitness, double[] gbestPosition, MayflyConfig cfg, UpdateSource source) {
        double previousGbest = gbestFitness[0];

        if (fitness < previousGbest) {
            gbestFitness[0] = fitness;
            System.arraycopy(pos, 0, gbestPosition, 0, cfg.dimensions());

            if (source != null) {
                fireEvent(new GbestUpdated(source, previousGbest, gbestFitness[0]));
            }        }
    }

    private List<Mayfly> sortByFitness(List<Mayfly> list) {
        return list.stream()
                .sorted(Comparator.comparingDouble(m -> m.fitness))
                .toList();
    }
}