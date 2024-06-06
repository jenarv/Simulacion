import java.util.Random;

public class PSO {
    private static final int NUM_PARTICLES = 30;
    private static final int NUM_DIMENSIONS = 2;
    private static final int MAX_ITERATIONS = 1000;
    private static final double INERTIA_WEIGHT = 0.5;
    private static final double COGNITIVE_COMPONENT = 1.5;
    private static final double SOCIAL_COMPONENT = 1.5;
    private static final double LOWER_BOUND = -1.0;
    private static final double UPPER_BOUND = 200.0;

    private Particle[] particles = new Particle[NUM_PARTICLES];
    private double[] globalBestPosition = new double[NUM_DIMENSIONS];
    private double globalBestFitness = 0.0;
    private Random random = new Random();
    private DataSet dataSet;
    private Regression regression;

    public PSO(DataSet dataSet) {
        this.dataSet = dataSet;

        double[] x = dataSet.getX();
        double[] y = dataSet.getY();
        regression = new Regression(x, y, 0, 0);
        regression.fit();

        for (int i = 0; i < NUM_PARTICLES; i++) {
            particles[i] = new Particle(NUM_DIMENSIONS);
            initializeParticle(particles[i]);
        }
    }

    //Inicializar la posición, velocidad y la mejor posición conocida de una sola particula
    private void initializeParticle(Particle particle) {
        //iterar entre las 2 dimensiones de la partícula
        for (int d = 0; d < NUM_DIMENSIONS; d++) {
            //generar una posición y velocidad aleatorias
            particle.position[d] = LOWER_BOUND + random.nextDouble() * (UPPER_BOUND - LOWER_BOUND);
            particle.velocity[d] = (random.nextDouble() * 2 - 1) * (UPPER_BOUND - LOWER_BOUND);
            //Inicialmente, la mejor posición de la partícula es la posición inicial
            particle.bestPosition[d] = particle.position[d];
        }
        //calcualr el fitness de la posición inicial de la particula
        particle.bestFitness = evaluate(particle.position);
    }

    //Evaluar el fitness de la particula comparando su predicción con la predicción del modelo de regresión
    private double evaluate(double[] position) {
        double[] x = dataSet.getX();
        double[] y = dataSet.getY();
        double beta0 = position[0];
        double beta1 = position[1];

        double fitness = 0.0;
        for (int i = 0; i < x.length; i++) {
            double psoPredictedY = beta0 + beta1 * x[i];
            double actualY = y[i];
            // Calculate the absolute difference between the predicted and actual values
            double absoluteDifference = Math.abs(psoPredictedY - actualY);
            // Calculate the ratio of closeness
            double closenessRatio = 1 - absoluteDifference / actualY;
            // Add the closeness ratio to the fitness
            fitness += closenessRatio;
        }

        // Divide the fitness sum by the number of data points to get the average
        fitness /= x.length;

        return fitness;
    }


    //Método para actualizar la velocidad de la partícula
    private void updateVelocity(Particle particle) {
        //itera entre las dimensiones de la partícula
        for (int d = 0; d < NUM_DIMENSIONS; d++) {
            double r1 = random.nextDouble();
            double r2 = random.nextDouble();

            //Fórmula para calcular la velocidad
            particle.velocity[d] = INERTIA_WEIGHT * particle.velocity[d] +
                    COGNITIVE_COMPONENT * r1 * (particle.bestPosition[d] - particle.position[d]) +
                    SOCIAL_COMPONENT * r2 * (globalBestPosition[d] - particle.position[d]);
        }
    }

    //Actualizar posición
    private void updatePosition(Particle particle) {
        //Iterar entre dimensiones de la partícula
        for (int d = 0; d < NUM_DIMENSIONS; d++) {
            //A la posición actual se le suma la velocidad
            particle.position[d] += particle.velocity[d];

            //Se asegura que la posición se mantenga entre los límites
            if (particle.position[d] < LOWER_BOUND) {
                particle.position[d] = LOWER_BOUND;
            } else if (particle.position[d] > UPPER_BOUND) {
                particle.position[d] = UPPER_BOUND;
            }
        }
    }

    public void run() {
        //Repetir proceso hasta el número máximo de iteraciones
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            //Para cada partícula en el Swarm
            for (Particle particle : particles) {
                //se evalúa el fitness
                double fitness = evaluate(particle.position);

                //Si el fintess actual es mayor al mejor fitness de la particula,
                // se actualiza el mejor fitness al fitness actual
                if (fitness > particle.bestFitness) {
                    particle.bestFitness = fitness;
                    System.arraycopy(particle.position, 0, particle.bestPosition, 0, NUM_DIMENSIONS);
                }

                //Si el fitness es mayor al mejor fitness global, se actualiza el
                // mejor fitness global al fitness actual
                if (fitness > globalBestFitness) {
                    globalBestFitness = fitness;
                    System.arraycopy(particle.position, 0, globalBestPosition, 0, NUM_DIMENSIONS);
                }
            }

            //Si el fitness global es igual a 1 se detienen las iteraciones,
            // ya que se hay llegado al resultado esperado
            if (globalBestFitness == 1.0) {
                System.out.println("Stopping at iteration " + iter + " with fitness 1.0");
                break;
            }

            //Se actualiza la velocidad y posición de la partícula actual
            for (Particle particle : particles) {
                updateVelocity(particle);
                updatePosition(particle);
            }

            System.out.println("Iteration " + iter + ": Best Fitness = " + globalBestFitness);
        }

        // Obtener los coeficientes de beta 0 y beta 1
        double beta0 = globalBestPosition[0];
        double beta1 = globalBestPosition[1];

        System.out.println("Beta0: " + beta0);
        System.out.println("Beta1: " + beta1);
        System.out.println("Equation: " + beta1 + "x + " + beta0);

        System.out.println("Fitness: " + globalBestFitness);
    }
}




