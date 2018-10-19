package sharedInformation;

import java.io.Serializable;
import java.util.ArrayList;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class SystemOutput implements Serializable{

	private static final long serialVersionUID = 2614788057434786149L;
	private ArrayList<ResourceEvent> occuredEvents;
	private ProductState currentState;
	private DirectedSparseGraph<ProductState, ResourceEvent> graph;

	/**
	 * @param graph
	 * @param currentState
	 * @param occuredEvents
	 */
	public SystemOutput(DirectedSparseGraph<ProductState,ResourceEvent> graph, ProductState currentState,
			ArrayList<ResourceEvent> occuredEvents) {
		this.graph = graph;
		this.currentState = currentState;
		this.occuredEvents = occuredEvents;
	}

	public ArrayList<ResourceEvent> getOccuredEvents() {
		return occuredEvents;
	}

	public ProductState getCurrentState() {
		return currentState;
	}

	public DirectedSparseGraph<ProductState, ResourceEvent> getGraph() {
		return graph;
	}
}