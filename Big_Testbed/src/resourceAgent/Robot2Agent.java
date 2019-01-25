package resourceAgent;

import java.util.HashMap;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import javafish.clients.opc.ReadWriteJADE;
import resourceAgent.Robot1Agent.resetSignal;
import sharedInformation.RequestSchedule;
import sharedInformation.ResourceEvent;

public class Robot2Agent extends ResourceAgent{
	private HashMap<String, String> varToPLC = new HashMap<>();
	ReadWriteJADE plcConnection;
	public Robot2Agent() { //Mapping PLC tags to Robot 2 events
		varToPLC.put("PerformEvent_conv2_cnc3", "Fanuc_Rbt_C2:O.Data[0].0");
		varToPLC.put("PerformEvent_cnc3_conv2", "Fanuc_Rbt_C2:O.Data[0].2");
		varToPLC.put("PerformEvent_cnc4_conv2", "Fanuc_Rbt_C2:O.Data[0].3");
		varToPLC.put("PerformEvent_conv2_cnc4", "Fanuc_Rbt_C2:O.Data[0].1");
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
		plcConnection.uninit();
		System.out.println("DELETED: "+getAID().getLocalName());
	}
}
