import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class FinderAgent extends Agent {

    double[] xValues;
    double[] yValues;
    private String clientAID; // Store client's AID

    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        // Store client's AID
                        clientAID = msg.getSender().getName();

                        System.out.println("FinderAgent received message: " + msg.getContent());
                        String[] parts = msg.getContent().split(",");
                        if (parts.length == 3) {
                            // Extract file path and sheet number from the message
                            String filePath = parts[1];
                            int sheetNum = Integer.parseInt(parts[2]);

                            DataSet dataSet = new DataSet(filePath, sheetNum);

                            xValues = dataSet.getX();
                            yValues = dataSet.getY();

                            // Search for agents offering the service (Solver)
                            DFAgentDescription template = new DFAgentDescription();
                            ServiceDescription sd = new ServiceDescription();
                            sd.setType("Solver");
                            template.addServices(sd);
                            try {
                                DFAgentDescription[] result = DFService.search(myAgent, template);
                                if (result.length > 0) {
                                    // Send the message to all solver agents
                                    for (DFAgentDescription solverAgent : result) {
                                        ACLMessage requestToSolver = new ACLMessage(ACLMessage.REQUEST);
                                        requestToSolver.addReceiver(solverAgent.getName());
                                        requestToSolver.setContent(msg.getContent()); // Send the original message
                                        send(requestToSolver);
                                        System.out.println("FinderAgent forwarded request to " + solverAgent.getName().getLocalName() + ": " + requestToSolver.getContent());
                                    }
                                } else {
                                    System.out.println("No agents found offering the Solver service.");
                                }
                            } catch (FIPAException fe) {
                                fe.printStackTrace();
                            }

                        } else {
                            // Invalid message format
                            System.out.println("Invalid message format.");
                        }

                    } else if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        handleProposal(msg);
                    }
                } else {
                    block();
                }
            }
        });

        // Register the service offered by the finder agent with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Finder");
        sd.setName("FinderAgent");
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


    // Define bestFitness and bestProposal as instance variables in the FinderAgent class
    private double bestFitness = 0.0;
    private ACLMessage bestProposal = null;

    private void handleProposal(ACLMessage proposal) {
        System.out.println("FinderAgent received proposal from " + proposal.getSender().getLocalName() + ": " + proposal.getContent());
        String[] parts = proposal.getContent().split(",");
        if (parts.length == 2) {
            double beta1 = Double.parseDouble(parts[0]);
            double beta0 = Double.parseDouble(parts[1]);

            // Check if the dataset is initialized and not empty
            if (xValues != null && yValues != null && xValues.length == yValues.length && xValues.length > 0) {
                // Initialize variables to keep track of fitness, predicted value, and actual value
                double totalFitness = 0.0;
                StringBuilder fitnessLog = new StringBuilder("Fitness for each data point:");

                for (int i = 0; i < xValues.length; i++) {

                    // Make prediction using the beta coefficients
                    double predictedValue = beta0 + beta1 * xValues[i];

                    // Compare the predicted value with the actual value in the dataset
                    double actualValue = yValues[i];

                    // Calculate the difference between the predicted and actual values
                    double difference = Math.abs(predictedValue - actualValue);

                    // Calculate fitness as the percentage of how close the predicted value is to the actual value
                    double fitness = 1.0 - (difference / actualValue);

                    // Add fitness to the total
                    totalFitness += fitness;

                    // Append fitness for each data point to the log
                    fitnessLog.append("\nData Point ").append(i + 1).append(": Predicted Value = ").append(predictedValue)
                            .append(", Actual Value = ").append(actualValue).append(", Fitness = ").append(fitness)
                            .append(", Difference = ").append(difference);
                }

                // Calculate average fitness
                double averageFitness = totalFitness / xValues.length;

                // If the current fitness is better than the previous best fitness, update the best fitness and proposal
                if (averageFitness > bestFitness) {
                    bestFitness = averageFitness;
                    bestProposal = proposal;

                    // Print the best fitness and the proposal received
                    System.out.println("FinderAgent received proposal with better fitness from " + proposal.getSender().getLocalName() + " : " + proposal.getContent() + ", Best Fitness: " + bestFitness);

                    // Send the best proposal to the client
                    ACLMessage replyToClient = new ACLMessage(ACLMessage.INFORM);
                    replyToClient.addReceiver(new jade.core.AID("Client", jade.core.AID.ISLOCALNAME)); // Send to stored client's AID
                    replyToClient.setContent(bestProposal.getContent());
                    send(replyToClient);
                    System.out.println("FinderAgent sent best proposal to Client: " + replyToClient.getContent());
                }
            } else {
                System.out.println("Dataset is not initialized or empty."); // Debugging line
            }
        } else {
            // Invalid proposal format
            System.out.println("Finder says: Invalid proposal format: " + proposal.getContent());
        }
    }
}


