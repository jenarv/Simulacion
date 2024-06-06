import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
public class SwarmAgent extends Agent {
    private static final String FILE_PATH = "D:\\Universidad\\5SEMESTRE\\Simulacion\\RegressionDatasets\\benetton_mlr.xlsx";
    private int SHEET_NUM = 0;
    private static final int MAX_ITERATIONS = 100;
    private DataSet dataSet;

    protected void setup() {
        System.out.println("Agent " + getLocalName() + " started.");

        dataSet = new DataSet(FILE_PATH, SHEET_NUM);

        int numParticles = 100;
        int numDimensions = dataSet.getX()[0].length + 1; 

        try {
            AgentContainer container = getContainerController();

            // Create Coordinator Agent
            Object[] coordArgs = {numDimensions, numParticles, MAX_ITERATIONS};
            AgentController coordinator = container.createNewAgent("coordinator", "CoordinatorAgent", coordArgs);
            coordinator.start();

            // Create Particle Agents
            for (int i = 0; i < numParticles; i++) {
                Object[] particleArgs = {dataSet};
                AgentController particle = container.createNewAgent("particle" + i, "ParticleAgent", particleArgs);
                particle.start();
            }

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    protected void takeDown() {
        System.out.println("Agent " + getLocalName() + " is being deleted.");
    }
}










