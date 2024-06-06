import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class SwarmAgent extends Agent {

    private static final int NUM_DIMENSIONS = 2;

    protected void setup() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println("SwarmAgent received message: " + msg.getContent());
                    String[] parts = msg.getContent().split(",");
                    if (parts.length == 3) {
                        String clientAID = parts[0];
                        String filePath = parts[1];
                        int sheetNum = Integer.parseInt(parts[2]);


                        DataSet dataSet = new DataSet(filePath, sheetNum);
                        PSO pso = new PSO(dataSet);
                        double[] coefficients = pso.run();
                        double b0 = coefficients[0];
                        double b1 = coefficients[1];

                        ACLMessage proposal = new ACLMessage(ACLMessage.PROPOSE);
                        proposal.addReceiver(new jade.core.AID("Finder", jade.core.AID.ISLOCALNAME));
                        String result = b1 + "," + b0;
                        proposal.setContent(result);
                        send(proposal);
                        System.out.println("SwarmAgent sent proposal to FinderAgent: " + result);
                    } else {
                        System.out.println("Swarm says: Invalid message format.");
                    }
                } else {
                    block();
                }
            }
        });

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Solver");
        sd.setName("SwarmAgent");
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
}
