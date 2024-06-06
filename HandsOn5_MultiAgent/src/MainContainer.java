import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jade.tools.sniffer.Sniffer;

public class MainContainer {
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();

        try {
            
            Profile mainProfile = new ProfileImpl();
            mainProfile.setParameter(Profile.MAIN_HOST, "localhost");
            mainProfile.setParameter(Profile.GUI, "true");
            AgentContainer mainContainer = rt.createMainContainer(mainProfile);

           
            Object[] snifferGuiArgs = new Object[]{"-gui"};
            AgentController snifferGuiController = mainContainer.createNewAgent("SnifferGUI", Sniffer.class.getName(), snifferGuiArgs);
            snifferGuiController.start();

            
            String[] agentNames = {"Finder", "Regression", "Gradient", "Genetic", "Swarm"};
            for (String agentName : agentNames) {
                createAgentWithSniffer(rt, agentName);
            }

            
            Thread.sleep(30000); // 30 seconds delay
            //Thread.sleep(2000); // 2 seconds delay

            
            AgentController clientController = mainContainer.createNewAgent("Client", "ClientAgent", null);
            clientController.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createAgentWithSniffer(Runtime rt, String agentName) throws StaleProxyException {
        
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");

        
        AgentContainer container = rt.createAgentContainer(p);

       
        AgentController agentController = container.createNewAgent(agentName, agentName + "Agent", null);
        agentController.start();

        AgentController snifferController = container.createNewAgent(agentName + "Sniffer", Sniffer.class.getName(), new Object[]{"-gui"});
        snifferController.start();
    }
}
