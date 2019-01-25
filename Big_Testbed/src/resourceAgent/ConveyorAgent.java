package resourceAgent;

import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import javafish.clients.opc.ReadWriteJADE;
import resourceAgent.Robot1Agent.resetSignal;
import sharedInformation.RequestSchedule;
import sharedInformation.ResourceEvent;

public class ConveyorAgent extends ResourceAgent{

	private static final long serialVersionUID = 7080006178159083214L;
	private HashMap<String, String> varToPLC = new HashMap<>();
	ReadWriteJADE plcConnection;
	
	public ConveyorAgent() { //Mapping the PLC tags to the Conveyor events
		varToPLC.put("PerformEvent_conv1_conv2", "C1HoldBack.Ret");
		varToPLC.put("PerformEvent_conv2_conv3", "C2HoldBack.Ret");
		varToPLC.put("PerformEvent_conv3_conv1", "C3HoldBack.Ret");
		varToPLC.put("PerformEvent_conv3_end", "t1.DN");
		
	}
	
	@Override
	public void runEdge(ResourceEvent edge, AID productAgent) {
		plcConnection = new ReadWriteJADE();
		String variableName = varToPLC.get(edge.getActiveMethod().split(",")[0]);
		String variableSet = edge.getActiveMethod().split(",")[1];
		System.out.println(plcConnection.writeTag(variableName,Integer.parseInt(variableSet)));

		System.out.println(variableName+variableSet);
		
		plcConnection.uninit();
		addBehaviour(new resetSignal(this, edge.getEventTime()+1500, edge));

	}
	
	public class resetSignal extends WakerBehaviour {
		private final ResourceEvent desiredEdge;

		public resetSignal(Agent a, long timeout, ResourceEvent desiredEdge) {
			super(a, timeout);
			this.desiredEdge = desiredEdge;
		}
		
		protected void onWake() {		
			plcConnection = new ReadWriteJADE();
			String variableName = varToPLC.get(desiredEdge.getActiveMethod().split(",")[0]);
			Integer variableSet = 0;
			System.out.println(plcConnection.writeTag(variableName,variableSet));
			plcConnection.uninit();
		}
	}

	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("DELETED: "+getAID().getLocalName());
	}
}