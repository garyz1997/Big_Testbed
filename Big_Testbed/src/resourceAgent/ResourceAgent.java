package resourceAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

	//import connectionPLC.CallAdsFuncs;
	//import de.beckhoff.jni.tcads.AmsAddr;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import initializingAgents.ExitPlan;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import javafish.clients.opc.ReadWriteJADE;
import javafish.clients.opc.ReadWriteJADE2;
import sharedInformation.Bid;
import sharedInformation.Capabilities;
import sharedInformation.CapabilitiesTable;
import sharedInformation.PhysicalProperty;
import sharedInformation.ProductState;
import sharedInformation.RequestAction;
import sharedInformation.RequestBid;
import sharedInformation.RequestReschedule;
import sharedInformation.RequestSchedule;
import sharedInformation.ResourceEvent;
import sharedInformation.SetupProductAgent;
import sharedInformation.StartingPAParams;
import sharedInformation.SystemOutput;
import sharedInformation.WatchRAVariableTable;

public class ResourceAgent extends Agent {
	private static final long serialVersionUID = -8012073439178287377L;

	private boolean working;
	private String program;
	private int PAincrement;
	
	// Capabilities Graph
	private Capabilities resourceCapabilities;
	private ResourceEvent runningEdge;
	private Function<ResourceEvent, Integer> timeFunction;
	
	//Neighbors
	private Set<AID> neighbors;
	private HashMap<AID, ProductState> tableNeighborNode;
	
	//Scheduling
	private RASchedule RAschedule;

	//PLC Communication
		//private AmsAddr addr;
	//private ReadWriteJADE PLCconnection;

	//For initialization
	private boolean capabilitiesSet;
	private boolean neighborsSet;
	private boolean watchVariablesSet;

	private HashMap<ProductState,HashMap<AID,ResourceEvent>> notifyAgentWhenState 
		= new HashMap<ProductState, HashMap<AID, ResourceEvent>>();
	
	//private ReadWriteJADE2 plcConnection;
	

	public ResourceAgent() {}

	/**
	 * @param name
	 * @param PLCport
	 * @param tableLocationObject
	 * @param resourceCapabilities
	 */
	protected void setup(){
		System.out.println("[" + this.getLocalName()+"] CREATED: ResourceAgent "+getAID().getLocalName());
		doSuspend();//Suspend Agent upon creation. Resume Agent via GUI to start it up.
		//Initialize everything
		this.working = false;
		this.setResourceCapabilities(new Capabilities());
		this.runningEdge = null;
		this.RAschedule = new RASchedule(this);
		this.neighbors = new HashSet<AID>();
		this.tableNeighborNode = new HashMap<AID, ProductState>();
		
		this.PAincrement = 0;
			/* OLD PLC			
		// Get information about the PLC
		Object[] args = getArguments();

		if (args.length!=2) {
			System.out.println("Please add IP Address and PLC port to agent: " + this);
			this.doDelete();
		}
	
			String IPaddress = (String) args[0];
			int PLCport = Integer.parseInt((String) args[1]);
			this.addr= new AmsAddr();
			addr.setNetIdStringEx(IPaddress);
			addr.setPort(PLCport);
			*/
		//Start by waiting for capabilities and neighbors
		this.capabilitiesSet = false;
		this.neighborsSet = false;
		this.addBehaviour(new WaitForCapabilities());
		
		final WaitForNeighbors behNeighbors = new WaitForNeighbors(this,100);
		final WaitForWatchVariables behWatch = new WaitForWatchVariables(this,100);
		this.addBehaviour(behNeighbors);
		this.addBehaviour(behWatch);
		
		this.addBehaviour(new WakerBehaviour(this, 60000) {
			private static final long serialVersionUID = 1L;
			protected void onWake() {
		         myAgent.removeBehaviour(behNeighbors);
		         myAgent.removeBehaviour(behWatch);		         
		      }
		});
		
		//this.addBehaviour(new WaitForNeighbors(this,60000));
		this.addBehaviour(new WaitForStartup());
	} 

	protected void takeDown() {
		// Printout a dismissal message
		System.out.println(getAID().getName()+" terminating.");
		//plcConnection.uninit();
	}
	
	//================================================================================
    // Adding capabilities and neighbors for this resource
    //================================================================================
	
