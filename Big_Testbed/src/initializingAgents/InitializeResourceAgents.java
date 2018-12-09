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
		System.out.println("CREATED: InitializeResourceAgents "+getAID().getLocalName());
		int[] RAs_Used = new int[] {0,1,2,3,4,5,6};
		//int[] ipsUsed = new int[] {5,6,11,12,13,14};
		this.addBehaviour(new RASetup(RAs_Used));
		
		/*final int[] ipsUsed2 = new int[] {7,8,9,10};
		//final int[] ipsUsed2 = new int[] {7,8};
		this.addBehaviour(new WakerBehaviour(this, 180000){
			private static final long serialVersionUID = -8850259243576833217L;
			protected void onWake() {
				myAgent.addBehaviour(new RASetup(ipsUsed2));
			}
		});*/
		
		
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
			
			//Input IPS and PLC Port
			/* String ip_mod1 = "192.168.16.2.1.1,1";
			String ip_mod2 = "192.168.16.4.1.1,2";
			String ip_mod3 = "192.168.16.3.1.1,3";
			String ip_mod4 = "192.168.16.1.1.1,4"; */
			/* Already initialized below
			String Robot1 = "Robot1";
			String CNC1 = "CNC1";
			String CNC2 = "CNC2";
			String Robot2 = "Robot2";
			String CNC3 = "CNC3";
			String CNC4 = "CNC4";
			String Conveyor = "Conveyor";
			*/
			String[][] edgeName = new String[][] {
				{"conv1_cnc1","cnc1_conv1","cnc2_conv1","conv1_cnc2"},
				{"cnc1_p0_cnc1","cnc1_p1_cnc1","cnc1_p2_cnc1","cnc1_cnc1_p0","cnc1_cnc1_p1","cnc1_cnc1_p2"},
				{"cnc2_p1_cnc2","cnc2_p2_cnc2","cnc2_p3_cnc2","cnc2_cnc2_p1","cnc2_cnc2_p2","cnc2_cnc2_p3"},
				{"conv2_cnc3","cnc3_conv2","cnc4_conv2","conv2_cnc4"},
				{"cnc3_p2_cnc3","cnc3_p3_cnc3","cnc3_p4_cnc3","cnc3_cnc3_p2","cnc3_cnc3_p3","cnc3_cnc3_p4"},
				{"cnc4_p3_cnc4","cnc4_p4_cnc4","cnc4_p5_cnc4","cnc4_cnc4_p3","cnc4_cnc4_p4","cnc4_cnc4_p5"},
				{"conv1_conv2","conv2_conv3","conv3_conv1", "conv3_end"}};
				
			int[][] edgeTime = new int[][] {
				{3000,18000,18000,6000},
				{5000,13000,12000,1000,1000,5000},
				{1500,1500,6000,6000,3500,13000},
				{7000,2500,2500,2500},
				{6000,7000,19000,1000,1500,1000},
				{1500,13000,1500,13000,6000,7000},
				{3000,3000,3000, 3000}};
			
			String[] RA_names = {"(RA)Robot1","(RA)CNC1","(RA)CNC2","(RA)Robot2","(RA)CNC3","(RA)CNC4","(RA)Conveyor"};
			String[] RA_class = {"resourceAgent.Robot1Agent", "resourceAgent.CNC1Agent", "resourceAgent.CNC2Agent", "resourceAgent.Robot2Agent", "resourceAgent.CNC3Agent", "resourceAgent.CNC4Agent", "resourceAgent.ConveyorAgent"}; 

			AID[] resourceAgents = new AID[RA_names.length];
			Capabilities[] raCapabilities = new Capabilities[RA_names.length];
			
			// Creating agents
			for (int i : RAs_Used) {
				//Object ip = ((String) ips[i]).split(",")[0];
				//String module = ((String) ips[i]).split(",")[1];
				
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


			
			//Module 1
			ProductState conv1 = new ProductState("Conveyor1", null, new PhysicalProperty(new Point(5,0)));
			ProductState cnc1 = new ProductState("CNC1", null, new PhysicalProperty(new Point(6,1)));
			ProductState cnc1_p0 = new ProductState("CNC1-Machined0", new PhysicalProperty("p0"), new PhysicalProperty(new Point(6,1)));
			ProductState cnc1_p1 = new ProductState("CNC1-Machined1", new PhysicalProperty("p1"), new PhysicalProperty(new Point(6,1)));
			ProductState cnc1_p2 = new ProductState("CNC1-Machined2", new PhysicalProperty("p2"), new PhysicalProperty(new Point(6,1)));
			ProductState cnc2 = new ProductState("CNC2", null, new PhysicalProperty(new Point(5,2)));
			ProductState cnc2_p1 = new ProductState("CNC2-Machined1", new PhysicalProperty("p1"), new PhysicalProperty(new Point(5,2)));
			ProductState cnc2_p2 = new ProductState("CNC2-Machined2", new PhysicalProperty("p2"), new PhysicalProperty(new Point(5,2)));	
			ProductState cnc2_p3 = new ProductState("CNC2-Machined3", new PhysicalProperty("p3"), new PhysicalProperty(new Point(5,2)));
			ProductState conv2 = new ProductState("Conveyor2", null, new PhysicalProperty(new Point(1,0)));
			ProductState cnc3 = new ProductState("CNC4", null, new PhysicalProperty(new Point(1,2)));
			ProductState cnc3_p2 = new ProductState("CNC3-Machined2", new PhysicalProperty("p2"), new PhysicalProperty(new Point(1,2)));
			ProductState cnc3_p3 = new ProductState("CNC3-Machined3", new PhysicalProperty("p3"), new PhysicalProperty(new Point(1,2)));
			ProductState cnc3_p4 = new ProductState("CNC3-Machined4", new PhysicalProperty("p4"), new PhysicalProperty(new Point(1,2)));
			ProductState cnc4 = new ProductState("CNC4", null, new PhysicalProperty(new Point(0,1)));			
			ProductState cnc4_p3 = new ProductState("CNC4-Machined3", new PhysicalProperty("p3"), new PhysicalProperty(new Point(0,1)));
			ProductState cnc4_p4 = new ProductState("CNC4-Machined4", new PhysicalProperty("p4"), new PhysicalProperty(new Point(0,1)));
			ProductState cnc4_p5 = new ProductState("CNC4-Machined5", new PhysicalProperty("p5"), new PhysicalProperty(new Point(0,1)));
			ProductState conv3 = new ProductState("Conveyor3", null,new PhysicalProperty(new Point(0,0)));
			ProductState end = new ProductState("ConveyorEnd", new PhysicalProperty("end"),new PhysicalProperty(new Point(0,0)));
		
			// Populate all of the edges
					
			HashMap<String, ProductState[]> stateEndpoints = new HashMap<String, ProductState[]>();

			//Robot 1 
			stateEndpoints.put(edgeName[0][0], new ProductState[] {conv1, cnc1}); // Conveyor 1 to CNC 1
			stateEndpoints.put(edgeName[0][1], new ProductState[] {cnc1, conv1}); // CNC 1 to Conveyor 1
			stateEndpoints.put(edgeName[0][2], new ProductState[] {cnc2, conv1}); // CNC 2 to Conveyor 1
			stateEndpoints.put(edgeName[0][3], new ProductState[] {conv1, cnc2}); // Conveyor 1 to CNC 2
			
			//CNC 1
			stateEndpoints.put(edgeName[1][0], new ProductState[] {cnc1_p0, cnc1}); // CNC 1-Machined 0 to CNC 1
			stateEndpoints.put(edgeName[1][1], new ProductState[] {cnc1_p1, cnc1}); // CNC 1-Machined 1 to CNC 1
			stateEndpoints.put(edgeName[1][2], new ProductState[] {cnc1_p2, cnc1}); // CNC 1-Machined 2 to CNC 1
			stateEndpoints.put(edgeName[1][3], new ProductState[] {cnc1, cnc1_p0}); // CNC 1 to CNC 1-Machined 0
			stateEndpoints.put(edgeName[1][4], new ProductState[] {cnc1, cnc1_p1}); // CNC 1 to CNC 1-Machined 1
			stateEndpoints.put(edgeName[1][5], new ProductState[] {cnc1, cnc1_p2}); // CNC 1 to CNC 1-Machined 2
				
			//CNC 2
			stateEndpoints.put(edgeName[2][0], new ProductState[] {cnc2_p1, cnc2}); // CNC 2-Machined 1 to CNC 2
			stateEndpoints.put(edgeName[2][1], new ProductState[] {cnc2_p2, cnc2}); // CNC 2-Machined 2 to CNC 2
			stateEndpoints.put(edgeName[2][2], new ProductState[] {cnc2_p3, cnc2}); // CNC 2-Machined 3 to CNC 2
			stateEndpoints.put(edgeName[2][3], new ProductState[] {cnc2, cnc2_p1}); // CNC 2 to CNC 2-Machined 1
			stateEndpoints.put(edgeName[2][4], new ProductState[] {cnc2, cnc2_p2}); // CNC 2 to CNC 2-Machined 2
			stateEndpoints.put(edgeName[2][5], new ProductState[] {cnc2, cnc2_p3}); // CNC 2 to CNC 2-Machined 3
			
			//Robot 2
			stateEndpoints.put(edgeName[3][0], new ProductState[] {conv2,cnc3}); // Conveyor 2 to CNC 3
			stateEndpoints.put(edgeName[3][1], new ProductState[] {cnc3, conv2}); // CNC 3 to Conveyor 2
			stateEndpoints.put(edgeName[3][2], new ProductState[] {cnc4, conv2}); // CNC 4 to Conveyor 2
			stateEndpoints.put(edgeName[3][3], new ProductState[] {conv2, cnc4}); // Conveyor 2 to CNC 4
							
			//CNC 3
			stateEndpoints.put(edgeName[4][0], new ProductState[] {cnc3_p2, cnc3}); // CNC 3-Machined 2 to CNC 3
			stateEndpoints.put(edgeName[4][1], new ProductState[] {cnc3_p3, cnc3}); // CNC 3-Machined 3 to CNC 3
			stateEndpoints.put(edgeName[4][2], new ProductState[] {cnc3_p4, cnc3}); // CNC 3-Machined 4 to CNC 3
			stateEndpoints.put(edgeName[4][3], new ProductState[] {cnc3, cnc3_p2}); // CNC 3 to CNC 3-Machined 2
			stateEndpoints.put(edgeName[4][4], new ProductState[] {cnc3, cnc3_p3}); // CNC 3 to CNC 3-Machined 3
			stateEndpoints.put(edgeName[4][5], new ProductState[] {cnc3, cnc3_p4}); // CNC 3 to CNC 3-Machined 4
			
			//CNC 4
			stateEndpoints.put(edgeName[5][0], new ProductState[] {cnc4_p3, cnc4}); // CNC 4-Machined 3 to CNC 4
			stateEndpoints.put(edgeName[5][1], new ProductState[] {cnc4_p4, cnc4}); // CNC 4-Machined 4 to CNC 4
			stateEndpoints.put(edgeName[5][2], new ProductState[] {cnc4_p5, cnc4}); // CNC 4-Machined 5 to CNC 4
			stateEndpoints.put(edgeName[5][3], new ProductState[] {cnc4, cnc4_p3}); // CNC 4 to CNC 4-Machined 3
			stateEndpoints.put(edgeName[5][4], new ProductState[] {cnc4, cnc4_p4}); // CNC 4 to CNC 4-Machined 4
			stateEndpoints.put(edgeName[5][5], new ProductState[] {cnc4, cnc4_p5}); // CNC 4 to CNC 4-Machined 5
			
			//Conveyor
			stateEndpoints.put(edgeName[6][0], new ProductState[] {conv1, conv2}); // Conveyor 1 to Conveyor 2
			stateEndpoints.put(edgeName[6][1], new ProductState[] {conv2, conv3}); // Conveyor 2 to end
			stateEndpoints.put(edgeName[6][2], new ProductState[] {conv3, conv1}); // end to Conveyor 1
			stateEndpoints.put(edgeName[6][3], new ProductState[] {conv3, end}); // end to Conveyor 1 
			
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
				{"Conveyor1","CNC1","CNC2"},
				{"CNC1","CNC1-Machined0","CNC1-Machined1","CNC 1-Machined2"},
				{"CNC2","CNC2-Machined1","CNC2-Machined2","CNC 2-Machined3"},
				{"Conveyor2","CNC3","CNC4"},
				{"CNC3","CNC3-Machined2","CNC3-Machined3","CNC 3-Machined4"},
				{"CNC4","CNC4-Machined3","CNC4-Machined4","CNC 4-Machined5"},
				{"Conveyor1","Conveyor2","Conveyor3", "ConveyorEnd"}
				};
				
			ProductState[][] statesForRAs= new ProductState[][] {
				{conv1, cnc1, cnc2},
				{cnc1, cnc1_p0, cnc1_p1, cnc1_p2},
				{cnc2, cnc2_p1, cnc2_p2, cnc2_p3},
				{conv2, cnc3, cnc4},
				{cnc3, cnc3_p2, cnc3_p3, cnc3_p4},
				{cnc4, cnc4_p3, cnc4_p4, cnc4_p5},
				{conv1, conv2, conv3, end}
				};
				
							
			for (int i : RAs_Used) {
				WatchRAVariableTable watchVariables = new WatchRAVariableTable();
				
				for (int j=0;j<statesForRAs[i].length;j++) {
					watchVariables.put("PartAt_"+stateNamesForRAs[i][j], statesForRAs[i][j], 100, true, 19000);
				}
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(resourceAgents[i]);
				try { msg.setContentObject(watchVariables);}
				catch (IOException e) {e.printStackTrace();}
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