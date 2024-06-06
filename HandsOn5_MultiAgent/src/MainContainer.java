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
            // Create the main container for the ClientAgent
            Profile mainProfile = new ProfileImpl();
            mainProfile.setParameter(Profile.MAIN_HOST, "localhost");
            mainProfile.setParameter(Profile.GUI, "true");
            AgentContainer mainContainer = rt.createMainContainer(mainProfile);

            // Create a centralized SnifferGUI to display all interactions
            Object[] snifferGuiArgs = new Object[]{"-gui"};
            AgentController snifferGuiController = mainContainer.createNewAgent("SnifferGUI", Sniffer.class.getName(), snifferGuiArgs);
            snifferGuiController.start();

            // Create other agents in separate containers with their own sniffers
            String[] agentNames = {"Finder", "Regression", "Gradient", "Genetic", "Swarm"};
            for (String agentName : agentNames) {
                createAgentWithSniffer(rt, agentName);
            }

            // Add a delay before creating the ClientAgent
            Thread.sleep(30000); // 30 seconds delay
            //Thread.sleep(2000); // 2 seconds delay

            // Create and start the ClientAgent in the main container
            AgentController clientController = mainContainer.createNewAgent("Client", "ClientAgent", null);
            clientController.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createAgentWithSniffer(Runtime rt, String agentName) throws StaleProxyException {
        // Create profile for the new container
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");

        // Create new container for the agent
        AgentContainer container = rt.createAgentContainer(p);

        // Create and start the agent
        AgentController agentController = container.createNewAgent(agentName, agentName + "Agent", null);
        agentController.start();

        // Create and start the sniffer agent in the same container
        AgentController snifferController = container.createNewAgent(agentName + "Sniffer", Sniffer.class.getName(), new Object[]{"-gui"});
        snifferController.start();
    }
}
