import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

public class SwarmAgent extends Agent {
    private static final String FILE_PATH = "D:\\Universidad\\5SEMESTRE\\Simulacion\\RegressionDatasets\\benetton.xlsx";
    private int SHEET_NUM = 0;
    private DataSet dataSet;

    protected void setup() {
        System.out.println("Agent " + getLocalName() + " started.");

        dataSet = new DataSet(FILE_PATH, SHEET_NUM);

        addBehaviour(new PSOAlgorithmBehavior());
    }

    private class PSOAlgorithmBehavior extends SimpleBehaviour {
        @Override
        public void action() {
            double[] x = dataSet.getX();
            double[] y = dataSet.getY();

            PSO pso = new PSO(dataSet);
            pso.run();
        }

        @Override
        public boolean done() {
            return true;
        }
    }

    protected void takeDown() {
        System.out.println("Agent " + getLocalName() + " is being deleted.");
    }
}




