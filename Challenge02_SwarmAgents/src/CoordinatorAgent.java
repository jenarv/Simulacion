import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CoordinatorAgent extends Agent {
    private double[] globalBestPosition;
    private double globalBestFitness = Double.POSITIVE_INFINITY;
    private int numDimensions;
    private int numParticles;
    private int maxIterations;
    private int currentIteration;

    protected void setup() {
        Object[] args = getArguments();
        numDimensions = (int) args[0];
        numParticles = (int) args[1];
        maxIterations = (int) args[2];
        globalBestPosition = new double[numDimensions];
        currentIteration = 0;

        addBehaviour(new CoordinatorBehaviour(this, 100));
    }

    private class CoordinatorBehaviour extends TickerBehaviour {
        public CoordinatorBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            if (currentIteration >= maxIterations) {
                System.out.println("Coordinator: Reached max iterations. Stopping.");
                try {
                    Thread.sleep(2000); // Wait for 2 seconds before printing results
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printBestEquationAndPredictions();
                myAgent.doDelete();
                return;
            }

            int receivedMessages = 0;
            for (int i = 0; i < numParticles; i++) {
                ACLMessage msg = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM), 500);
                if (msg != null) {
                    String[] content = msg.getContent().split(";");
                    double fitness = Double.parseDouble(content[0]);
                    double[] position = parseMessage(content[1]);

                    if (fitness < globalBestFitness) {
                        globalBestFitness = fitness;
                        globalBestPosition = position;
                    }

                    receivedMessages++;
                }
            }

            if (receivedMessages == numParticles) {
                ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                reply.setContent(arrayToString(globalBestPosition));
                for (int i = 0; i < numParticles; i++) {
                    reply.addReceiver(getAID("particle" + i));
                }
                send(reply);

                System.out.println("Coordinator: Global Best Fitness = " + globalBestFitness + " Global Best Position = " + arrayToString(globalBestPosition));
                currentIteration++;
            } else {
                System.out.println("Coordinator: Did not receive messages from all particles.");
            }
        }

        private double[] parseMessage(String content) {
            String[] parts = content.split(",");
            double[] position = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                position[i] = Double.parseDouble(parts[i]);
            }
            return position;
        }

        private String arrayToString(double[] array) {
            StringBuilder sb = new StringBuilder();
            for (double v : array) {
                sb.append(v).append(",");
            }
            return sb.toString().replaceAll(",$", "");
        }

        private void printBestEquationAndPredictions() {
            System.out.println("Best Regression Equation:");
            StringBuilder equation = new StringBuilder("Sales = " + globalBestPosition[0]);
            equation.append(" + ").append(globalBestPosition[1]).append(" * Year");
            equation.append(" + ").append(globalBestPosition[2]).append(" * Advertising");
            System.out.println(equation);

            // Print the betas
            System.out.println("Beta Coefficients:");
            System.out.println("Intercept (Beta 0): " + globalBestPosition[0]);
            System.out.println("Year (Beta 1): " + globalBestPosition[1]);
            System.out.println("Advertising (Beta 2): " + globalBestPosition[2]);

            // Making predictions for specified (Advertising, Year) pairs
            double[][] inputs = {
                    {23, 1},
                    {26, 2},
                    {43, 5},
                    {52, 7},
                    {58, 9}
            };
            double[] predictions = makePredictions(inputs);
            System.out.println("Predictions:");
            for (int i = 0; i < inputs.length; i++) {
                System.out.println("Advertising " + inputs[i][0] + ", Year " + inputs[i][1] + ": " + predictions[i]);
            }
        }

        private double[] makePredictions(double[][] inputs) {
            double[] predictions = new double[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                double prediction = globalBestPosition[0]; // Intercept
                prediction += globalBestPosition[1] * inputs[i][1]; // Year
                prediction += globalBestPosition[2] * inputs[i][0]; // Advertising
                predictions[i] = prediction;
            }
            return predictions;
        }
    }
}
