package initializingAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import resourceAgent.ConveyorAgent;
import resourceAgent.ResourceAgent;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import sharedInformation.Capabilities;
import sharedInformation.CapabilitiesTable;
import sharedInformation.PhysicalProperty;
import sharedInformation.ProductState;
import sharedInformation.ResourceEvent;
import sharedInformation.SetupProductAgent;

public class InitializeAgents extends Agent {
	
	private static final long serialVersionUID = 6601315432329573157L;

	public InitializeAgents() {}
	
	protected void setup() {
		this.addBehaviour(new RASetup());
	}
	
	private class RASetup extends OneShotBehaviour{

		private static final long serialVersionUID = 6736634843017599144L;

		@Override
		public void action() {
			
			//Input IPS and PLC Port
			Object[] ips = {"123","421","143","342"};
			String PLCPort = "851";
			
			// Creating agents
			ArrayList<AID> resourceAgents = new ArrayList<AID>();
			ArrayList<Capabilities> raCapabilities = new ArrayList<Capabilities>();
			for (int i=0; i<ips.length;i++) {
				//Name is going to start from 1
				String raName = "resourceAgent"+(i+1);
				
				//Create the agent
				try {
					AgentController ac = getContainerController().createNewAgent(raName, "resourceAgent.ConveyorAgent", new Object[] {ips[i],PLCPort});
					ac.start();
				} catch (StaleProxyException e) {
					e.printStackTrace();
				}
				
				//Record agent and RA capabilities to make setup faster
				resourceAgents.add(new AID(raName,AID.ISLOCALNAME));
				raCapabilities.add(new Capabilities());
			}
			
			//Wait a second
			myAgent.doWait(1000);
			
			// Populate all of the state
			ProductState s1 = new ProductState("conveyor", null, new PhysicalProperty(new Point(0,0)));
			ProductState s2 = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,30)));
			ProductState s3 = new ProductState("conveyor", null, new PhysicalProperty(new Point(41,30)));
			ProductState s4 = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,29)));
			ProductState s5 = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,20)));
			ProductState s6 = new ProductState("conveyor", null, new PhysicalProperty(new Point(41,20)));
			ProductState s7 = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,10)));
			ProductState s8 = new ProductState("conveyor", null, new PhysicalProperty(new Point(50,20)));
			ProductState s9 = new ProductState("conveyor", null, new PhysicalProperty(new Point(49,20)));
			ProductState s10 = new ProductState("conveyor", null, new PhysicalProperty(new Point(40,0)));
			
			// Create edges for RA1
			AID raPut = resourceAgents.get(0);
			ResourceEvent e1 = new ResourceEvent(raPut, s1, s2, "0", 110000);
			ResourceEvent e2 = new ResourceEvent(raPut, s2, s3, "1", 1000);
			ResourceEvent e3 = new ResourceEvent(raPut, s2, s4, "2", 1000);

			// Create edges for RA2
			raPut = resourceAgents.get(1);
			ResourceEvent e4 = new ResourceEvent(raPut, s4, s5, "0", 10000);
			ResourceEvent e5 = new ResourceEvent(raPut, s5, s6, "1", 5000);
			ResourceEvent e6 = new ResourceEvent(raPut, s6, s5, "2", 5000);
			ResourceEvent e7 = new ResourceEvent(raPut, s5, s7, "3", 30000);
			
			// Create edges for RA3
			raPut = resourceAgents.get(2);
			ResourceEvent e8 = new ResourceEvent(raPut, s3, s8, "0", 30000);
			ResourceEvent e9 = new ResourceEvent(raPut, s8, s9, "1", 5000);
			ResourceEvent e10 = new ResourceEvent(raPut, s8, s7, "2", 50000);
			ResourceEvent e11 = new ResourceEvent(raPut, s7, s10, "3", 10000);
			
			// Create edges for RA4
			raPut = resourceAgents.get(3);
			ResourceEvent e12 = new ResourceEvent(raPut, s6, s9, "0", 20000);
			ResourceEvent e13 = new ResourceEvent(raPut, s9, s6, "1", 20000);
			ResourceEvent e14 = new ResourceEvent(raPut, s6, s6, "2", 1000);
			
			// Populate all of the capabilities
			raCapabilities.get(0).addEdge(e1,e2,e3);
			raCapabilities.get(1).addEdge(e4,e5,e6,e7);
			raCapabilities.get(2).addEdge(e8,e9,e10,e11);
			raCapabilities.get(3).addEdge(e12,e13,e14);
			CapabilitiesTable allCapabilities = new CapabilitiesTable();

			// Send out capabilities to RAs
			for (int i=0; i<resourceAgents.size();i++) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(resourceAgents.get(i));
				
				try { msg.setContentObject(raCapabilities.get(i));}
				catch (IOException e) {e.printStackTrace();}
				send(msg);

				// Populate capabilties table
				allCapabilities.put(resourceAgents.get(i), raCapabilities.get(i));
			}
			
			//Wait a couple of seconds
			myAgent.doWait(2000);
			
			// Let RAs figure out their neighbors
			for (int i=0; i<resourceAgents.size();i++) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(resourceAgents.get(i));
				
				try { msg.setContentObject(allCapabilities);}
				catch (IOException e) {e.printStackTrace();}
				send(msg);
			}
			
			//Wait a couple of seconds and initialize a test product agent
			myAgent.doWait(5000);
			AgentController ac;
			try {
				ac = getContainerController().createNewAgent("ProductAgent", "productAgent.ProductAgent", new Object[] {"a1"});
				ac.start();
			} catch (StaleProxyException e) { e.printStackTrace();}
			
			ProductionPlan pp = new ProductionPlan();
			pp.add(s6.getPhysicalProperties().get(0));
			SetupProductAgent spa = new SetupProductAgent(resourceAgents.get(0), s1, 1, pp, new ExitPlan(null, null));
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID("ProductAgent",AID.ISLOCALNAME));
			try { msg.setContentObject(spa);}
			catch (IOException e) {e.printStackTrace();}
			send(msg);
			
			myAgent.doDelete();
		}
	}	
	
	
	
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Setup agent is deleted.");
	}
}