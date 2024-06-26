public class GeneticAlgorithm {
        private static final int CHROMOSOME_LENGTH = 2;
        private static final double BETA0_MIN = 1.0;
        private static final double BETA0_MAX = 200.0;
        private static final double BETA1_MIN = 0.0;
        private static final double BETA1_MAX = 50.0;
        private static final double CROSSOVER_RATE = 0.95;
        private static final double MUTATION_RATE = 0.01;
        private static final int ELITISM_COUNT = 0;

        private int populationSize;

        public GeneticAlgorithm(int populationSize) {
            this.populationSize = populationSize;
        }

        public Chromosome[] initializePopulation() {
            Chromosome[] population = new Chromosome[populationSize];
            Random random = new Random();

            for (int i = 0; i < populationSize; i++) {
                double beta0 = BETA0_MIN + (BETA0_MAX - BETA0_MIN) * random.nextDouble();
                double beta1 = BETA1_MIN + (BETA1_MAX - BETA1_MIN) * random.nextDouble();
                population[i] = new Chromosome(beta0, beta1);
            }

            return population;
        }

        public void crossover(Chromosome[] population, Chromosome[] newPopulation) {
            Random random = new Random();

            for (int i = 0; i < population.length; i++) {
                Chromosome individual = population[i];
                if (random.nextDouble() < CROSSOVER_RATE) {
                    Chromosome secondParent = selectParent(population);
                    Chromosome[] offspring = crossover(individual, secondParent);
                    newPopulation[i] = offspring[0];
                    if (i + 1 < population.length) {
                        newPopulation[i + 1] = offspring[1];
                        i++;
                    }
                } else {
                    newPopulation[i] = individual;
                }
            }
        }

        public Chromosome[] crossover(Chromosome parent1, Chromosome parent2) {
            Random random = new Random();

            double child1_beta0 = parent1.getBeta0();
            double child1_beta1 = parent2.getBeta1();
            double child2_beta0 = parent2.getBeta0();
            double child2_beta1 = parent1.getBeta1();

            if (random.nextDouble() < CROSSOVER_RATE) {
                double temp = child1_beta0;
                child1_beta0 = child2_beta0;
                child2_beta0 = temp;
            }

            if (random.nextDouble() < CROSSOVER_RATE) {
                double temp = child1_beta1;
                child1_beta1 = child2_beta1;
                child2_beta1 = temp;
            }

            return new Chromosome[]{new Chromosome(child1_beta0, child1_beta1), new Chromosome(child2_beta0, child2_beta1)};
        }

        public Chromosome selectParent(Chromosome[] population) {
            double totalFitness = 0.0;

            for (Chromosome chromosome : population) {
                totalFitness += chromosome.getFitness();
            }

            double rand = Math.random() * totalFitness;
            double runningSum = 0.0;

            for (Chromosome chromosome : population) {
                runningSum += chromosome.getFitness();
                if (runningSum >= rand) {
                    return chromosome;
                }
            }

            return null;
        }

        public double[] runGeneticAlgorithm(double[] x, double[] y, int maxGenerations) {
            Chromosome[] population = initializePopulation();

            for (int generation = 1; generation <= maxGenerations; generation++) {
                calculateFitness(population, x, y);

                Chromosome[] newPopulation = new Chromosome[populationSize];
                crossover(population, newPopulation);

                for (Chromosome chromosome : newPopulation) {
                    mutate(chromosome);
                }

                double bestFitness = -1;
                int bestChromosomeIndex = -1;
                for (int i = 0; i < population.length; i++) {
                    if (population[i].getFitness() >= 0.95) {
                        bestChromosomeIndex = i;
                        bestFitness = population[i].getFitness();
                        break;
                    }
                }

                if (bestFitness >= 0.95) {
                    bestChromosome = population[bestChromosomeIndex];
                    /*
                    System.out.println("Generation: " + generation);
                    System.out.println("Best Chromosome Number: " + (bestChromosomeIndex + 1));
                    System.out.println("Best Fitness: " + bestFitness);
                    //System.out.println("Beta0: " + bestChromosome.getBeta0());
                    //System.out.println("Beta1: " + bestChromosome.getBeta1());
                    System.out.println("Equation: " + bestChromosome.getBeta1() + "x + " + bestChromosome.getBeta0());
                    */
                    break;

                }

                population = newPopulation;
            }
            return new double[]{bestChromosome.getBeta0(), bestChromosome.getBeta1()};
        }

        public void calculateFitness(Chromosome[] population, double[] x, double[] y) {
            for (Chromosome chromosome : population) {
                double beta0 = chromosome.getBeta0();
                double beta1 = chromosome.getBeta1();

                Regression regression = new Regression(x, y, beta0, beta1);
                regression.fit();
                double regressionBeta0 = regression.getB0();
                double regressionBeta1 = regression.getB1();

                double tolerance = 0.1;
                double closenessBeta0 = 1 - Math.abs((regressionBeta0 - beta0) / regressionBeta0);
                double closenessBeta1 = 1 - Math.abs((regressionBeta1 - beta1) / regressionBeta1);

                double fitness = (closenessBeta0 + closenessBeta1) / 2;

                chromosome.setFitness(fitness);
            }
        }

        public void mutate(Chromosome chromosome) {
            Random random = new Random();

            if (random.nextDouble() < MUTATION_RATE) {
                chromosome.setBeta0(BETA0_MIN + (BETA0_MAX - BETA0_MIN) * random.nextDouble());
            }
            if (random.nextDouble() < MUTATION_RATE) {
                chromosome.setBeta1(BETA1_MIN + (BETA1_MAX - BETA1_MIN) * random.nextDouble());
            }
        }
    }
