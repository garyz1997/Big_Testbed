package initializingAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import resourceAgent.ConveyorAgent;
import resourceAgent.ResourceAgent;
import java.awt.Point;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import sharedInformation.Capabilities;
import sharedInformation.CapabilitiesTable;
import sharedInformation.PhysicalProperty;
import sharedInformation.ProductState;
import sharedInformation.ResourceEvent;
import sharedInformation.SetupProductAgent;
import sharedInformation.WatchRAVariableTable;

public class InitializeResourceAgents extends Agent {
	
	private static final long serialVersionUID = 6601315432329573157L;
	private CapabilitiesTable allCapabilities;

	public InitializeResourceAgents() {}
	
	protected void setup() {
		System.out.println("[" + this.getLocalName()+"] CREATED: InitializeResourceAgents "+getAID().getLocalName());
		int[] RAs_Used = new int[] {0,1,2};
		this.addBehaviour(new RASetup(RAs_Used));
		
		this.allCapabilities = new CapabilitiesTable();
	}
	
	private class RASetup extends OneShotBehaviour{

		private static final long serialVersionUID = 6736634843017599144L;
		private final int[] RAs_Used;

		public RASetup(int[] RAs_Used) {
			this.RAs_Used = RAs_Used;
		}

		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		@Override
		public void action() {
		
			//================================================================================
		    // Start Agents
		    //================================================================================
			
			String[][] edgeName = new String[][] {
				{"conv1_cnc1p1","cnc1p1_conv1","cnc2p2_conv1","conv1_cnc2p2"}, //Events for Robot 1
				{"conv2_cnc3p3","cnc3p3_conv2","cnc4p4_conv2","conv2_cnc4p4"}, //Events for Robot 2
				{"conv1_conv2","conv2_conv3","conv3_conv1", "conv3_end"}}; //Events for Conveyor
				
			int[][] edgeTime = new int[][] { //TODO: Find out edge times for each event
				{20000,20000,20000,20000}, //Robot 1
				{20000,20000,20000,20000}, //Robot 2
				{3000,3000,3000,3000}}; //Conveyor
			
			String[] RA_names = {"Robot1Agent", "Robot2Agent", "ConveyorAgent"}; //Resource Agent names
			String[] RA_class = {"resourceAgent.Robot1Agent", "resourceAgent.Robot2Agent", "resourceAgent.ConveyorAgent"}; //Classes for each resource agent 

			AID[] resourceAgents = new AID[RA_names.length];
			Capabilities[] raCapabilities = new Capabilities[RA_names.length];
			
			// Creating agents
			for (int i : RAs_Used) {
				
				//Name for each resource agent
				String raName = RA_names[i];
				String raClass = RA_class[i];
				//Create the agent
				try {
					AgentController ac = getContainerController().createNewAgent(raName, raClass, new Object[] {});
					ac.start();
				} 
				catch (StaleProxyException e) {
					e.printStackTrace();
				}
				
				//Record agent and RA capabilities to make setup faster
				resourceAgents[i] = new AID(raName,AID.ISLOCALNAME);
				raCapabilities[i] = new Capabilities();
			}
			
			//Wait a second
			myAgent.doWait(1000);
			
			//================================================================================
		    // Populate RA Capabilities
		    //================================================================================
			
			// Populate all of the states


			
			//Cell 1 states
			/*old
			ProductState conv1 = new ProductState("Conveyor1", null, new PhysicalProperty(new Point(5,0)));
			ProductState cnc1 = new ProductState("CNC1", null, new PhysicalProperty(new Point(6,1)));
			ProductState cnc1_p0 = new ProductState("CNC1-Machined0", new PhysicalProperty("p0"), new PhysicalProperty(new Point(6,1)));
			ProductState cnc1_p1 = new ProductState("CNC1-Machined1", new PhysicalProperty("p1"), new PhysicalProperty(new Point(6,1)));
			ProductState cnc1_p2 = new ProductState("CNC1-Machined2", new PhysicalProperty("p2"), new PhysicalProperty(new Point(6,1)));
			ProductState cnc2 = new ProductState("CNC2", null, new PhysicalProperty(new Point(5,2)));
			ProductState cnc2_p1 = new ProductState("CNC2-Machined1", new PhysicalProperty("p1"), new PhysicalProperty(new Point(5,2)));
			ProductState cnc2_p2 = new ProductState("CNC2-Machined2", new PhysicalProperty("p2"), new PhysicalProperty(new Point(5,2)));	
			ProductState cnc2_p3 = new ProductState("CNC2-Machined3", new PhysicalProperty("p3"), new PhysicalProperty(new Point(5,2)));
			*/
			//New states
			ProductState conv1 = new ProductState("Conveyor1", new PhysicalProperty("conveyor"), new PhysicalProperty(new Point(5,0)));
			ProductState cnc1_p1 = new ProductState("CNC1Machined1", new PhysicalProperty("p1"), new PhysicalProperty(new Point(6,1)));
			ProductState cnc2_p2 = new ProductState("CNC2Machined2", new PhysicalProperty("p2"), new PhysicalProperty(new Point(5,2)));
			
			//Cell 2 states
			/*old
			ProductState conv2 = new ProductState("Conveyor2", null, new PhysicalProperty(new Point(1,0)));
			ProductState cnc3 = new ProductState("CNC3", null, new PhysicalProperty(new Point(1,2)));
			ProductState cnc3_p2 = new ProductState("CNC3-Machined2", new PhysicalProperty("p2"), new PhysicalProperty(new Point(1,2)));
			ProductState cnc3_p3 = new ProductState("CNC3-Machined3", new PhysicalProperty("p3"), new PhysicalProperty(new Point(1,2)));
			ProductState cnc3_p4 = new ProductState("CNC3-Machined4", new PhysicalProperty("p4"), new PhysicalProperty(new Point(1,2)));
			ProductState cnc4 = new ProductState("CNC4", null, new PhysicalProperty(new Point(0,1)));			
			ProductState cnc4_p3 = new ProductState("CNC4-Machined3", new PhysicalProperty("p3"), new PhysicalProperty(new Point(0,1)));
			ProductState cnc4_p4 = new ProductState("CNC4-Machined4", new PhysicalProperty("p4"), new PhysicalProperty(new Point(0,1)));
			ProductState cnc4_p5 = new ProductState("CNC4-Machined5", new PhysicalProperty("p5"), new PhysicalProperty(new Point(0,1)));
			*/
			//New states
			ProductState conv2 = new ProductState("Conveyor2", new PhysicalProperty("conveyor"), new PhysicalProperty(new Point(1,0)));
			ProductState cnc3_p3 = new ProductState("CNC3Machined3", new PhysicalProperty("p3"), new PhysicalProperty(new Point(1,2)));
			ProductState cnc4_p4 = new ProductState("CNC4Machined4", new PhysicalProperty("p4"), new PhysicalProperty(new Point(0,1)));
			
			//End States
			ProductState conv3 = new ProductState("Conveyor3", new PhysicalProperty("conveyor"),new PhysicalProperty(new Point(0,0)));
			ProductState end = new ProductState("ConveyorEnd", new PhysicalProperty("end"),new PhysicalProperty(new Point(0,0)));
		
			// Populate all of the edges
					
			HashMap<String, ProductState[]> stateEndpoints = new HashMap<String, ProductState[]>();

			//Robot 1 events
			stateEndpoints.put(edgeName[0][0], new ProductState[] {conv1, cnc1_p1}); // Conveyor 1 to CNC 1
			stateEndpoints.put(edgeName[0][1], new ProductState[] {cnc1_p1, conv1}); // CNC 1 to Conveyor 1
			stateEndpoints.put(edgeName[0][2], new ProductState[] {cnc2_p2, conv1}); // CNC 2 to Conveyor 1
			stateEndpoints.put(edgeName[0][3], new ProductState[] {conv1, cnc2_p2}); // Conveyor 1 to CNC 2
			
			/*old events
			//CNC 1 events
			stateEndpoints.put(edgeName[1][0], new ProductState[] {cnc1_p0, cnc1}); // CNC 1-Machined 0 to CNC 1
			stateEndpoints.put(edgeName[1][1], new ProductState[] {cnc1_p1, cnc1}); // CNC 1-Machined 1 to CNC 1
			stateEndpoints.put(edgeName[1][2], new ProductState[] {cnc1_p2, cnc1}); // CNC 1-Machined 2 to CNC 1
			stateEndpoints.put(edgeName[1][3], new ProductState[] {cnc1, cnc1_p0}); // CNC 1 to CNC 1-Machined 0
			stateEndpoints.put(edgeName[1][4], new ProductState[] {cnc1, cnc1_p1}); // CNC 1 to CNC 1-Machined 1
			stateEndpoints.put(edgeName[1][5], new ProductState[] {cnc1, cnc1_p2}); // CNC 1 to CNC 1-Machined 2
			
			//CNC 2 events 
			stateEndpoints.put(edgeName[2][0], new ProductState[] {cnc2_p1, cnc2}); // CNC 2-Machined 1 to CNC 2
			stateEndpoints.put(edgeName[2][1], new ProductState[] {cnc2_p2, cnc2}); // CNC 2-Machined 2 to CNC 2
			stateEndpoints.put(edgeName[2][2], new ProductState[] {cnc2_p3, cnc2}); // CNC 2-Machined 3 to CNC 2
			stateEndpoints.put(edgeName[2][3], new ProductState[] {cnc2, cnc2_p1}); // CNC 2 to CNC 2-Machined 1
			stateEndpoints.put(edgeName[2][4], new ProductState[] {cnc2, cnc2_p2}); // CNC 2 to CNC 2-Machined 2
			stateEndpoints.put(edgeName[2][5], new ProductState[] {cnc2, cnc2_p3}); // CNC 2 to CNC 2-Machined 3
			*/
			
			//Robot 2 events 
			stateEndpoints.put(edgeName[1][0], new ProductState[] {conv2,cnc3_p3}); // Conveyor 2 to CNC 3
			stateEndpoints.put(edgeName[1][1], new ProductState[] {cnc3_p3, conv2}); // CNC 3 to Conveyor 2
			stateEndpoints.put(edgeName[1][2], new ProductState[] {cnc4_p4, conv2}); // CNC 4 to Conveyor 2
			stateEndpoints.put(edgeName[1][3], new ProductState[] {conv2, cnc4_p4}); // Conveyor 2 to CNC 4
							
			/*old events
			//CNC 3 events
			stateEndpoints.put(edgeName[4][0], new ProductState[] {cnc3_p2, cnc3}); // CNC 3-Machined 2 to CNC 3
			stateEndpoints.put(edgeName[4][1], new ProductState[] {cnc3_p3, cnc3}); // CNC 3-Machined 3 to CNC 3
			stateEndpoints.put(edgeName[4][2], new ProductState[] {cnc3_p4, cnc3}); // CNC 3-Machined 4 to CNC 3
			stateEndpoints.put(edgeName[4][3], new ProductState[] {cnc3, cnc3_p2}); // CNC 3 to CNC 3-Machined 2
			stateEndpoints.put(edgeName[4][4], new ProductState[] {cnc3, cnc3_p3}); // CNC 3 to CNC 3-Machined 3
			stateEndpoints.put(edgeName[4][5], new ProductState[] {cnc3, cnc3_p4}); // CNC 3 to CNC 3-Machined 4
			
			//CNC 4 events
			stateEndpoints.put(edgeName[5][0], new ProductState[] {cnc4_p3, cnc4}); // CNC 4-Machined 3 to CNC 4
			stateEndpoints.put(edgeName[5][1], new ProductState[] {cnc4_p4, cnc4}); // CNC 4-Machined 4 to CNC 4
			stateEndpoints.put(edgeName[5][2], new ProductState[] {cnc4_p5, cnc4}); // CNC 4-Machined 5 to CNC 4
			stateEndpoints.put(edgeName[5][3], new ProductState[] {cnc4, cnc4_p3}); // CNC 4 to CNC 4-Machined 3
			stateEndpoints.put(edgeName[5][4], new ProductState[] {cnc4, cnc4_p4}); // CNC 4 to CNC 4-Machined 4
			stateEndpoints.put(edgeName[5][5], new ProductState[] {cnc4, cnc4_p5}); // CNC 4 to CNC 4-Machined 5
			*/
			
			//Conveyor events
			stateEndpoints.put(edgeName[2][0], new ProductState[] {conv1, conv2}); // Conveyor 1 to Conveyor 2
			stateEndpoints.put(edgeName[2][1], new ProductState[] {conv2, conv3}); // Conveyor 2 to end
			stateEndpoints.put(edgeName[2][2], new ProductState[] {conv3, conv1}); // end to Conveyor 1
			stateEndpoints.put(edgeName[2][3], new ProductState[] {conv3, end}); // end to Conveyor 1 
			
			// Send out capabilities to RAs
			for (int i : RAs_Used) {
				
				for (int j=0;j<edgeName[i].length;j++) {
					//System.out.println(i+","+j);
					
					ProductState startState = stateEndpoints.get(edgeName[i][j])[0];
					ProductState endState = stateEndpoints.get(edgeName[i][j])[1];

					raCapabilities[i].addEdge(new ResourceEvent(resourceAgents[i], startState, endState,
							"PerformEvent_"+edgeName[i][j]+",1", edgeTime[i][j]));
				}


				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(resourceAgents[i]);
				
				try { msg.setContentObject(raCapabilities[i]);}
				catch (IOException e) {e.printStackTrace();}
				send(msg);

				// Populate capabilities table
				allCapabilities.put(resourceAgents[i], raCapabilities[i]);
			}
			
			//Wait a couple of seconds
			myAgent.doWait(3000);
			
			//================================================================================
		    // Populate Neighbors
		    //================================================================================
			
			// Let RAs figure out their neighbors
			for (int i : RAs_Used) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(resourceAgents[i]);
				
				try { msg.setContentObject(allCapabilities);}
				catch (IOException e) {e.printStackTrace();}
				send(msg);
			}
			
