public class Chromosome {
    private double beta0;
    private double beta1;
    private double fitness;

    public Chromosome(double beta0, double beta1) {
        this.beta0 = beta0;
        this.beta1 = beta1;
        this.fitness = 0.0;
    }

    public double getBeta0() {
        return beta0;
    }

    public void setBeta0(double beta0) {
        this.beta0 = beta0;
    }

    public double getBeta1() {
        return beta1;
    }

    public void setBeta1(double beta1) {
        this.beta1 = beta1;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}

