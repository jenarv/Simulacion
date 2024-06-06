public class Regression {
    private double[] x;
    private double[] y;
    private double slope;
    private double intercept;

    public Regression(double[] x, double[] y, double beta0, double beta1) {
        this.x = x;
        this.y = y;
        this.slope = beta1;
        this.intercept = beta0;
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    //calcular beta0 y beta1
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

        slope = (n * sumXY - sumX * sumY) / (n * sumXSquare - sumX * sumX);
        intercept = (sumY - slope * sumX) / n;
    }

    public double predict(double xValue) {
        return slope * xValue + intercept;
    }

}