			//================================================================================
		    // Set up RA sensing
		    //================================================================================

			//Wait a couple of seconds
			myAgent.doWait(2000);
			
			String[][] stateNamesForRAs= new String[][] {
				{"CNC1Machined1","CNC2Machined2","RFID_N054:I.Channel[1].TagPresent"}, //Robot 1 states
				{"CNC3Machined3","CNC4Machined4","RFID_N055:I.Channel[0].TagPresent"}, //Robot 2 state names
				{"Conveyor1","Conveyor2","Conveyor3", "ConveyorEnd"} //Conveyor state names
				};
				
			ProductState[][] statesForRAs= new ProductState[][] { //Same as above but these are the variables used
				{cnc1_p1, cnc2_p2, conv1},
				{cnc3_p3, cnc4_p4, conv2},
				{conv1, conv2, conv3, end}
				};
				
			String[][] watchVariableNames= new String[][] {
				{"CNC1Machined1","CNC2Machined2","RFID_N054:I.Channel[1].TagPresent"},
				{"CNC3Machined3","CNC4Machined4","RFID_N055:I.Channel[0].TagPresent"},
				{"C1_N011:I.Data[6].7","Conv_N053:I.Data[3].1","Conv_N053:I.Data[3].3", "ConveyorEnd"}
				};
							
			for (int i : RAs_Used) {
				WatchRAVariableTable watchVariables = new WatchRAVariableTable();
				
				for (int j=0;j<statesForRAs[i].length;j++) {
					watchVariables.put(watchVariableNames[i][j], statesForRAs[i][j], 100, true, 1000);//monitorperiod*10
				}
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(resourceAgents[i]);
				try { 
					msg.setContentObject(watchVariables);
					Thread.sleep(100);
				}
				catch (IOException | InterruptedException e) {e.printStackTrace();}
				send(msg);
					
			}			
			
			myAgent.doDelete();
		}
	}	
	
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("DELETED: "+getAID().getLocalName());
	}
}