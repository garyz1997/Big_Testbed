package initializingAgents;

import java.awt.Point;
import java.io.IOException;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.tools.testagent.ReceiveCyclicBehaviour;
import jade.tools.testagent.TestAgent;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import sharedInformation.CapabilitiesTable;
import sharedInformation.PhysicalProperty;
import sharedInformation.ProductState;
import sharedInformation.SetupProductAgent;
import sharedInformation.StartingPAParams;

public class InitializeProductAgent extends Agent {

	private static final long serialVersionUID = 6601315432329573157L;
	//private static int count = 0; ##never used
	private StartingPAParams startingPAParams;

	public InitializeProductAgent() {
	}

	protected void setup() {
		System.out.println("CREATED: InitializeProductAgent "+getAID().getLocalName());
		addBehaviour(new PASetup());
	}
	
	private class PASetup extends CyclicBehaviour{

		private static final long serialVersionUID = 8099183790582282038L;
		
		@Override
		public void action() {
			//String outputNeighbor = ""; //to output in the console ##never used

			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);		
			if(msg != null){
				try {
					//If this is a capabilities table (RA and Capabilities pair)
					if (msg.getContentObject().getClass().getName().contains("StartingPAParams")) {
						
						startingPAParams = (StartingPAParams) msg.getContentObject();
						
						ProductionPlan pp = getProductionPlan();
						String paName = "(PA)" + startingPAParams.getID().substring(4);
						
						AgentController ac;
						try {
							ac = getContainerController().createNewAgent(paName, "productAgent.ProductAgent", new Object[] {"a1"});
							ac.start();
						} catch (StaleProxyException e) { e.printStackTrace();}
						
						myAgent.doWait(2000);
						
						SetupProductAgent spa = new SetupProductAgent(startingPAParams.getResourceAgent(), startingPAParams.getState(),
								1, pp, new ExitPlan(null, null));
						
						ACLMessage startMsg = new ACLMessage(ACLMessage.INFORM);
						startMsg.addReceiver(new AID(paName,AID.ISLOCALNAME));
						try {startMsg.setContentObject(spa);}
						catch (IOException e) {e.printStackTrace();}
						send(startMsg);
						
						myAgent.doDelete();
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
					myAgent.doDelete();
				}
			}
			else{
				block();
			}
			
		}

		private ProductionPlan getProductionPlan() {
			ProductionPlan pp = new ProductionPlan();

			Random rand = new Random();
			//String paramID = startingPAParams.getID();
			//Boolean randomSet = paramID[paramID.length()-1].equals("3");
			System.out.println(startingPAParams.getID().length());
			if (startingPAParams.getID().length() == 14 && startingPAParams.getID().charAt(startingPAParams.getID().length()-1)=='0') {//randomize part production plans TODO: remove
				pp.addNewSet(new PhysicalProperty("p3"));
			}

			pp.addNewSet(new PhysicalProperty("end"));
			
			//System.out.println("Production plan for " + startingPAParams.getID()+":"+pp);
			return pp;
		}
	}
	
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("DELETED: "+getAID().getLocalName());
	}

}