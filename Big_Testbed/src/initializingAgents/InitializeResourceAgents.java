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
		System.out.println("CREATED: InitializeResourceAgents "+getAID().getName());
		int[] ipsUsed = new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		//int[] ipsUsed = new int[] {5,6,11,12,13,14};
		this.addBehaviour(new RASetup(ipsUsed));
		
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
		private final int[] ipsUsed;

		public RASetup(int[] ipsUsed) {
			this.ipsUsed = ipsUsed;
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
			
			String Robot1 = "192.168.16.2.1.1,1";
			String CNC1 = "192.168.16.4.1.1,2";
			String CNC2 = "192.168.16.3.1.1,3";
			String Robot2 = "192.168.16.1.1.1,4";
			String CNC3 = "192.168.16.2.1.1,1";
			String CNC4 = "192.168.16.4.1.1,2";
			String Conveyor = "192.168.16.3.1.1,3";
			
			String[][] edgeName = new String[][] {
				{"conv1_cnc1","cnc1_conv1","cnc2_conv1","conv1_cnc2"},
				{"cnc1_p0_cnc1","cnc1_p1_cnc1","cnc1_p2_cnc1","cnc1_cnc1_p0","cnc1_cnc1_p1","cnc1_cnc1_p2"},
				{"cnc2_p1_cnc2","cnc2_p2_cnc2","cnc2_p3_cnc2","cnc2_cnc2_p1","cnc2_cnc2_p2","cnc2_cnc2_p3"},
				{"conv2_cnc3","cnc3_conv2","cnc4_conv2","conv2_cnc4"},
				{"cnc3_p2_cnc3","cnc3_p3_cnc3","cnc3_p4_cnc3","cnc3_cnc3_p2","cnc3_cnc3_p3","cnc3_cnc3_p4"},
				{"cnc4_p3_cnc4","cnc4_p4_cnc4","cnc4_p5_cnc4","cnc4_cnc4_p3","cnc4_cnc4_p4","cnc4_cnc4_p5"},
				{"conv1_conv2","conv2_conv1"}};
				
			int[][] edgeTime = new int[][] {
				{300,18000,9000,6000},
				{5000,13000,12000,1000,1000,5000},
				{1500,1500,6000,6000,3500,13000},
				{7000,2500,2500,2500},
				{6000,7000,19000,1000,1500,1000},
				{1500,13000,1500,13000,6000,7000},
				{300,300}};
			
			// Mod 1 - 7 RAs, Mod 2 - 4 RAs, Mod 3 - 6 RAs, Mod 4 - 1 RAs, 
			/*Object[] ips = {ip_mod1,ip_mod1,ip_mod1,ip_mod1,ip_mod1,ip_mod1,ip_mod1,
									ip_mod2,ip_mod2,ip_mod2,ip_mod2,
										ip_mod3,ip_mod3,ip_mod3,ip_mod3,ip_mod3,ip_mod3,
										ip_mod4}; */
			
			Object[] ips = {Robot1,CNC1,CNC2,Robot2,CNC3,CNC4,Conveyor};

			String PLCPort = "851";
			AID[] resourceAgents = new AID[ips.length];
			Capabilities[] raCapabilities = new Capabilities[ips.length];
			
			// Creating agents
			for (int i : ipsUsed) {
				Object ip = ((String) ips[i]).split(",")[0];
				String module = ((String) ips[i]).split(",")[1];
				
				//Name is going to start from 1
				String raName = "mod"+module+".conv"+edgeName[i][0].substring(0,2);
				
				//Create the agent
				try {
					AgentController ac = getContainerController().createNewAgent(raName,
							"resourceAgent.ResourceAgent", new Object[] {ip,PLCPort});
					ac.start();
				} catch (StaleProxyException e) {
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
			ProductState conv1 = new ProductState("Conveyor 1", null, new PhysicalProperty(new Point(5,0)));
			ProductState cnc1 = new ProductState("CNC 1", null, new PhysicalProperty(new Point(6,1)));
			ProductState cnc1_p0 = new ProductState("CNC 1-Machined 0", new PhysicalProperty("p0"), new PhysicalProperty(new Point(6,1)));
			ProductState cnc1_p1 = new ProductState("CNC 1-Machined 1", new PhysicalProperty("p1"), new PhysicalProperty(new Point(6,1)));
			ProductState cnc1_p2 = new ProductState("CNC 1-Machined 2", new PhysicalProperty("p2"), new PhysicalProperty(new Point(6,1)));
			ProductState cnc2 = new ProductState("CNC 2", null, new PhysicalProperty(new Point(5,2)));
			ProductState cnc2_p1 = new ProductState("CNC 2-Machined 1", new PhysicalProperty("p1"), new PhysicalProperty(new Point(5,2)));
			ProductState cnc2_p2 = new ProductState("CNC 2-Machined 2", new PhysicalProperty("p2"), new PhysicalProperty(new Point(5,2)));	
			ProductState cnc2_p3 = new ProductState("CNC 2-Machined 3", new PhysicalProperty("p3"), new PhysicalProperty(new Point(5,2)));
			ProductState conv2 = new ProductState("Conveyor 2", null, new PhysicalProperty(new Point(1,0)));
			ProductState cnc3 = new ProductState("CMC 4", null, new PhysicalProperty(new Point(1,2)));
			ProductState cnc3_p2 = new ProductState("CNC 3-Machined 2", new PhysicalProperty("p2"), new PhysicalProperty(new Point(1,2)));
			ProductState cnc3_p3 = new ProductState("CNC 3-Machined 3", new PhysicalProperty("p3"), new PhysicalProperty(new Point(1,2)));
			ProductState cnc3_p4 = new ProductState("CNC 3-Machined 4", new PhysicalProperty("p4"), new PhysicalProperty(new Point(1,2)));
			ProductState cnc4 = new ProductState("CNC 4", null, new PhysicalProperty(new Point(0,1)));			
			ProductState cnc4_p3 = new ProductState("CNC 4-Machined 3", new PhysicalProperty("p3"), new PhysicalProperty(new Point(0,1)));
			ProductState cnc4_p4 = new ProductState("CNC 4-Machined 4", new PhysicalProperty("p4"), new PhysicalProperty(new Point(0,1)));
			ProductState cnc4_p5 = new ProductState("CNC 4-Machined 5", new PhysicalProperty("p5"), new PhysicalProperty(new Point(0,1)));
			ProductState end = new ProductState("Conveyor End", null,new PhysicalProperty(new Point(0,0)));
			
		
			// Populate all of the edges
					
			HashMap<String, ProductState[]> conveyorEndPoints = new HashMap<String, ProductState[]>();

			//Robot 1 
			conveyorEndPoints.put(edgeName[0][0], new ProductState[] {conv1,cnc1}); // Conveyor 1 to CNC 1
			conveyorEndPoints.put(edgeName[0][1], new ProductState[] {cnc1, conv1}); // CNC 1 to Conveyor 1
			conveyorEndPoints.put(edgeName[0][2], new ProductState[] {cnc2, conv1}); // CNC 2 to Conveyor 1
			conveyorEndPoints.put(edgeName[0][3], new ProductState[] {conv1, cnc2}); // Conveyor 1 to CNC 2
			
			//CNC 1
			conveyorEndPoints.put(edgeName[1][0], new ProductState[] {cnc1_p0, cnc1}); // CNC 1-Machined 0 to CNC 1
			conveyorEndPoints.put(edgeName[1][1], new ProductState[] {cnc1_p1, cnc1}); // CNC 1-Machined 1 to CNC 1
			conveyorEndPoints.put(edgeName[1][2], new ProductState[] {cnc1_p2, cnc1}); // CNC 1-Machined 2 to CNC 1
			conveyorEndPoints.put(edgeName[1][3], new ProductState[] {cnc1, cnc1_p0}); // CNC 1 to CNC 1-Machined 0
			conveyorEndPoints.put(edgeName[1][4], new ProductState[] {cnc1, cnc1_p1}); // CNC 1 to CNC 1-Machined 1
			conveyorEndPoints.put(edgeName[1][5], new ProductState[] {cnc1, cnc1_p2}); // CNC 1 to CNC 1-Machined 2
				
			//CNC 2
			conveyorEndPoints.put(edgeName[2][0], new ProductState[] {cnc2_p1, cnc2}); // CNC 2-Machined 1 to CNC 2
			conveyorEndPoints.put(edgeName[2][1], new ProductState[] {cnc2_p2, cnc2}); // CNC 2-Machined 2 to CNC 2
			conveyorEndPoints.put(edgeName[2][2], new ProductState[] {cnc2_p3, cnc2}); // CNC 2-Machined 3 to CNC 2
			conveyorEndPoints.put(edgeName[2][3], new ProductState[] {cnc2, cnc2_p1}); // CNC 2 to CNC 2-Machined 1
			conveyorEndPoints.put(edgeName[2][4], new ProductState[] {cnc2, cnc2_p2}); // CNC 2 to CNC 2-Machined 2
			conveyorEndPoints.put(edgeName[2][5], new ProductState[] {cnc2, cnc2_p3}); // CNC 2 to CNC 2-Machined 3
			
			//Robot 2
			conveyorEndPoints.put(edgeName[3][0], new ProductState[] {conv2,cnc3}); // Conveyor 2 to CNC 3
			conveyorEndPoints.put(edgeName[3][1], new ProductState[] {cnc3, conv2}); // CNC 3 to Conveyor 2
			conveyorEndPoints.put(edgeName[3][2], new ProductState[] {cnc4, conv2}); // CNC 4 to Conveyor 2
			conveyorEndPoints.put(edgeName[3][3], new ProductState[] {conv2, cnc4}); // Conveyor 2 to CNC 4
			
							
			//CNC 3
			conveyorEndPoints.put(edgeName[4][0], new ProductState[] {cnc3_p2, cnc3}); // CNC 3-Machined 2 to CNC 3
			conveyorEndPoints.put(edgeName[4][1], new ProductState[] {cnc3_p3, cnc3}); // CNC 3-Machined 3 to CNC 3
			conveyorEndPoints.put(edgeName[4][2], new ProductState[] {cnc3_p4, cnc3}); // CNC 3-Machined 4 to CNC 3
			conveyorEndPoints.put(edgeName[4][3], new ProductState[] {cnc3, cnc3_p2}); // CNC 3 to CNC 3-Machined 2
			conveyorEndPoints.put(edgeName[4][4], new ProductState[] {cnc3, cnc3_p3}); // CNC 3 to CNC 3-Machined 3
			conveyorEndPoints.put(edgeName[4][5], new ProductState[] {cnc3, cnc3_p4}); // CNC 3 to CNC 3-Machined 4
			
			//CNC 4
			conveyorEndPoints.put(edgeName[5][0], new ProductState[] {cnc4_p3, cnc4}); // CNC 4-Machined 3 to CNC 4
			conveyorEndPoints.put(edgeName[5][1], new ProductState[] {cnc4_p4, cnc4}); // CNC 4-Machined 4 to CNC 4
			conveyorEndPoints.put(edgeName[5][2], new ProductState[] {cnc4_p5, cnc4}); // CNC 4-Machined 5 to CNC 4
			conveyorEndPoints.put(edgeName[5][3], new ProductState[] {cnc4, cnc4_p3}); // CNC 4 to CNC 4-Machined 3
			conveyorEndPoints.put(edgeName[5][4], new ProductState[] {cnc4, cnc4_p4}); // CNC 4 to CNC 4-Machined 4
			conveyorEndPoints.put(edgeName[5][5], new ProductState[] {cnc4, cnc4_p5}); // CNC 4 to CNC 4-Machined 5
			
			//Conveyor
			conveyorEndPoints.put(edgeName[6][0], new ProductState[] {conv1, conv2}); // Conveyor 1 to Conveyor 2
			conveyorEndPoints.put(edgeName[6][1], new ProductState[] {conv2, end}); // Conveyor 2 to end
			
			// Send out capabilities to RAs
			for (int i : ipsUsed) {
				
				for (int j=0;j<edgeName[i].length;j++) {
					//System.out.println(i+","+j);
					
					ProductState startState = conveyorEndPoints.get(edgeName[i][j])[0];
					ProductState endState = conveyorEndPoints.get(edgeName[i][j])[1];

					raCapabilities[i].addEdge(new ResourceEvent(resourceAgents[i], startState, endState,
							"PAIlya_variables.StartConveyor"+edgeName[i][j]+",1", edgeTime[i][j]));
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
			for (int i : ipsUsed) {
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
				{"Conveyor 1","CNC 1","CNC 2"},
				{"CNC 1","CNC 1-Machined 0","CNC 1-Machined 1","CNC 1-Machined 2"},
				{"CNC 2","CNC 2-Machined 1","CNC 2-Machined 2","CNC 2-Machined 3"},
				{"Conveyor 2","CNC 3","CNC 4"},
				{"CNC 3","CNC 3-Machined 2","CNC 3-Machined 3","CNC 3-Machined 4"},
				{"CNC 4","CNC 4-Machined 3","CNC 4-Machined 4","CNC 4-Machined 5"}};
				
			ProductState[][] statesForRAs= new ProductState[][] {
				{conv1,cnc1,cnc2},
				{cnc1, cnc1_p0, cnc1_p1, cnc1_p2},
				{cnc2, cnc2_p1, cnc2_p2, cnc2_p3},
				{conv2, cnc3, cnc4},
				{cnc3, cnc3_p2, cnc3_p3, cnc3_p4},
				{cnc4_p3, cnc4_p4, cnc4_p5}};
				
							
			for (int i : ipsUsed) {
				WatchRAVariableTable watchVariables = new WatchRAVariableTable();
				
				for (int j=0;j<statesForRAs[i].length;j++) {
					watchVariables.put("PAIlya_variables.PartAt_"+stateNamesForRAs[i][j], statesForRAs[i][j], 100, true, 19000);
				}
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(resourceAgents[i]);
				try { msg.setContentObject(watchVariables);}
				catch (IOException e) {e.printStackTrace();}
				send(msg);
					
			}			
			
			//myAgent.doDelete();
		}
	}	
	
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Resource setup agent is deleted.");
	}
}