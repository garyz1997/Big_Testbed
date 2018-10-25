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
			String ip_mod1 = "192.168.16.2.1.1,1";
			String ip_mod2 = "192.168.16.4.1.1,2";
			String ip_mod3 = "192.168.16.3.1.1,3";
			String ip_mod4 = "192.168.16.1.1.1,4";
			
			String[][] edgeName = new String[][] {
				{"18"},{"17"},{"16"},{"15"},{"01"},{"02"},{"04","04M2","04M3"},
					{"05","05M2","05M4"},{"11"},{"12"},{"20"},
						{"06"},{"07","07M3M3","07M3M4","07M4M3"},{"09"},{"08"},{"22"},{"21","22RM3","22RM2"},
							{"10_M2M4","10_M4M3","10_M3M4","10_M4M2"}};
				
			int[][] edgeTime = new int[][] {
				{300},{18000},{9000},{6000},{5000},{13000},{12000,1000,1000},
					{5000,1500,1500},{6000},{6000},{3500},
						{13000},{7000,2500,2500,2500},{6000},{7000},{19000},{1000,1500,1000},
							{1500,13000,1500,13000}};
			
			// Mod 1 - 7 RAs, Mod 2 - 4 RAs, Mod 3 - 6 RAs, Mod 4 - 1 RAs, 
			Object[] ips = {ip_mod1,ip_mod1,ip_mod1,ip_mod1,ip_mod1,ip_mod1,ip_mod1,
									ip_mod2,ip_mod2,ip_mod2,ip_mod2,
										ip_mod3,ip_mod3,ip_mod3,ip_mod3,ip_mod3,ip_mod3,
										ip_mod4};
			
			//Object[] ips = {ip_mod1,ip_mod1,ip_mod1,ip_mod1,ip_mod1,ip_mod1};

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

			ProductState end = new ProductState("conveyor", new PhysicalProperty("End"), new PhysicalProperty(new Point(40,9)));
			
			//Module 1
			ProductState c18_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(20,0)));
			ProductState c18_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(20,9)));
			ProductState c17_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(19,10)));
			ProductState c17_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(1,10)));
			ProductState c16_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(0,11)));
			ProductState c16_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(0,19)));
			ProductState c15_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(1,20)));
			ProductState c15_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(9,20)));	
			ProductState c01_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(10,21)));
			ProductState c01_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(10,29)));
			ProductState c02_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(11,30)));
			ProductState c02_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(19,30)));
			ProductState c04_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(21,30)));
			ProductState c04_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(39,30)));
			
			ProductState swch_08_arr = new ProductState("conveyor", null, new PhysicalProperty(new Point(20,10)));			
			ProductState swch_07_arr = new ProductState("conveyor", null, new PhysicalProperty(new Point(10,20)));
			ProductState swch_01_arr = new ProductState("conveyor", null, new PhysicalProperty(new Point(20,30)));
			ProductState swch_02_arr = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,30)));
			ProductState swch_02_finM2 = new ProductState("conveyor", new PhysicalProperty("switchedM2")
					,new PhysicalProperty(new Point(40,30)));
			ProductState swch_02_finM3 = new ProductState("conveyor", new PhysicalProperty("switchedM3")
					,new PhysicalProperty(new Point(40,30)));
			
			//Module 2
			ProductState c05_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,29)));
			ProductState c05_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,21)));
			ProductState c11_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(39,20)));
			ProductState c11_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(31,20)));
			ProductState c12_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(30,19)));
			ProductState c12_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(30,11)));
			ProductState c20_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(31,10)));
			ProductState c20_out = new ProductState("conveyor", new PhysicalProperty("Test2"), new PhysicalProperty(new Point(39,10)));
			
			ProductState swch_04_arr = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,20)));
			ProductState swch_04_finM2 = new ProductState("conveyor", new PhysicalProperty("switchedM2")
					,new PhysicalProperty(new Point(40,20)));
			ProductState swch_04_finM4 = new ProductState("conveyor", new PhysicalProperty("switchedM4")
					,new PhysicalProperty(new Point(40,20)));
			ProductState swch_05_arr = new ProductState("conveyor", new PhysicalProperty("Test1"), new PhysicalProperty(new Point(30,20)));
			ProductState swch_09_arr = new ProductState("conveyor", null, new PhysicalProperty(new Point(30,10)));
			
			//Module 3
			ProductState c06_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(41,30)));
			ProductState c06_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(49,30)));
			ProductState c07_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(50,29)));
			ProductState c07_out = new ProductState("conveyor", new PhysicalProperty("Test1"), new PhysicalProperty(new Point(50,21)));
			ProductState c09_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(51,20)));
			ProductState c09_out = new ProductState("conveyor", new PhysicalProperty("Test3"), new PhysicalProperty(new Point(59,20)));
			ProductState c08_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(60,19)));
			ProductState c08_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(60,11)));
			ProductState c22_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(59,10)));
			ProductState c22_out = new ProductState("conveyor", new PhysicalProperty("Test2"), new PhysicalProperty(new Point(41,10)));	
			ProductState c21_in = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,9)));
			ProductState c21_out = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,0)));
			
			ProductState swch_03_arr = new ProductState("conveyor", null, new PhysicalProperty(new Point(50,20)));
			ProductState swch_03_finM3 = new ProductState("conveyor", new PhysicalProperty("switchedM3")
					,new PhysicalProperty(new Point(50,20)));
			ProductState swch_03_finM4 = new ProductState("conveyor", new PhysicalProperty("switchedM4")
					,new PhysicalProperty(new Point(50,20)));
			ProductState swch_10_arr = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,10)));
			ProductState swch_10_finM3 = new ProductState("conveyor", new PhysicalProperty("switchedM3")
					,new PhysicalProperty(new Point(40,10)));
			
			//Module 4
			ProductState c10_M2 = new ProductState("conveyor", null, new PhysicalProperty(new Point(41,20)));
			ProductState c10_M3 = new ProductState("conveyor", null, new PhysicalProperty(new Point(49,20)));
						
			// Populate all of the edges
					
			HashMap<String, ProductState[]> conveyorEndPoints = new HashMap<String, ProductState[]>();

			//Module 1
			conveyorEndPoints.put(edgeName[0][0], new ProductState[] {c18_out,swch_08_arr}); // Conveyor 18
			conveyorEndPoints.put(edgeName[1][0], new ProductState[] {swch_08_arr,c17_out}); // Conveyor 17
			conveyorEndPoints.put(edgeName[2][0], new ProductState[] {c17_out,c16_out}); // Conveyor 16
			conveyorEndPoints.put(edgeName[3][0], new ProductState[] {c16_out,swch_07_arr}); // Conveyor 15
			conveyorEndPoints.put(edgeName[4][0], new ProductState[] {swch_07_arr,c01_out}); // Conveyor 01
			conveyorEndPoints.put(edgeName[5][0], new ProductState[] {c01_out,swch_01_arr}); // Conveyor 02
			conveyorEndPoints.put(edgeName[6][0], new ProductState[] {swch_01_arr,swch_02_arr}); // Conveyor 04
			conveyorEndPoints.put(edgeName[6][1], new ProductState[] {swch_02_arr,swch_02_finM2}); // Conveyor 04 to M2
			conveyorEndPoints.put(edgeName[6][2], new ProductState[] {swch_02_arr,swch_02_finM3}); // Conveyor 04 to M3
				
			//Module 2
			conveyorEndPoints.put(edgeName[7][0], new ProductState[] {swch_02_finM2,c05_out}); // Conveyor 05
			conveyorEndPoints.put(edgeName[7][1], new ProductState[] {c05_out,swch_04_finM2}); // Conveyor 05M2
			conveyorEndPoints.put(edgeName[7][2], new ProductState[] {c05_out,swch_04_finM4}); // Conveyor 05M4
			conveyorEndPoints.put(edgeName[8][0], new ProductState[] {swch_04_finM2,swch_05_arr}); // Conveyor 11
			conveyorEndPoints.put(edgeName[9][0], new ProductState[] {swch_05_arr,swch_09_arr}); // Conveyor 12
			conveyorEndPoints.put(edgeName[10][0], new ProductState[] {swch_09_arr,c20_out}); // Conveyor 20
			
			//Module 3
			conveyorEndPoints.put(edgeName[11][0], new ProductState[] {swch_02_finM3,c06_out}); // Conveyor 06
			conveyorEndPoints.put(edgeName[12][0], new ProductState[] {c06_out,c07_out}); // Conveyor 07
			conveyorEndPoints.put(edgeName[12][1], new ProductState[] {c07_out,swch_03_finM3}); // Conveyor 07M3
			conveyorEndPoints.put(edgeName[12][2], new ProductState[] {c07_out,swch_03_finM4}); // Conveyor 07M4
			conveyorEndPoints.put(edgeName[12][3], new ProductState[] {c10_M3,swch_03_finM3}); // Conveyor 07M4M3
			conveyorEndPoints.put(edgeName[13][0], new ProductState[] {swch_03_finM3,c09_out}); // Conveyor 09
			conveyorEndPoints.put(edgeName[14][0], new ProductState[] {c09_out,c08_out}); // Conveyor 08
			conveyorEndPoints.put(edgeName[15][0], new ProductState[] {c08_out,c22_out}); // Conveyor 22
			conveyorEndPoints.put(edgeName[16][0], new ProductState[] {swch_10_finM3,end}); // Conveyor 21
			conveyorEndPoints.put(edgeName[16][1], new ProductState[] {c22_out,swch_10_finM3}); // Conveyor 22RM3
			conveyorEndPoints.put(edgeName[16][2], new ProductState[] {c20_out,swch_10_finM3}); // Conveyor 22RM2
			
							
			//Module 4
			conveyorEndPoints.put(edgeName[17][0], new ProductState[] {swch_04_finM4,c10_M2}); // Conveyor 10_M2M4
			conveyorEndPoints.put(edgeName[17][1], new ProductState[] {c10_M2,c10_M3}); // Conveyor 10_M4M3
			conveyorEndPoints.put(edgeName[17][2], new ProductState[] {swch_03_finM4,c10_M3}); // Conveyor 10_M3M4
			conveyorEndPoints.put(edgeName[17][3], new ProductState[] {c10_M3,c10_M2}); // Conveyor 10_M4M2
			
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
				{"c18_out","swch_08_arr"},{"c17_out"},{"c16_out"},{"swch_07_arr"},{"c01_out"},{"swch_01_arr"},{"swch_02_arr","swch_02_finM2","swch_02_finM3"},
					{"c05_out","swch_04_finM2","swch_04_finM4"},{"swch_05_arr"},{"swch_09_arr"},{"c20_out"},
						{"c06_out"},{"c07_out","swch_03_finM3","swch_03_finM4"},{"c09_out"},{"c08_out"},{"c22_out"},{"c21_in","swch_10_finM3"},
							{"c10_M2","c10_M3"}};
				
			ProductState[][] statesForRAs= new ProductState[][] {
				{c18_out,swch_08_arr},{c17_out},{c16_out},{swch_07_arr},{c01_out},{swch_01_arr},{swch_02_arr,swch_02_finM2,swch_02_finM3},
					{c05_out,swch_04_finM2,swch_04_finM4},{swch_05_arr},{swch_09_arr},{c20_out},
						{c06_out},{c07_out,swch_03_finM3,swch_03_finM4},{c09_out},{c08_out},{c22_out},{end,swch_10_finM3},
							{c10_M2,c10_M2}};
				
							
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