	/**
	 * Wait for resource capabilities. Remove behavior after capabilities are initialized.
	 * Set a directed sparse graph of capabilities, what edges the RA can fulfill and what Product States
	 * Maybe by communicating with the PA
	 */
	private class WaitForCapabilities extends SimpleBehaviour{ 
		private static final long serialVersionUID = 4863610712056640738L;
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){	
				try {
					//If the message are resource capabilities
					if (msg.getContentObject().getClass().getName().contains("Capabilities")) {
						System.out.println("[" + myAgent.getAID().getLocalName() + "] obtained capabilities message");
						//setResourceCapabilities set the capabilities of the RA on the graph
						setResourceCapabilities((Capabilities) msg.getContentObject());
						
						//Function for shortest path (time based)
						timeFunction = new Function<ResourceEvent,Integer>(){
							public Integer apply(ResourceEvent event){return event.getEventTime();}};  
						capabilitiesSet = true;
						
						System.out.println("Capabilities set for: " + myAgent.getAID().getLocalName());
					}
					else {
						System.out.println("waitforcapabilities putback " + msg.getOntology());
						putBack(msg);
						System.out.println("[" + myAgent.getAID().getLocalName() + "] putback message that was not capabilities");
					}
				} catch (UnreadableException e) {
					myAgent.doDelete(); e.printStackTrace();
				}
			}
			else{
				block();
			}
		}

		@Override
		public boolean done() {
			return capabilitiesSet;
		}
	}
	
	/**
	 * Action that waits for the set-up to let the RA know its neighbors. Neighbors have direct edge with the RA
	 * Note that you can keep running this action and keep adding other RAs when they enter the system
	 * Let all neighboring RAs know your own capabilities
	 */
	private class WaitForNeighbors extends TickerBehaviour{

		private static final long serialVersionUID = 4863610712056640738L;

		/**
		 * @param time 
		 * @param killAfterInitialize
		 */
		public WaitForNeighbors(Agent a,int time) {
			super(a,time);
		}

		@Override
		public void onTick() {
			String outputNeighbor = ""; //to output in the console
			CapabilitiesTable myCapTable = new CapabilitiesTable();
			myCapTable.put(myAgent.getAID(), getResourceCapabilities());
			boolean newneighborsAdded = false;

			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);		
			//to make sure it does not interfere with the messages OPCLayer sends to the specific RAs
			if(msg != null && !msg.getSender().getLocalName().equals("OPCLayer")){
				try {
					//If this is a capabilities table (RA and Capabilties pair)
					//System.out.println(msg.getSender().getLocalName());
					if (msg.getContentObject().getClass().getName().contains("CapabilitiesTable")) {
						System.out.println("[" + myAgent.getAID().getLocalName() + "] obtained capability table message");
						CapabilitiesTable capTable = (CapabilitiesTable) msg.getContentObject();
						
						//For each possible neighbor
						for (AID possibleNeighbor : capTable.keySet()) {
							//If the neighbor isn't the agent itself (don't re-add the same neighbor - can be changed)
							if (!possibleNeighbor.equals(myAgent.getAID()) && !tableNeighborNode.containsKey(possibleNeighbor)) {
								//Search through capabilities to see if possible neighbor's states correspond to the agent's states
								for (ProductState vertex : getResourceCapabilities().getVertices()) {
									if (capTable.get(possibleNeighbor).containsVertex(vertex)) {
										//If there is a match, add the RA in question to the agent's resources
										tableNeighborNode.put(possibleNeighbor, vertex);
										neighbors.add(possibleNeighbor);
										
										ACLMessage msgOut = new ACLMessage(ACLMessage.INFORM);
										msgOut.addReceiver(possibleNeighbor);
										try { msgOut.setContentObject(myCapTable);}
										catch (IOException e) {e.printStackTrace();}
										send(msgOut);
										
										newneighborsAdded = true;
										outputNeighbor = outputNeighbor + possibleNeighbor.getLocalName() + ", ";
									}
								}
							}
						}

						if (newneighborsAdded) {
							System.out.println("Neighbors for " + myAgent.getAID().getLocalName() + ": " + outputNeighbor.substring(0, outputNeighbor.length()-2));
						}
						neighborsSet = true;	

					}
					else {
						System.out.println("waitforneighbors " + msg.getOntology());
						putBack(msg);
						System.out.println("[" + myAgent.getAID().getLocalName() + "] putback message that was not neighbors");
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
					myAgent.doDelete();
				}
			}
		}
	}
	
	/**
	 * Waiting to set up the variables for sensing
	 */
	private class WaitForWatchVariables extends TickerBehaviour{

		public WaitForWatchVariables(Agent a, long period) {
			super(a, period);
		}

		private static final long serialVersionUID = 1394147355767330332L;

		@Override
		public void onTick() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);		
			if(msg != null && !msg.getSender().getLocalName().equals("OPCLayer")){
				try {
					if (msg.getContentObject().getClass().getName().contains("WatchRAVariableTable")) {
						System.out.println("[" + myAgent.getAID().getLocalName() + "] obtained watchRA variable table");
						// Set up the mapping from a variable to a product state
						WatchRAVariableTable watchVariableTable = (WatchRAVariableTable) msg.getContentObject();				
						String[] tags = new String[watchVariableTable.getStateMapping().keySet().size()];
						int count = 0;
						for (String key:watchVariableTable.getStateMapping().keySet()) {
							/* OLD PLC
							CallAdsFuncs caf = new CallAdsFuncs();
							caf.openPort(addr);
							caf.setBoolValue(key,Boolean.FALSE);
							caf.closePort();
							*/
							/*
							//new PLC
							ReadWriteJADE plcConnection = new ReadWriteJADE();
							System.out.println(plcConnection.writeTag(key,0));
							plcConnection.uninit();
							//end new PLC
							 */
							tags[count] = key;
							count++;
							addBehaviour(new PhysicalSystemMonitoring(myAgent, key,
									watchVariableTable.getStateMapping().get(key),
										watchVariableTable.getMonitorPeriodMapping().get(key),
											watchVariableTable.getInitializeMapping().get(key),
												watchVariableTable.getCreatePeriod().get(key)));
						}						
						//System.out.println(this.getAgent().getLocalName() + " created new PLC connection");
						/*
						for (String tag : tags) {
							System.out.println(myAgent.getAID().getLocalName()+ " " + tag);
						}
						*/
						//plcConnection = new ReadWriteJADE2(tags);
						watchVariablesSet = true;
						System.out.println("Watch variables set for: " + myAgent.getAID().getLocalName());
					}

					else {
						System.out.println("waitforwatchvariables " + msg.getOntology());
						putBack(msg);
						System.out.println("[" + myAgent.getAID().getLocalName() + "] putback message that was not watch variables");
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			}
			else{
				block();
			}
		}
	}
	
	/**
	 * Once everything has been initialized, wait for products to come into the system
	 */
	private class WaitForStartup extends SimpleBehaviour{
		private static final long serialVersionUID = 6492940778914568927L;
		private boolean finished = false;

		@Override
		public void action() {
			if (capabilitiesSet & neighborsSet & watchVariablesSet) {
				addBehaviour(new AgentCooperation());
				this.finished = true;
			}
			else {
				block();
			}
		}

		@Override
		public boolean done() {
			return this.finished;
		}
		
	}

	/**
	 * Allows for cooperation with the product agents
	 */
	private class AgentCooperation extends CyclicBehaviour{

		private static final long serialVersionUID = 27724142029603547L;

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			//System.out.println("["+myAgent.getLocalName()+"] got request");
			String response = "False";
			if(msg != null){	
				try {
					if (msg.getContentObject().getClass().getName().contains("RequestAction")) {
						query((RequestAction) msg.getContentObject());
						System.out.println(msg.getSender().getLocalName() +" requests from "+ myAgent.getLocalName()+":"+((RequestAction) msg.getContentObject()).getQueriedEdge());
						//response = "True";
					}
					else if (msg.getContentObject().getClass().getName().contains("RequestBid")) {
						new teamQuery((RequestBid) msg.getContentObject(),msg.getSender());
						respond(msg,"True");
					}
					else if (msg.getContentObject().getClass().getName().contains("RequestSchedule")) {
						requestScheduleTime((RequestSchedule) msg.getContentObject());
						//response = "True";
					}
					else if (msg.getContentObject().getClass().getName().contains("RequestReschedule")) {
						removeScheduleTime((RequestReschedule) msg.getContentObject());
						//response = "True";
					}

					else {
						putBack(msg);
						System.out.println("[" + myAgent.getAID().getLocalName() + "] putback message that was not requests");
					}
					
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			}
			else{
				block();
			}
		}

		private void respond(ACLMessage msg, String string) {
			//ACLMessage reply = msg.createReply();
            //reply.setPerformative( ACLMessage.INFORM );
            //reply.setContent(response);
            //send(reply);
		}
		
	}
	
	/**
	 * Monitoring the physical system to communicate with the PAs if a sensor is triggered
	 */
	private class PhysicalSystemMonitoring extends TickerBehaviour{

		private static final long serialVersionUID = -3562983232362586844L;
		private final String variable;
		private final ProductState productState;
		private final Boolean createNew;
		private final Integer createPeriod;
		private Integer lastCreated = 19001;
		private Integer readTimer = 10000000;
		private Integer monitorPeriod;

		public PhysicalSystemMonitoring(Agent a, String variable, ProductState productState, Integer monitorPeriod, Boolean createNew, Integer createPeriod) {
			super(a, monitorPeriod);
			this.monitorPeriod = monitorPeriod;
			this.variable = variable;
			this.productState = productState;
			this.createNew = createNew;		
			this.createPeriod = createPeriod;
		}

		@Override
		protected void onTick() {
			lastCreated = lastCreated+1;
			readTimer = readTimer + 1;
			/* OLD PLC
			CallAdsFuncs caf = new CallAdsFuncs();
			caf.openPort(addr);
			*/
			//new PLC
			//System.out.println("Variable: " + variable);
			
			//request info from OPC agent
			ACLMessage reqReadTag = new ACLMessage(ACLMessage.REQUEST);
			reqReadTag.setSender(myAgent.getAID());
			reqReadTag.addReceiver(new AID("OPCLayer", AID.ISLOCALNAME));
			reqReadTag.setContent(variable);
			reqReadTag.setOntology("Read"); // so that OPC Agent knows that the request is a read tag request
			reqReadTag.setConversationId("agent1-read");
			//reqReadTag.setReplyWith("read" + System.currentTimeMillis()); //to make the request unique
			//System.out.println("Sending read request for the tag: " + variable + ".....");
			myAgent.send(reqReadTag);
			//System.out.println("Sent read request for the tag: " + variable);
			
			//receive info from OPC agent
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			//System.out.println("lastCreated for agent " + myAgent.getLocalName() + "is " + lastCreated + " createperiod " + (createPeriod)); //check create delay
			ACLMessage response = receive(mt);
			if (response!=null) {
				if ( response.getSender().getLocalName().equals("OPCLayer") && response.getOntology().equals(variable)) {
					Boolean varValue = Boolean.valueOf(response.getContent());
					//System.out.println("Tag value received for tag " + response.getOntology() + ": " + varValue + " Content: " + response.getContent());
					//operate on the response
					//System.out.println("["+myAgent.getLocalName()+"] Checking " +variable+" "+readTimer+" "+10 * monitorPeriod+" "+varValue+" "+notifyAgentWhenState.containsKey(productState)+"keyset: "+notifyAgentWhenState.keySet()+ " "+ productState);
					if (readTimer > (10 * monitorPeriod) && varValue && notifyAgentWhenState.containsKey(productState)) {
						//Reset read timer
						readTimer = 0;
						//wait edgetime to inform PA
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//Reset the counter
						lastCreated = 0;
						//Message to inform the PA about the product state
						System.out.println("notifyAgentWhenState");
						for(AID productAgent:notifyAgentWhenState.get(productState).keySet()) {

							System.out.println("notifyAgentWhenState1");
							informPA(productAgent, notifyAgentWhenState.get(productState).get(productAgent));
							notifyAgentWhenState.remove(productState);
						}
					}
					
					else if (lastCreated > createPeriod && varValue && createNew) {
						System.out.println(""+myAgent.getLocalName()+ " is creating when lastCreated is " + lastCreated + "["+variable+"=" +varValue+" createperiod: "+ createPeriod +"]");
						lastCreated = 0;
						String ipaName = "(initPA)" + myAgent.getAID().getLocalName() + "_" + variable;
						//System.out.println(ipaName);
						//Get the Agent controller
						AgentController ac;
						try {
							ac = getContainerController().createNewAgent(ipaName, "initializingAgents.InitializeProductAgent",
									new Object[] {});
							ac.start();
						} catch (StaleProxyException e) { e.printStackTrace();}
						
						//Creating a new PA starter
						//System.out.println(myAgent.getLocalName());
						String uniqueRAIdentifier = myAgent.getLocalName().replaceAll("resourceAgent", "");// TODO: maybe can remove replaceALL
						
						StartingPAParams spa = new StartingPAParams(myAgent.getAID(), productState,
								uniqueRAIdentifier+"-"+PAincrement);
						PAincrement++;
						//System.out.println(PAincrement);
						
						//Message to initialize the PA
						ACLMessage startMsg = new ACLMessage(ACLMessage.INFORM);
						startMsg.addReceiver(new AID(ipaName,AID.ISLOCALNAME));
						try { startMsg.setContentObject(spa);}
						catch (IOException e) {e.printStackTrace();}
						send(startMsg);
						
					}
				}
				else {
					//System.out.println("physicalsystemmonitoring " + response.getOntology());
					putBack(response);
				}				
			}
			
		
			
			
			//String varValueStr = plcConnection.readTag(variable);
			//String varValueStr = "random string";
			//end new PLC
			//System.out.println("lastCreated: " + lastCreated + "createperiod: " +createPeriod);
			//only read after 10 * monitorPeriod time after last read
			
			//lastCreated= lastCreated+this.monitorPeriod;
			/* OLD PLC
			caf.setBoolValue(variable,Boolean.FALSE);
			caf.closePort();
			*/
			/*
			//new PLC
			plcConnection = new ReadWriteJADE();
			System.out.println(plcConnection.writeTag(variable,0));
			plcConnection.uninit();
			//end new PLC
			 *
			 */
		}
		
		
	}
	
	//================================================================================
    // Product/resource team formation
    //================================================================================

	private class teamQuery{
	
		public teamQuery(RequestBid bidRequest, AID requestor) {
			
			//Deep copy everything
			Bid currentBid = bidRequest.getBid();
			ProductState currentState = bidRequest.getCurrentNode().copy();
			PhysicalProperty desiredProperty = bidRequest.getDesiredProperty();
			int existingTime = bidRequest.getExistingBidTime();
			int maxTime = bidRequest.getMaxTime();
			AID productAgent = bidRequest.getProductAgent();		
			
			DirectedSparseGraph<ProductState,ResourceEvent> updatedCapabilities = copyGraph(getResourceCapabilities()); //need to update based on current schedule
			//Copy graphs to not mess with pointers
			Bid bid = currentBid.copyBid();
			DirectedSparseGraph<ProductState,ResourceEvent> searchGraph = currentBid.copyBid();
			
			// 1. Update events in capabilities based on current schedule
			// 2. Create new full graph (capabilities + bid)
			Iterator<ResourceEvent> itr = updatedCapabilities.getEdges().iterator();
			while (itr.hasNext()){
				// Find the edge and update it based on current schedule
				ResourceEvent edge = itr.next().copy();
				
				int bidOffset = getSchedule().getNextFreeTime(existingTime,edge.getEventTime())-existingTime;
	
				if (bidOffset != 0) {
					bidOffset+=1000;
				}
				edge.setWeight(edge.getEventTime()+bidOffset);
				
				//Add to entire graph
				searchGraph.addEdge(edge, edge.getParent(), edge.getChild());
			}
					
			DijkstraShortestPath<ProductState, ResourceEvent> shortestPathGetter = 
					new DijkstraShortestPath<ProductState, ResourceEvent>(searchGraph);
			shortestPathGetter.reset();
			
			//Check if a node in the capabilities graph satisfies a desired property
			boolean flag = false;
			ProductState desiredVertex = null;
			for (ProductState vertex : updatedCapabilities.getVertices()){
				if(vertex.getPhysicalProperties().contains(desiredProperty)){
					flag = true;
					desiredVertex = vertex;
					break;
				}
			}
			
			int currentTime = getCurrentTime();
			
			// If a vertex satisfied a desired property
			if (flag){
				
				//Find the shortest path
				shortestPathGetter.reset();
				List<ResourceEvent> shortestPathCandidateList = shortestPathGetter.getPath(currentState, desiredVertex);
						
				//Check if there is a path from the current node to the desired one
				if (!shortestPathCandidateList.isEmpty()) {
					//Calculate the bid
					int bidTime = existingTime;
					for (ResourceEvent path : shortestPathCandidateList){
						bidTime = bidTime + path.getEventTime();
						bid.addEdge(path, path.getParent(), path.getChild());
						bidRequest.setCurrentNode(path.getChild());
					}
					
					//Submit the bid to the product agent
					if (bidTime < currentTime + maxTime) {
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(productAgent);
						try {msg.setContentObject(bid);} 
						catch (IOException e) {e.printStackTrace();}
						send(msg);
					}	
				}
			}
			
			//Push the bid negotiation to a neighbor
			else{ //Note: if a desired node is found, don't push it to the neighbor (can be turned off)	
				for (AID neighbor: neighbors){
					
					if (!neighbor.equals(requestor)) {
						ProductState neighborNode = tableNeighborNode.get(neighbor);
						
						//Find the shortest path
						shortestPathGetter.reset();
						
						List<ResourceEvent> shortestPathCandidateList = new LinkedList<ResourceEvent>();
						if(searchGraph.containsVertex(currentState)) {
							shortestPathCandidateList = shortestPathGetter.getPath(currentState, neighborNode);
						}
						else {
							break;
						}
						
						//If the current state is the 1st neighbor state -> pass the bid on to the resource's neighbor
						if(shortestPathCandidateList.isEmpty() && bid.getEdgeCount()==0){
							//ResourceEvent selfEdge = new ResourceEvent(getAID(), neighborNode, neighborNode, "Hold", 0);
							//bid.addEdge(selfEdge, selfEdge.getParent(), selfEdge.getChild());
							
							RequestBid newBidRequest = new RequestBid(productAgent, desiredProperty, currentState,
									maxTime, bid, existingTime);
							
							ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
							msg.addReceiver(neighbor);
							try {msg.setContentObject(newBidRequest);} 
							catch (IOException e) {e.printStackTrace();}
							send(msg);
						}
						else
							
						//Don't revisit the same edges
						if(!bid.getEdges().containsAll(shortestPathCandidateList)){	
							//Calculate the bid
							int bidTime = existingTime; //Reset bid time
							for (ResourceEvent path : shortestPathCandidateList){
								bidTime = bidTime + path.getEventTime();
								bid.addEdge(path, path.getParent(), path.getChild());
							}
							
							ProductState newBidPartState = shortestPathCandidateList.get(shortestPathCandidateList.size()-1).getChild();
							
							// Request bids from the neighbors						
							if (bidTime < currentTime + maxTime && bidTime >= existingTime){
								RequestBid newBidRequest = new RequestBid(productAgent, desiredProperty, 
										newBidPartState, maxTime, bid, bidTime);
								
								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
								msg.addReceiver(neighbor);
								try {msg.setContentObject(newBidRequest);} 
								catch (IOException e) {e.printStackTrace();}
								send(msg);
							}
						}
					}
				}
			}
			
			//For garbage collector
			clearGraph(updatedCapabilities);
			updatedCapabilities = null;
			clearGraph(bid);
			bid = null;
			clearGraph(searchGraph);
			searchGraph = null;
		}
	}
	
	//================================================================================
    // Product agent scheduling
    //================================================================================
	
	public RASchedule getSchedule() {
		return this.RAschedule;
	}

	public boolean requestScheduleTime(RequestSchedule requestSchedule) {
		
		ResourceEvent edge = requestSchedule.getEdge();
		AID productAgent = requestSchedule.getProductAgent();
		int startTime = requestSchedule.getStartTime();
		int endTime = requestSchedule.getEndTime();
		
		int edgeOffset = edge.getEventTime() - this.getResourceCapabilities().findEdge(edge.getParent(),edge.getChild()).getEventTime();
		
		boolean planned = this.RAschedule.addPA(productAgent, startTime+edgeOffset, endTime, false);
		
		System.out.println(productAgent.getLocalName() + ",Scheduled," + getCurrentTime());
		return planned;
	}


	public boolean removeScheduleTime(RequestReschedule requestReschedule) {
		AID productAgent = requestReschedule.getProductAgent();
		int startTime = requestReschedule.getStartTime();
		int endTime = requestReschedule.getEndTime();
		
		return this.RAschedule.removePA(productAgent, startTime, endTime);
	}
	
	//================================================================================
    // Product agent communication
    //================================================================================

	public boolean query(RequestAction action) {
		ResourceEvent queriedEdge = action.getQueriedEdge();
		AID productAgent = action.getProductAgent();
		//Find the desired edge
		ResourceEvent desiredEdge = null;
		for (ResourceEvent edge : this.getResourceCapabilities().getEdges()){
			if (edge.getActiveMethod().equals(queriedEdge.getActiveMethod())){
				desiredEdge = edge;
				break;
			}
		}
		
		//Find the offset between the queried edge and when the actual program should be run
		int edgeOffset = queriedEdge.getEventTime() - this.getResourceCapabilities().findEdge(queriedEdge.getParent(),queriedEdge.getChild()).getEventTime();
		double startTime = getCurrentTime()+edgeOffset;
		
		//If the product agent is scheduled for this time, run the desired program at that time;
		if (desiredEdge!=null &&  this.RAschedule.checkPATime(productAgent, (int) startTime, (int) startTime+desiredEdge.getEventTime())){
			//Schedule it for the future
			addBehaviour(new sendSignal(this, edgeOffset, desiredEdge,productAgent));
			
			HashMap<AID,ResourceEvent> edgeProductMap = new HashMap<AID,ResourceEvent>();
			edgeProductMap.put(productAgent, desiredEdge);
			this.notifyAgentWhenState.put(desiredEdge.getChild(), edgeProductMap);
			System.out.println(getLocalName()+"Putting state in notifyAgentWhenState"+desiredEdge.getChild()+edgeProductMap);
			return true;
		}
		
		return false;
	}
	
	public class sendSignal extends WakerBehaviour {
		private static final long serialVersionUID = 6800760293776896340L;
		private final ResourceEvent desiredEdge;
		private final AID productAgent;

		public sendSignal(Agent a, long timeout, ResourceEvent desiredEdge, AID productAgent) {
			super(a, timeout);
			this.desiredEdge = desiredEdge;
			this.productAgent = productAgent;
		}
		
		protected void onWake() {
			runEdge(desiredEdge,productAgent);	
		}
	}
	
	/**
	 * 
	 * @param productAgent 
	 * @param The edge that needs to be run
	 */
	public void runEdge(ResourceEvent edge, AID productAgent) {
		String variableName = edge.getActiveMethod().split(",")[0];
		String variableSet = edge.getActiveMethod().split(",")[1];
		/* OLD PLC
		CallAdsFuncs caf = new CallAdsFuncs();
		caf.openPort(addr);
		caf.setIntValue(variableName, Integer.parseInt(variableSet));
		caf.closePort();
		*/
		System.out.println(productAgent.getLocalName()+",Execution," + variableName + ",time," + getCurrentTime());
	}
	
	/** Check when the edge is done and inform the product agent
	 * @param productAgent
	 * @param edge
	 */
	public void informPA(AID productAgent, ResourceEvent edge){
		
		DirectedSparseGraph<ProductState, ResourceEvent> outputGraph = new DirectedSparseGraph<ProductState, ResourceEvent>();
		outputGraph.addEdge(edge, edge.getParent(),edge.getChild());
		ArrayList<ResourceEvent> occuredEvents = new ArrayList<ResourceEvent>();
		occuredEvents.add(edge);
		
		SystemOutput systemOutput = new SystemOutput(outputGraph, edge.getChild(), occuredEvents);
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM_REF);
		msg.addReceiver(productAgent);
		try {msg.setContentObject(systemOutput);} 
		catch (IOException e) {e.printStackTrace();}
		send(msg);
	}
	
	//================================================================================
    // Helper methods
    //================================================================================
	

	/** Copies the graph
	 * @param oldgraph
	 * @return
	 */
	public DirectedSparseGraph<ProductState, ResourceEvent> copyGraph(
			DirectedSparseGraph<ProductState, ResourceEvent> oldgraph) {
		DirectedSparseGraph<ProductState, ResourceEvent> graph = new DirectedSparseGraph<ProductState, ResourceEvent>();
		
		for (ResourceEvent e : oldgraph.getEdges()){
			ResourceEvent newEdge = e.copy();
			graph.addEdge(newEdge,newEdge.getParent(),newEdge.getChild());
		}
		
		return graph;
	}
 
	/** Clears the graph
	 * @param graph
	 */
	@SuppressWarnings("unused")
	public void clearGraph(DirectedSparseGraph<ProductState, ResourceEvent> graph) {
		for (ResourceEvent e : graph.getEdges()){
			e = null;
		}
		for (ProductState v : graph.getVertices()){
	        v = null;
		}    
	}
	
	protected int getCurrentTime() {
		return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
	}
	/* OLD PLC
	public AmsAddr getAddr() {
		return this.addr;
	}
	*/
	public Capabilities getResourceCapabilities() {
		return resourceCapabilities;
	}

	public void setResourceCapabilities(Capabilities resourceCapabilities) {
		this.resourceCapabilities = resourceCapabilities;
	}
}