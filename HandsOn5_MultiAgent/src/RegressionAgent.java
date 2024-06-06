import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class RegressionAgent extends Agent {

    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        // Process the regression request
                        System.out.println("RegressionAgent received message: " + msg.getContent());
                        String[] parts = msg.getContent().split(",");
                        if (parts.length == 3) {
                            try {
                                // Parse client AID, file path, and sheet number
                                String clientAID = parts[0];
                                String filePath = parts[1];
                                int sheetNum = Integer.parseInt(parts[2]);

                                // Assuming DataSet class and its methods are defined correctly
                                DataSet dataSet = new DataSet(filePath, sheetNum);
                                double[] xValues = dataSet.getX();
                                double[] yValues = dataSet.getY();

                                // Create Regression object and fit the model
                                Regression regression = new Regression(xValues, yValues);
                                regression.fit();

                                // Get coefficients
                                double B1 = regression.getB1();
                                double B0 = regression.getB0();

                                // Send the result to the FinderAgent as a proposal
                                ACLMessage proposal = new ACLMessage(ACLMessage.PROPOSE);
                                proposal.addReceiver(new jade.core.AID("Finder", jade.core.AID.ISLOCALNAME));
                                proposal.setContent(B1 + "," + B0);
                                send(proposal);
                                System.out.println("RegressionAgent sent proposal to FinderAgent: " + B1 + "," + B0);

                            } catch (NumberFormatException e) {
                                System.out.println("Regression says: Invalid input format.");
                            }
                        } else {
                            System.out.println("Regression says: Invalid message format or not a regression request.");
                        }
                    }
                } else {
                    block();
                }
            }
        });

        // Register the service offered by the regression agent with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Solver");
        sd.setName("RegressionAgent");
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
}







