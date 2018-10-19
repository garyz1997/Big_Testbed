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
	 * inittest
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

//States 
ProductState conv1 = new ProductState("Conveyor 1", null, new PhysicalProperty(new Point(5,0)));
ProductState cnc1 = new ProductState("CNC 1", null, new PhysicalProperty(new Point(6,1)));
ProductState cnc1_p0 = new ProductState("CNC 1-Machined 0", 0, new PhysicalProperty(new Point(6,1)));
ProductState cnc1_p1 = new ProductState("CNC 1-Machined 1", 1, new PhysicalProperty(new Point(6,1)));
ProductState cnc1_p2 = new ProductState("CNC 1-Machined 2", 2, new PhysicalProperty(new Point(6,1)));
ProductState cnc2 = new ProductState("CNC 2", null, new PhysicalProperty(new Point(5,2)));
ProductState cnc2_p1 = new ProductState("CNC 2-Machined 1", 1, new PhysicalProperty(new Point(5,2)));
ProductState cnc2_p2 = new ProductState("CNC 2-Machined 2", 2, new PhysicalProperty(new Point(5,2)));
ProductState cnc2_p3 = new ProductState("CNC 2-Machined 3", 3, new PhysicalProperty(new Point(5,2)));
ProductState conv2 = new ProductState("Conveyor 2", null, new PhysicalProperty(new Point(1,0)));
ProductState cnc3 = new ProductState("CNC 3", null, new PhysicalProperty(new Point(1,2)));
ProductState cnc3_p2 = new ProductState("CNC 3-Machined 2", 2, new PhysicalProperty(new Point(1,2)));
ProductState cnc3_p3 = new ProductState("CNC 3-Machined 3", 3, new PhysicalProperty(new Point(1,2)));
ProductState cnc3_p4 = new ProductState("CNC 3-Machined 4", 4, new PhysicalProperty(new Point(1,2)));
ProductState cnc4 = new ProductState("CNC 4", null, new PhysicalProperty(new Point(0,1)));
ProductState cnc4_p3 = new ProductState("CNC 4-Machined 3", 3, new PhysicalProperty(new Point(0,1)));
ProductState cnc4_p4 = new ProductState("CNC 4-Machined 4", 4, new PhysicalProperty(new Point(0,1)));
ProductState cnc4_p4 = new ProductState("CNC 4-Machined 5", 5, new PhysicalProperty(new Point(0,1)));
ProductState end = new ProductState("Conveyor End", null, new PhysicalProperty(new Point(0,0)));


//Events




