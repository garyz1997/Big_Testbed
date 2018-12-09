
package resourceAgent;
import java.util.HashMap;
import java.util.Map;
//import connectionPLC.CallAdsFuncs;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import javafish.clients.opc.ReadWriteJADE;
import sharedInformation.RequestSchedule;
import sharedInformation.ResourceEvent;

public class Robot1Agent extends ResourceAgent{
	private HashMap<String, String> varToPLC = new HashMap<>();
	ReadWriteJADE plcConnection;
	public Robot1Agent() {
		varToPLC.put("PerformEvent_conv1_cnc1", "Fanuc_Rbt_C1:O.Data[0].1");
		varToPLC.put("PerformEvent_cnc1_conv1", "Fanuc_Rbt_C1:O.Data[0].3");
		varToPLC.put("PerformEvent_cnc2_conv1", "Fanuc_Rbt_C1:O.Data[0].2");
		varToPLC.put("PerformEvent_conv1_cnc2", "Fanuc_Rbt_C1:O.Data[0].0");
		
	}

	@Override
	public void runEdge(ResourceEvent edge, AID productAgent) {
		plcConnection = new ReadWriteJADE();
		String variableName = varToPLC.get(edge.getActiveMethod().split(",")[0]);
		String variableSet = edge.getActiveMethod().split(",")[1];
		// TODO: set PLC tag value {variablename, variableSet}
		/*
		CallAdsFuncs caf = new CallAdsFuncs();
		caf.openPort(getAddr());
		caf.setIntValue(variableName, Integer.parseInt(variableSet));
		System.out.println(productAgent.getLocalName().substring(productAgent.getLocalName().length()-3, 
				productAgent.getLocalName().length())+",plc," + getCurrentTime());
		caf.closePort();
		*/
		//plcConnection = new ReadWriteJADE();
		//System.out.println(plcConnection.readTag(variableName));
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
			/*
			CallAdsFuncs caf = new CallAdsFuncs();
			caf.openPort(getAddr());
			caf.setIntValue(variableName, variableSet);
			caf.closePort();
			*/
			plcConnection = new ReadWriteJADE();
			//System.out.println(plcConnection.readTag(variableName));
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