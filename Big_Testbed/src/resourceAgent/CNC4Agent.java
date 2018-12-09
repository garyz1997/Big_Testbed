package resourceAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import sharedInformation.RequestSchedule;
import sharedInformation.ResourceEvent;

public class CNC4Agent extends ResourceAgent{


	@Override
	public void runEdge(ResourceEvent edge, AID productAgent) {
		String variableName = edge.getActiveMethod().split(",")[0];
		String variableSet = edge.getActiveMethod().split(",")[1];
		
		// TODO: set PLC tag value {variablename, variableSet}
		

		System.out.println(variableName+variableSet);
		
		addBehaviour(new resetSignal(this, edge.getEventTime()+1500, edge));
	}
	
	public class resetSignal extends WakerBehaviour {
		private final ResourceEvent desiredEdge;

		public resetSignal(Agent a, long timeout, ResourceEvent desiredEdge) {
			super(a, timeout);
			this.desiredEdge = desiredEdge;
		}
		
		protected void onWake() {		
			String variableName = desiredEdge.getActiveMethod().split(",")[0];
			Integer variableSet = 0;
			
			// TODO: set PLC tag value {variablename, variableSet}
		}
	}
}