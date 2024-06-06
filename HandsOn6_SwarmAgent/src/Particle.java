public class Particle {
    double[] position;
    double[] velocity;
    double[] bestPosition;
    double bestFitness;

    public Particle(int dimensions) {
        position = new double[dimensions];
        velocity = new double[dimensions];
        bestPosition = new double[dimensions];
        bestFitness = Double.MAX_VALUE;
    }
}


