package resourceAgent;

import connectionPLC.CallAdsFuncs;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import sharedInformation.RequestSchedule;
import sharedInformation.ResourceEvent;

public class ConveyorAgent extends ResourceAgent{

	private static final long serialVersionUID = 7080006178159083214L;
	
	@Override
	public void runEdge(ResourceEvent edge, AID productAgent) {
		String variableName = edge.getActiveMethod().split(",")[0];
		String variableSet = edge.getActiveMethod().split(",")[1];
		
		CallAdsFuncs caf = new CallAdsFuncs();
		caf.openPort(getAddr());
		caf.setIntValue(variableName, Integer.parseInt(variableSet));
		System.out.println(productAgent.getLocalName().substring(productAgent.getLocalName().length()-3, 
				productAgent.getLocalName().length())+",plc," + getCurrentTime());
		caf.closePort();
		

		System.out.println(variableName+variableSet);
		
		addBehaviour(new resetSignal(this, edge.getEventTime()+1500, edge));
	}
	
	public class resetSignal extends WakerBehaviour {
		private static final long serialVersionUID = 6800760293776896340L;
		private final ResourceEvent desiredEdge;

		public resetSignal(Agent a, long timeout, ResourceEvent desiredEdge) {
			super(a, timeout);
			this.desiredEdge = desiredEdge;
		}
		
		protected void onWake() {		
			String variableName = desiredEdge.getActiveMethod().split(",")[0];
			Integer variableSet = 0;
			
			CallAdsFuncs caf = new CallAdsFuncs();
			caf.openPort(getAddr());
			caf.setIntValue(variableName, variableSet);
			caf.closePort();
		}
	}
}