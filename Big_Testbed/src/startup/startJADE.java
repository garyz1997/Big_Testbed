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
public class startJADE extends Agent {

	/**
	 * inittest
	 */
	public startJADE() {
		// TODO Auto-generated constructor stub
	}
	 /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	String[][] agentList = {
    			{"helloAgent","agents.HelloWorldAgent"},
    			{"initResource","initializingAgents.InitializeResourceAgents"}
    		};
        String startAgents = "";
        for (int a = 0; a < agentList.length; a++){
        	startAgents += agentList[a][0] + ":" + agentList[a][1] + ";";
        }

		// Start JADE with the above defined agents agents.
		String[] parameters = new String[] { "-gui", // Starts the agent management tools
				// "-container", //Uncomment if you want to run a remote container on a different host
				//"-host", "localhost", // Replace this with your local IP.
				//"-port", "10207", // The port number for inter-platform communication
				startAgents};
		
		Boot.main(parameters);    
    }
}
/*
//States 
ProductState conv1 = new ProductState("Conveyor 1", null, new PhysicalProperty(new Point(5,0)));
ProductState cnc1 = new ProductState("CNC 1", null, new PhysicalProperty(new Point(6,1)));
ProductState cnc1_p0 = new ProductState("CNC 1",new PhysicalProperty("CNC 1-Machined 0"), new PhysicalProperty(new Point(6,1)));
ProductState cnc1_p1 = new ProductState("CNC 1",new PhysicalProperty("CNC 1-Machined 1"), new PhysicalProperty(new Point(6,1)));
ProductState cnc1_p2 = new ProductState("CNC 1",new PhysicalProperty("CNC 1-Machined 2"), new PhysicalProperty(new Point(6,1)));
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
ProductState cnc4_p5 = new ProductState("CNC 4-Machined 5", 5, new PhysicalProperty(new Point(0,1)));
ProductState end = new ProductState("Conveyor End", null, new PhysicalProperty(new Point(0,0)));


//Events for Robot 1
AID raPut = resourceAgents.get(0);
ResourceEvent conv1_cnc1 = new ResourceEvent(raPut, conv1, cnc1, "0", 1000);
ResourceEvent cnc1_conv1 = new ResourceEvent(raPut, cnc1, conv1, "1", 1000);
ResourceEvent cnc1_cnc2 = new ResourceEvent(raPut, cnc1, cnc2, "2", 1000);
ResourceEvent cnc2_cnc1 = new ResourceEvent(raPut, cnc2, cnc1, "3", 1000);
ResourceEvent cnc2_conv1 = new ResourceEvent(raPut, cnc2, conv1, "4", 1000);
ResourceEvent conv1_cnc2 = new ResourceEvent(raPut, conv1, cnc2, "5", 1000);

//Events for CNC 1
raPut = resourceAgents.get(1);
ResourceEvent cnc1_p0_cnc1 = new ResourceEvent(raPut, cnc1_p0, cnc1, "0", 1000);
ResourceEvent cnc1_p1_cnc1 = new ResourceEvent(raPut, cnc1_p1, cnc1, "1", 1000);
ResourceEvent cnc1_p2_cnc1 = new ResourceEvent(raPut, cnc1_p2, cnc1, "2", 1000);
ResourceEvent cnc1_cnc1_p0 = new ResourceEvent(raPut, cnc1, cnc1_p0, "3", 1000);
ResourceEvent cnc1_cnc1_p1 = new ResourceEvent(raPut, cnc1, cnc1_p1, "4", 1000);
ResourceEvent cnc1_cnc1_p2 = new ResourceEvent(raPut, cnc1, cnc1_p2, "5", 1000);

//Events for CNC 2
AID raPut = resourceAgents.get(2);
ResourceEvent cnc2_p1_cnc2 = new ResourceEvent(raPut, cnc2_p1, cnc2, "0", 1000);
ResourceEvent cnc2_p2_cnc2 = new ResourceEvent(raPut, cnc2_p2, cnc2, "1", 1000);
ResourceEvent cnc2_p3_cnc2 = new ResourceEvent(raPut, cnc2_p3, cnc2, "2", 1000);
ResourceEvent cnc2_cnc2_p1 = new ResourceEvent(raPut, cnc2, cnc2_p1, "3", 1000);
ResourceEvent cnc2_cnc2_p2 = new ResourceEvent(raPut, cnc2, cnc2_p2, "4", 1000);
ResourceEvent cnc2_cnc2_p3 = new ResourceEvent(raPut, cnc2, cnc2_p3, "5", 1000);

//Events for Robot 2
AID raPut = resourceAgents.get(3);
ResourceEvent conv2_cnc3 = new ResourceEvent(raPut, conv2, cnc3, "0", 1000);
ResourceEvent cnc3_conv2 = new ResourceEvent(raPut, cnc3, conv2, "1", 1000);
ResourceEvent cnc3_cnc4 = new ResourceEvent(raPut, cnc3, cnc4, "2", 1000);
ResourceEvent cnc4_cnc3 = new ResourceEvent(raPut, cnc4, cnc3, "3", 1000);
ResourceEvent cnc4_conv3 = new ResourceEvent(raPut, cnc4, conv2, "4", 1000);
ResourceEvent conv3_cnc4 = new ResourceEvent(raPut, conv2, cnc4, "5", 1000);

//Events for CNC 3
AID raPut = resourceAgents.get(4);
ResourceEvent cnc3_p2_cnc3 = new ResourceEvent(raPut, cnc3_p2, cnc3, "0", 1000);
ResourceEvent cnc3_p3_cnc3 = new ResourceEvent(raPut, cnc3_p3, cnc3, "1", 1000);
ResourceEvent cnc3_p4_cnc3 = new ResourceEvent(raPut, cnc3_p4, cnc3, "2", 1000);
ResourceEvent cnc3_cnc3_p2 = new ResourceEvent(raPut, cnc3, cnc3_p2, "3", 1000);
ResourceEvent cnc3_cnc3_p3 = new ResourceEvent(raPut, cnc3, cnc3_p3, "4", 1000);
ResourceEvent cnc3_cnc3_p4 = new ResourceEvent(raPut, cnc3, cnc3_p4, "5", 1000);

//Events for CNC 4
AID raPut = resourceAgents.get(5);
ResourceEvent cnc4_p3_cnc4 = new ResourceEvent(raPut, cnc4_p3, cnc4, "0", 1000);
ResourceEvent cnc4_p4_cnc4 = new ResourceEvent(raPut, cnc4_p4, cnc4, "1", 1000);
ResourceEvent cnc4_p5_cnc4 = new ResourceEvent(raPut, cnc4_p5, cnc4, "2", 1000);
ResourceEvent cnc4_cnc4_p3 = new ResourceEvent(raPut, cnc4, cnc4_p3, "3", 1000);
ResourceEvent cnc4_cnc4_p4 = new ResourceEvent(raPut, cnc4, cnc4_p4, "4", 1000);
ResourceEvent cnc4_cnc4_p5 = new ResourceEvent(raPut, cnc4, cnc4_p5, "5", 1000);

//Events for Conveyor
AID raPut = resourceAgents.get(0);
ResourceEvent conv1_conv2 = new ResourceEvent(raPut, conv1, conv2, "0", 1000);
ResourceEvent conv2_end = new ResourceEvent(raPut, conv2, end, "1", 1000);

*/





