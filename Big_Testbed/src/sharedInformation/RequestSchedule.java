package sharedInformation;

import java.io.Serializable;

import jade.core.AID;

public class RequestSchedule implements Serializable{

	private static final long serialVersionUID = -6068368355365227748L;
	AID productAgent;
	ResourceEvent edge;
	int startTime;
	int endTime;
	
	public RequestSchedule(AID productAgent, ResourceEvent edge, int startTime, int endTime) {
		super();
		this.productAgent = productAgent;
		this.edge = edge;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public AID getProductAgent() {
		return productAgent;
	}

	public ResourceEvent getEdge() {
		return edge;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}
	
	
}