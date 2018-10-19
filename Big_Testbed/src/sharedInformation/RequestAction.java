package sharedInformation;

import java.io.Serializable;

import jade.core.AID;

public class RequestAction implements Serializable{

	private static final long serialVersionUID = 2444200146397282610L;
	ResourceEvent queriedEdge;
	AID productAgent;
	
	public RequestAction(ResourceEvent queriedEdge, AID productAgent) {
		this.queriedEdge = queriedEdge;
		this.productAgent = productAgent;
	}

	public ResourceEvent getQueriedEdge() {
		return queriedEdge;
	}

	public AID getProductAgent() {
		return productAgent;
	}
}