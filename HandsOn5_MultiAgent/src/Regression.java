public class Regression {
    private double[] x;
    private double[] y;
    private double B1;
    private double B0;

    public Regression(double[] x, double[] y) {
        this.x = x;
        this.y = y;
    }

    public Regression(double[] x, double[] y, double beta0, double beta1) {
        this.x = x;
        this.y = y;
        this.B1 = beta1;
        this.B0 = beta0;
    }

    public double getB1() {
        return B1;
    }

    public double getB0() {
        return B0;
    }

    public void fit() {
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXSquare = 0;
        int n = x.length;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumXSquare += x[i] * x[i];
        }

        B1 = (n * sumXY - sumX * sumY) / (n * sumXSquare - sumX * sumX);
        B0 = (sumY - B1 * sumX) / n;
    }

    public double predict(double xValue) {
        return B1 * xValue + B0;
    }

    public double getRSquared() {
        double yMean = 0;
        for (double yi : y) {
            yMean += yi;
        }
        yMean /= y.length;

        double ssTot = 0;
        double ssRes = 0;
        for (int i = 0; i < y.length; i++) {
            double yPredicted = predict(x[i]);
            ssTot += (y[i] - yMean) * (y[i] - yMean);
            ssRes += (y[i] - yPredicted) * (y[i] - yPredicted);
        }

        return 1 - (ssRes / ssTot);
    }
}