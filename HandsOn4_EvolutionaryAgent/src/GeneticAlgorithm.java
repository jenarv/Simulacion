import java.util.Random;

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
        //Crear arreglo para guardar la población de cromosomas
        Chromosome[] population = new Chromosome[populationSize];
        Random random = new Random();

        //Inicializar la población con números aleatorios dentro del rango
        for (int i = 0; i < populationSize; i++) {
            double beta0 = BETA0_MIN + (BETA0_MAX - BETA0_MIN) * random.nextDouble();
            double beta1 = BETA1_MIN + (BETA1_MAX - BETA1_MIN) * random.nextDouble();
            population[i] = new Chromosome(beta0, beta1);
        }

        return population;
    }

    /*
    * Este método itera sobre toda la población de cromosomas
    * Para cada cromosoma en la población, decide si debe hacer el crossover basándose en el CROSSOVER_RATE.
    * Si se decide realizar el cruce, entonces se selecciona un padre y se manda a llamar
    * el otro método que hace el cruce entre los cromosomas
    * Los hijos resultantes se ponen en una nueva población.
    * */
    public void crossover(Chromosome[] population, Chromosome[] newPopulation) {
        Random random = new Random();


        for (int i = 0; i < population.length; i++) { //Itera sobre cada individuo en la población actual
            Chromosome individual = population[i]; //Seleccionar individuo
            if (random.nextDouble() < CROSSOVER_RATE) { //Comprobar si se necesita realizar la operación
                Chromosome secondParent = selectParent(population);
                Chromosome[] offspring = crossover(individual, secondParent); //Se realiza el crossover
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


    //Este método hace directamente el cruce entre 2 cromosomas
    public Chromosome[] crossover(Chromosome parent1, Chromosome parent2) {
        Random random = new Random();

        //Se elige un punto de cruce aleatorio
        double beta0_crossover_point = random.nextInt(CHROMOSOME_LENGTH);
        double beta1_crossover_point = random.nextInt(CHROMOSOME_LENGTH);

        //Obtener valores de beta0 y beta1 de los padres
        double child1_beta0 = parent1.getBeta0();
        double child1_beta1 = parent2.getBeta1();
        double child2_beta0 = parent2.getBeta0();
        double child2_beta1 = parent1.getBeta1();

        //Se verifica si se realiza el cruce para beta0
        if (random.nextDouble() < CROSSOVER_RATE) {
            double temp = child1_beta0;
            child1_beta0 = child2_beta0;
            child2_beta0 = temp;
        }

        //Se verifica si se realiza el cruce para beta1
        if (random.nextDouble() < CROSSOVER_RATE) {
            double temp = child1_beta1;
            child1_beta1 = child2_beta1;
            child2_beta1 = temp;
        }

        return new Chromosome[]{new Chromosome(child1_beta0, child1_beta1), new Chromosome(child2_beta0, child2_beta1)};
    }

    public Chromosome selectParent(Chromosome[] population) {
        double totalFitness = 0.0;

        //Se calcula la suma total de fitness de todos los cromosomas de la población
        for (Chromosome chromosome : population) {
            totalFitness += chromosome.getFitness();
        }

        double rand = Math.random() * totalFitness;

        double runningSum = 0.0;
        //Se suma el fitness del cromosoma actual al fitness acumulado
        for (Chromosome chromosome : population) {
            runningSum += chromosome.getFitness();
            // Si la suma acumulada supera el valor aleatorio generado,
            // se selecciona el cromosoma actual como padre y se devuelve.
            if (runningSum >= rand) {
                return chromosome;
            }
        }

        return null;
    }

    public void runGeneticAlgorithm(double[] x, double[] y, int maxGenerations) {
        // Inicializa la población de cromosomas
        Chromosome[] population = initializePopulation();

        // Itera a través de las generaciones
        for (int generation = 1; generation <= maxGenerations; generation++) {
            // Calcula el fitness de cada cromosoma en la población
            calculateFitness(population, x, y);

            // Se crea una nueva población cruzando los cromosomas de la población actual
            Chromosome[] newPopulation = new Chromosome[populationSize];
            // Se seleccionan los padres y se realiza el cruce de población
            crossover(population, newPopulation);

            // Se mutan los cromosomas de la nueva población
            for (Chromosome chromosome : newPopulation) {
                mutate(chromosome);
            }

            // Busca el cromosoma con el mejor fitness en la población actual
            double bestFitness = -1;
            int bestChromosomeIndex = -1;
            for (int i = 0; i < population.length; i++) {
                if (population[i].getFitness() >= 0.95) {
                    bestChromosomeIndex = i;
                    bestFitness = population[i].getFitness();
                    break;
                }
            }

            // Si se encuentra el mejor cromosoma
            if (bestFitness >= 0.98) {
                System.out.println("Generation: " + generation);
                System.out.println("Best Chromosome Number: " + (bestChromosomeIndex + 1));
                System.out.println("Best Fitness: " + bestFitness);
                System.out.println("Beta0: " + population[bestChromosomeIndex].getBeta0());
                System.out.println("Beta1: " + population[bestChromosomeIndex].getBeta1());
                System.out.println("Equation: " + population[bestChromosomeIndex].getBeta1() + "x + " + population[bestChromosomeIndex].getBeta0());
                break;
            }

            population = newPopulation;
        }
    }



    public void calculateFitness(Chromosome[] population, double[] x, double[] y) {
        //Itera sobre cada cromosoma en la población
        for (Chromosome chromosome : population) {
            //Obtener los valores de beta0 y beta1 del cromosoma
            double beta0 = chromosome.getBeta0();
            double beta1 = chromosome.getBeta1();

            //Obtener los valores ideales de regresión líneal
            Regression regression = new Regression(x, y, beta0, beta1);
            regression.fit();
            double regressionBeta0 = regression.getIntercept();
            double regressionBeta1 = regression.getSlope();

            double tolerance = 0.1;

            //Calcular la cercanía entre los valores de beta0 y beta1 del cromosoma
            //y los valores ajustados de la regresión
            double closenessBeta0 = 1 - Math.abs((regressionBeta0 - beta0) / regressionBeta0);
            double closenessBeta1 = 1 - Math.abs((regressionBeta1 - beta1) / regressionBeta1);

            //Calcular el fitness del cromosoma como el promedio
            double fitness = (closenessBeta0 + closenessBeta1) / 2;

            chromosome.setFitness(fitness);
        }
    }

    public void mutate(Chromosome chromosome) {
        Random random = new Random();

        //Verificar si se debe aplicar mutación
        if (random.nextDouble() < MUTATION_RATE) {
            //Si la condición se cumple, genera valor aleatorio dentro del rango permitido
            //para beta0 y se asigna al cromosoma mutado
            chromosome.setBeta0(BETA0_MIN + (BETA0_MAX - BETA0_MIN) * random.nextDouble());
        }
        if (random.nextDouble() < MUTATION_RATE) {
            chromosome.setBeta1(BETA1_MIN + (BETA1_MAX - BETA1_MIN) * random.nextDouble());
        }
    }
}

