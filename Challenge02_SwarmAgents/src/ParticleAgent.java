import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

public class ParticleAgent extends Agent {
    private double[] position;
    private double[] velocity;
    private double[] bestPosition;
    private double bestFitness;
    private Random random = new Random();
    private DataSet dataSet;
    private static final double INERTIA_WEIGHT = 0.5;
    private static final double COGNITIVE_COMPONENT = 1.5;
    private static final double SOCIAL_COMPONENT = 1.5;
    private static final double LOWER_BOUND = -100.0;
    private static final double UPPER_BOUND = 400.0;

    protected void setup() {
        Object[] args = getArguments();
        dataSet = (DataSet) args[0];
        int numDimensions = dataSet.getX()[0].length + 1; 

        position = new double[numDimensions];
        velocity = new double[numDimensions];
        bestPosition = new double[numDimensions];
        bestFitness = Double.POSITIVE_INFINITY;

        initializeParticle();

        addBehaviour(new ParticleBehaviour(this, 100));
    }

    private void initializeParticle() {
        for (int d = 0; d < position.length; d++) {
            position[d] = LOWER_BOUND + random.nextDouble() * (UPPER_BOUND - LOWER_BOUND);
            velocity[d] = (random.nextDouble() * 2 - 1) * (UPPER_BOUND - LOWER_BOUND);
            bestPosition[d] = position[d];
        }
        bestFitness = evaluate(position);
        System.arraycopy(position, 0, bestPosition, 0, position.length);
    }

    private double evaluate(double[] position) {
        double[][] x = dataSet.getX();
        double[] y = dataSet.getY();

        double sumSquaredErrors = 0.0;
        for (int i = 0; i < x.length; i++) {
            double psoPredictedY = position[0];
            for (int j = 0; j < x[i].length; j++) {
                psoPredictedY += position[j + 1] * x[i][j];
            }
            double actualY = y[i];
            double error = psoPredictedY - actualY;
            sumSquaredErrors += error * error;
        }

        return sumSquaredErrors;
    }

    private void updateVelocity(double[] globalBestPosition) {
        for (int d = 0; d < velocity.length; d++) {
            double r1 = random.nextDouble();
            double r2 = random.nextDouble();
            velocity[d] = INERTIA_WEIGHT * velocity[d]
                    + COGNITIVE_COMPONENT * r1 * (bestPosition[d] - position[d])
                    + SOCIAL_COMPONENT * r2 * (globalBestPosition[d] - position[d]);
        }
    }

    private void updatePosition() {
        for (int d = 0; d < position.length; d++) {
            position[d] += velocity[d];
            if (position[d] < LOWER_BOUND) {
                position[d] = LOWER_BOUND;
            } else if (position[d] > UPPER_BOUND) {
                position[d] = UPPER_BOUND;
            }
        }
    }

    private class ParticleBehaviour extends TickerBehaviour {
        public ParticleBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            double fitness = evaluate(position);

            if (fitness < bestFitness) { 
                bestFitness = fitness;
                System.arraycopy(position, 0, bestPosition, 0, position.length);
            }

            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(getAID("coordinator"));
            msg.setContent(bestFitness + ";" + arrayToString(bestPosition));
            send(msg);
            System.out.println(getLocalName() + ": Sent fitness = " + bestFitness + " and best position = " + arrayToString(bestPosition));

            ACLMessage reply = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (reply != null) {
                double[] globalBestPosition = parseMessage(reply);
                updateVelocity(globalBestPosition);
                updatePosition();
                System.out.println(getLocalName() + ": Received global best position = " + arrayToString(globalBestPosition));
            }

            System.out.println(getLocalName() + ": Position = " + arrayToString(position) + " Best Fitness = " + bestFitness);
        }

        private double[] parseMessage(ACLMessage msg) {
            String[] parts = msg.getContent().split(",");
            double[] globalBestPosition = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                globalBestPosition[i] = Double.parseDouble(parts[i]);
            }
            return globalBestPosition;
        }

        private String arrayToString(double[] array) {
            StringBuilder sb = new StringBuilder();
            for (double v : array) {
                sb.append(v).append(",");
            }
            return sb.toString().replaceAll(",$", "");
        }
    }
}




