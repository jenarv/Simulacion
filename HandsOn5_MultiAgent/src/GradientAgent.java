import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class GradientAgent extends Agent {

    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        // Process the gradient descent request
                        System.out.println("GradientAgent received message: " + msg.getContent());
                        String[] parts = msg.getContent().split(",");
                        if (parts.length == 3) {
                            try {
                                // Parse client AID, file path, and sheet number
                                String clientAID = parts[0];
                                String filePath = parts[1];
                                int sheetNum = Integer.parseInt(parts[2]);

                                // Load dataset
                                DataSet dataSet = new DataSet(filePath, sheetNum);

                                // Create GradientDescent object and train the model
                                GradientDescent gradientDescent = new GradientDescent(dataSet);
                                gradientDescent.train(60000);

                                // Get the values of B0 and B1
                                double b0 = gradientDescent.getBeta0();
                                double b1 = gradientDescent.getBeta1();

                                // Send the result to the FinderAgent as a proposal
                                ACLMessage proposal = new ACLMessage(ACLMessage.PROPOSE);
                                proposal.addReceiver(new jade.core.AID("Finder", jade.core.AID.ISLOCALNAME));
                                String result = b1 + "," + b0;
                                proposal.setContent(result);
                                send(proposal);
                                System.out.println("GradientAgent sent proposal to FinderAgent: " + result);


                            } catch (NumberFormatException e) {
                                System.out.println("Gradient says: Invalid input format.");
                            }
                        } else {
                            System.out.println("Gradient says: Invalid message format.");
                        }
                    }
                } else {
                    block();
                }
            }
        });

        // Register the service offered by the gradient agent with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Solver");
        sd.setName("GradientAgent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    // Clean-up operations
    protected void takeDown() {
        // Deregister the service upon termination
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public class GradientDescent {
        private double beta0;
        private double beta1;
        private DataSet dataSet;
        private double learningRate;

        public GradientDescent(DataSet data) {
            dataSet = data;
            beta0 = 0;
            beta1 = 0;
            learningRate = 0.0005;
        }

        // Method to calculate the change in beta0 or beta1
        private double calculateBeta(int index) {
            double sum = 0;
            double n = dataSet.getY().length;

            // Iterate over all points in the dataset
            for (int i = 0; i < n; i++) {
                // Calculate the difference between the actual value and the predicted value by the model
                if (index == 0) {
                    sum += (dataSet.getY()[i] - (beta0 + (beta1 * dataSet.getX()[i])));
                } else {
                    sum += (dataSet.getX()[i] * (dataSet.getY()[i] - (beta0 + (beta1 * dataSet.getX()[i]))));
                }
            }

            // Return the change in the coefficient
            return (-2 / n) * sum;
        }

        // Method to calculate the mean squared error
        private double calculateMSE() {
            double sum = 0;
            double n = dataSet.getY().length;

            // Iterate over all points in the dataset
            for (int i = 0; i < n; i++) {
                double prediction = beta0 + (beta1 * dataSet.getX()[i]);
                sum += Math.pow(dataSet.getY()[i] - prediction, 2);
            }

            // Return the mean squared error
            return sum / n;
        }

        // Method to train the model for a specified number of epochs
        public void train(int epoch) {
            int i = 0;
            // Iterate until the specified number of epochs is reached
            while (i < epoch) {
                // Calculate the new values of beta0 and beta1 using gradient descent
                double newB0 = beta0 - (learningRate * calculateBeta(0));
                double newB1 = beta1 - (learningRate * calculateBeta(1));
                beta0 = newB0;
                beta1 = newB1;
                double mse = calculateMSE();
                // System.out.println("Epoch " + (i+1) + ", MSE: " + mse);
                i++;
            }
            //System.out.println("Trained");
        }

        public double getBeta0() {
            return beta0;
        }

        public double getBeta1() {
            return beta1;
        }
    }

}


