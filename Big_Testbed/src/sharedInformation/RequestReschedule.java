package sharedInformation;

import java.io.Serializable;

import jade.core.AID;

public class RequestReschedule  implements Serializable{

	private static final long serialVersionUID = -5552344911236880028L;
	AID productAgent;
	int startTime;
	int endTime;
	
	public RequestReschedule(AID productAgent, int startTime, int endTime) {
		super();
		this.productAgent = productAgent;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public AID getProductAgent() {
		return productAgent;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getEndTime() {
		return endTime;
	}
	
	
}