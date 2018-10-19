package sharedInformation;

import java.io.Serializable;

import initializingAgents.ExitPlan;
import initializingAgents.ProductionPlan;
import jade.core.AID;
import resourceAgent.ResourceAgent;

public class SetupProductAgent implements Serializable{

	
	private static final long serialVersionUID = -7832327704096506661L;
	AID startingResource;
	ProductState startingNode;
	int priority;
	ProductionPlan productionPlan;
	ExitPlan exitPlan;
	
	public SetupProductAgent(AID startingResource, ProductState startingNode, int priority,
			ProductionPlan productionPlan, ExitPlan exitPlan) {
		this.startingResource = startingResource;
		this.startingNode = startingNode;
		this.priority = priority;
		this.productionPlan = productionPlan;
		this.exitPlan = exitPlan;
	}

	public AID getStartingResource() {
		return startingResource;
	}

	public ProductState getStartingNode() {
		return startingNode;
	}

	public int getPriority() {
		return priority;
	}

	public ProductionPlan getProductionPlan() {
		return productionPlan;
	}

	public ExitPlan getExitPlan() {
		return exitPlan;
	}
}