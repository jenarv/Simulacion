import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class ClientAgent extends Agent {

    protected void setup() {
        addBehaviour(new OneShotBehaviour(this) {
            public void action() {
                
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                
                sd.setType("Finder");
                template.addServices(sd);
                try {
                    
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {

                        String FILE_PATH = "D:\\Universidad\\5SEMESTRE\\Simulacion\\RegressionDatasets\\handsOn2_datasets.xlsx";
                        int SHEET_NUM = 3;

                        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                        msg.addReceiver(result[0].getName());
                        msg.setContent(getAID().getName() + "," + FILE_PATH + "," + SHEET_NUM);
                        send(msg);
                        System.out.println("ClientAgent sent file path and sheet number: " + FILE_PATH + ", " + SHEET_NUM);
                    } else {
                        System.out.println("No agents found offering the Finder service.");
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM) {

                        System.out.println("ClientAgent received regression result: " + msg.getContent());
                        String[] parts = msg.getContent().split(",");
                        if (parts.length == 2) {
                            double B1 = Double.parseDouble(parts[0].trim());
                            double B0 = Double.parseDouble(parts[1].trim());

                            System.out.print("Beta 0: " + B0);
                            System.out.print("\nBeta 1: " + B1);

                            String equation = "y = " + B1 + "x + " + B0;
                            System.out.println("\nEquation: " + equation);


                            double[] xValues = {23, 43, 58, 60, 75};
                            for (double x : xValues) {
                                double prediction = B1 * x + B0;
                                System.out.println("Prediction for x = " + x + " is y = " + prediction);
                            }
                        } else {
                            System.out.println("Invalid regression result format.");
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }
}
