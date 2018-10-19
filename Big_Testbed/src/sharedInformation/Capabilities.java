package sharedInformation;

import java.util.HashMap;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class Capabilities extends DirectedSparseGraph<ProductState,ResourceEvent>{
	
	private static final long serialVersionUID = -2137412673419126879L;

	public void addEdge(ResourceEvent... edges) {
		for(ResourceEvent edge: edges) {
			this.addEdge(edge, edge.getParent(), edge.getChild());
		}
	}
	
}