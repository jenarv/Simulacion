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
    private String clientAID; 

    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        
                        clientAID = msg.getSender().getName();

                        System.out.println("FinderAgent received message: " + msg.getContent());
                        String[] parts = msg.getContent().split(",");
                        if (parts.length == 3) {
                            
                            String filePath = parts[1];
                            int sheetNum = Integer.parseInt(parts[2]);

                            DataSet dataSet = new DataSet(filePath, sheetNum);

                            xValues = dataSet.getX();
                            yValues = dataSet.getY();

                            
                            DFAgentDescription template = new DFAgentDescription();
                            ServiceDescription sd = new ServiceDescription();
                            sd.setType("Solver");
                            template.addServices(sd);
                            try {
                                DFAgentDescription[] result = DFService.search(myAgent, template);
                                if (result.length > 0) {
                                    
                                    for (DFAgentDescription solverAgent : result) {
                                        ACLMessage requestToSolver = new ACLMessage(ACLMessage.REQUEST);
                                        requestToSolver.addReceiver(solverAgent.getName());
                                        requestToSolver.setContent(msg.getContent()); 
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

    
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }


    private double bestFitness = 0.0;
    private ACLMessage bestProposal = null;

    private void handleProposal(ACLMessage proposal) {
        System.out.println("FinderAgent received proposal from " + proposal.getSender().getLocalName() + ": " + proposal.getContent());
        String[] parts = proposal.getContent().split(",");
        if (parts.length == 2) {
            double beta1 = Double.parseDouble(parts[0]);
            double beta0 = Double.parseDouble(parts[1]);

           
            if (xValues != null && yValues != null && xValues.length == yValues.length && xValues.length > 0) {
                
                double totalFitness = 0.0;
                StringBuilder fitnessLog = new StringBuilder("Fitness for each data point:");

                for (int i = 0; i < xValues.length; i++) {

                    
                    double predictedValue = beta0 + beta1 * xValues[i];
                    double actualValue = yValues[i];
                    double difference = Math.abs(predictedValue - actualValue);
                    double fitness = 1.0 - (difference / actualValue);
                    totalFitness += fitness;
                    fitnessLog.append("\nData Point ").append(i + 1).append(": Predicted Value = ").append(predictedValue)
                            .append(", Actual Value = ").append(actualValue).append(", Fitness = ").append(fitness)
                            .append(", Difference = ").append(difference);
                }

                double averageFitness = totalFitness / xValues.length;

                if (averageFitness > bestFitness) {
                    bestFitness = averageFitness;
                    bestProposal = proposal;

                 
                    System.out.println("FinderAgent received proposal with better fitness from " + proposal.getSender().getLocalName() + " : " + proposal.getContent() + ", Best Fitness: " + bestFitness);

                    
                    ACLMessage replyToClient = new ACLMessage(ACLMessage.INFORM);
                    replyToClient.addReceiver(new jade.core.AID("Client", jade.core.AID.ISLOCALNAME)); // Send to stored client's AID
                    replyToClient.setContent(bestProposal.getContent());
                    send(replyToClient);
                    System.out.println("FinderAgent sent best proposal to Client: " + replyToClient.getContent());
                }
            } else {
                System.out.println("Dataset is not initialized or empty."); 
            }
        } else {
            System.out.println("Finder says: Invalid proposal format: " + proposal.getContent());
        }
    }
}


