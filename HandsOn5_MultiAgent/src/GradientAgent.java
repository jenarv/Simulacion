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
                        
                        System.out.println("GradientAgent received message: " + msg.getContent());
                        String[] parts = msg.getContent().split(",");
                        if (parts.length == 3) {
                            try {
                                
                                String clientAID = parts[0];
                                String filePath = parts[1];
                                int sheetNum = Integer.parseInt(parts[2]);

                                
                                DataSet dataSet = new DataSet(filePath, sheetNum);

                                
                                GradientDescent gradientDescent = new GradientDescent(dataSet);
                                gradientDescent.train(60000);

                                
                                double b0 = gradientDescent.getBeta0();
                                double b1 = gradientDescent.getBeta1();

                                
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

    
    protected void takeDown() {
        
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

       
        private double calculateBeta(int index) {
            double sum = 0;
            double n = dataSet.getY().length;

           
            for (int i = 0; i < n; i++) {
                
                if (index == 0) {
                    sum += (dataSet.getY()[i] - (beta0 + (beta1 * dataSet.getX()[i])));
                } else {
                    sum += (dataSet.getX()[i] * (dataSet.getY()[i] - (beta0 + (beta1 * dataSet.getX()[i]))));
                }
            }

            
            return (-2 / n) * sum;
        }

        /
        private double calculateMSE() {
            double sum = 0;
            double n = dataSet.getY().length;

            
            for (int i = 0; i < n; i++) {
                double prediction = beta0 + (beta1 * dataSet.getX()[i]);
                sum += Math.pow(dataSet.getY()[i] - prediction, 2);
            }

            
            return sum / n;
        }

        
        public void train(int epoch) {
            int i = 0;
            
            while (i < epoch) {
                
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


