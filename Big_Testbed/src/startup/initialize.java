/**
 * 
 */
package startup;

import jade.Boot;
import jade.core.Agent;

/**
 * @author garyz
 *
 */
public class initialize extends Agent {

	/**
	 * 
	 */
	public initialize() {
		// TODO Auto-generated constructor stub
	}
	 /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	
        String startAgents = "helloAgent:agents.HelloWorldAgent;rfid3Agent:Tags.rfid3;";

		// Start JADE with the above defined agents agents.
		String[] parameters = new String[] { "-gui", // Starts the agent management tools
				// "-container", //Uncomment if you want to run a remote container on a different host
				//"-host", "localhost", // Replace this with your local IP.
				//"-port", "10207", // The port number for inter-platform communication
				startAgents};
		
		
		Boot.main(parameters);    
    }
}
