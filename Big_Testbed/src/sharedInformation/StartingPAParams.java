package sharedInformation;

import java.io.Serializable;

import jade.core.AID;

public class StartingPAParams implements Serializable {

	private AID resourceAgent;
	private ProductState state;
	private String ID;
	
	public StartingPAParams(AID resourceAgent, ProductState state, String string) {
		this.resourceAgent = resourceAgent;
		this.state = state;
		this.ID = string;
	}

	public AID getResourceAgent() {
		return resourceAgent;
	}

	public ProductState getState() {
		return state;
	}

	public String getID() {
		return ID;
	}
	
	
	
}