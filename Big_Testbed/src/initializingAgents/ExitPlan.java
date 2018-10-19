package initializingAgents;

import java.io.Serializable;

import resourceAgent.ResourceAgent;
import sharedInformation.ResourceEvent;

public class ExitPlan implements Serializable{
	private ResourceAgent exitRA;
	private ResourceEvent exitEvent;

	public ExitPlan(ResourceAgent exitRA, ResourceEvent exitEvent) {
		this.exitRA = exitRA;
		this.exitEvent = exitEvent;
	}
	
	public ResourceAgent getExitRA() {
		return exitRA;
	}

	public ResourceEvent getExitEvent() {
		return exitEvent;
	}

	@Override
	public String toString() {
		return "Exit Event: "+ this.exitEvent +  " - " + this.exitRA;
	}
}