package resourceAgent;

import java.util.HashMap;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javafish.clients.opc.ReadWriteJADE;
import resourceAgent.Robot1Agent.resetSignal;
import sharedInformation.RequestSchedule;
import sharedInformation.ResourceEvent;

public class Robot2Agent extends ResourceAgent{
	private HashMap<String, String> varToPLC = new HashMap<>();
	ReadWriteJADE plcConnection;
	public Robot2Agent() { //Mapping PLC tags to Robot 2 events
		varToPLC.put("PerformEvent_conv2_cnc3p3", "CONV2_CNC3");
		varToPLC.put("PerformEvent_cnc3p3_conv2", "CNC3_CONV2");
		varToPLC.put("PerformEvent_cnc4p4_conv2", "CNC4_CONV2");
		varToPLC.put("PerformEvent_conv2_cnc4p4", "CONV2_CNC4");
		String[] tags = {"CONV2_CNC3","CNC3_CONV2","CNC4_CONV2","CONV2_CNC4"};
	}
	/*
	protected void setup() {
		String[] tags = {"Fanuc_Rbt_C2:O.Data[0].0","Fanuc_Rbt_C2:O.Data[0].2","Fanuc_Rbt_C2:O.Data[0].3","Fanuc_Rbt_C2:O.Data[0].1"};
		String registerMessage = "["+this.getLocalName()+"] Trying to register ";
		for (String tag : tags)
		{
			registerMessage += tag + ", ";
		}
		System.out.println(registerMessage);
		//plcConnection = new ReadWriteJADE(tags);
	}
	*/

	@Override
	public void runEdge(ResourceEvent edge, AID productAgent) {
		String variableName = varToPLC.get(edge.getActiveMethod().split(",")[0]);
		String variableSet = edge.getActiveMethod().split(",")[1];
		
		//edit::: System.out.println(plcConnection.writeTag(variableName,Integer.parseInt(variableSet)));
		//MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE); //OPCAGENT template
		// Request Tag reading value from OPC Agent
		ACLMessage reqWriteTag = new ACLMessage(ACLMessage.REQUEST);
		reqWriteTag.setSender(this.getAID());
		reqWriteTag.addReceiver(new AID("OPCLayer", AID.ISLOCALNAME));
		reqWriteTag.setContent(variableName);
		reqWriteTag.setOntology("Write"); // so that OPC Agent knows that the request is a read tag request
		reqWriteTag.setConversationId("robot2-agent-write");
		//reqReadTag.setReplyWith("read" + System.currentTimeMillis()); //to make the request unique
		System.out.println("Sending write request for the tag: " + variableName + ".....");
		this.send(reqWriteTag);
		System.out.println("Sent write request for the tag: " + variableName);
		 // wait until a reply is received TODO: MIGHT NEED TO SEPARATE THIS OUT INTO A CYCLIC BEHAVIOR THAT DETECTS IF A MESSAGE IS RECEIVED
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
		ACLMessage response = null;
		boolean done = false;
		 while (!done) { //  SKETCHY PARTS HERE
			 response = receive(mt);
			 if (response == null) {
				 continue;
			 }
			 if (response.getOntology() == "Write") {
				 String tag = response.getContent();
				 if (response.getContent().equals("1")) {
					 System.out.println("Tag value for " + variableName + " written ");
				 }
				 else {
					 System.out.println("Tag value for " + variableName + " could not be written ");
				 }
				 done = true;
			 }
			 else {
				 putBack(response);
			 }
		 }
		 System.out.println("[Robot2Agent] Completed event");
		//plcConnection.uninit();
		addBehaviour(new resetSignal(this, edge.getEventTime()+1500, edge));
	}
	
	public class resetSignal extends WakerBehaviour {
		private final ResourceEvent desiredEdge;

		public resetSignal(Agent a, long timeout, ResourceEvent desiredEdge) {
			super(a, timeout);
			this.desiredEdge = desiredEdge;
		}
		
		protected void onWake() {		
			String variableName = varToPLC.get(desiredEdge.getActiveMethod().split(",")[0]);
			Integer variableSet = 0;
			//System.out.println(plcConnection.writeTag(variableName,variableSet));
			//plcConnection.uninit();
		}
	}
	protected void takeDown() {
		// Printout a dismissal message
		//plcConnection.uninit();
		System.out.println("DELETED: "+getAID().getLocalName());
	}
}
