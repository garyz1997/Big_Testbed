package productAgent;

import initializingAgents.ExitPlan;
import initializingAgents.ProductionPlan;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import resourceAgent.ResourceAgent;
import sharedInformation.ResourceEvent;
import sharedInformation.SetupProductAgent;
import sharedInformation.SystemOutput;
import sharedInformation.ProductState;
import sharedInformation.RequestAction;
import sharedInformation.RequestBid;
import sharedInformation.RequestReschedule;
import sharedInformation.RequestSchedule;
import sharedInformation.Bid;
import sharedInformation.Capabilities;
import sharedInformation.PhysicalProperty;

public class ProductAgent extends Agent {	
	
	private static final long serialVersionUID = -1820877264966313122L;
	private String partID;
	private int PANumber = -1;
	private int priority;
	
	private ProductionPlan productionPlan;
	private ExitPlan exitPlan;
	private ProductHistory productHistory;
	private EnvironmentModel environmentModel;
	private PAPlan plan;
	
	private ResourceEvent queriedEdge;
	private ResourceAgent lastResourceAgent;
	
	private ArrayList<Bid> bids;
	private EnvironmentModel newEnvironmentModel;
	private PAPlan newPlan;
	
	private final int explorationWaitTime = 150;
	private final int planningWaitTime = 50;
	private final int nextExecutionStartTime = 500;
	
	private int bidTime = getStartBidTime();
	
	private int lastActionQueriedTime;
	private final int actionTimeout = 15000;
	
	public ProductAgent(){}
	
	protected void setup(){
		
		// Get information about the product through the arguments
		Object[] args = getArguments();
	
		if (args.length!=1) {
			System.out.println("Please add id for: " + this);
			this.doDelete();
		}
		this.partID = (String) args[0];

		//Start all of the initialization
		this.addBehaviour(new Initialization(this, 100));
	
	}
	
	/**
	 * Initializes the PA and starts exploring
	 */
	private class Initialization extends WakerBehaviour {

		public Initialization(Agent a, long period) {
			super(a, period);
		}

		private static final long serialVersionUID = 983617189224701010L;
		private boolean finished = false;

		@Override
		protected void handleElapsedTimeout() {

			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){	
				try {
					//If the message are resource capabilities
					if (msg.getContentObject().getClass().getName().contains("SetupProductAgent")) {
						SetupProductAgent setupParams = (SetupProductAgent) msg.getContentObject();
						
						priority = setupParams.getPriority();
						
						//Initialize the Knowledge Base
						productionPlan = setupParams.getProductionPlan(); // Production Plan
						exitPlan = setupParams.getExitPlan(); 
						productHistory = new ProductHistory(myAgent.getAID(),setupParams.getStartingNode(),setupParams.getStartingResource()); //Product History
						environmentModel = new EnvironmentModel(myAgent.getAID(),setupParams.getStartingNode(),setupParams.getStartingResource()); // Environment Model
						plan = new PAPlan(myAgent.getAID()); //Agent Plan
						
						//Set the starting node in the belief model
						environmentModel.update(setupParams.getStartingNode());
					
						//No edge queried
						queriedEdge = null;
						
						//Setup is finished
						this.finished  = true;
						addBehaviour(new DecisionDirector("Execution"));
						addBehaviour(new AcceptSystemOutput());
					}
					else {
						putBack(msg);
					}

				} catch (UnreadableException e) {
					myAgent.doDelete(); e.printStackTrace();
				}
			}
			else{
				block();
			}
			
			if (!finished) {
				myAgent.addBehaviour(new Initialization(myAgent, 100));
			}
		
		}

		/*@Override
		public boolean done() {
			if (finished) {
				//Start the exploration phase
				addBehaviour(new DecisionDirector("Execution"));
				addBehaviour(new AcceptSystemOutput());
			}
			
			return finished;
		}*/
	}
	
	@Override
	public String toString() {
		return "PA" + this.PANumber + " for " + this.partID;
	}

	public String getPartID(){
		return this.partID;
	}
	
	//================================================================================
    // Decision Director
    //================================================================================
	
	/**
	 * Start this behavior by giving it a completed task (exploration, planning, and execution).
	 * This behaviour will dictate what to do next.
	 */
	private class DecisionDirector extends OneShotBehaviour {

		private static final long serialVersionUID = 6957978387141807023L;
		private String finishedTask;

		public DecisionDirector(String finishedTask) {
			this.finishedTask = finishedTask;
		}

		@Override
		public void action() {
			if (finishedTask.equals("Exploration")) {
				if (newEnvironmentModel.isEmpty()) {exit();}
				else {
					environmentModel.clear();
					environmentModel.update(newEnvironmentModel,newEnvironmentModel.getCurrentState());
					newEnvironmentModel.clear();
					myAgent.addBehaviour(new Planning());
				}
			}
			else if (finishedTask.equals("Planning")) {
				if (newPlan.isEmpty(getCurrentTime())) {exit();}
				else {
					plan = newPlan;
					myAgent.addBehaviour(new Execution());
				}
			}
			else if (finishedTask.equals("Execution")) {
				if (getDesiredProperties().isEmpty()) {exit();}
				else {
					myAgent.addBehaviour(new Exploration());
				}
			}
		}
	}
	
	private void exit() { 
		//Replace with exit plan
		System.out.println(this.getLocalName() + "called Exit Plan");
		doDelete();
	}
	
	//================================================================================
    // Decision Director - Working with KB
    //================================================================================
	
	/** Update the internal environment model
	 * @param currentState
	 */
	private void updateEnvironmentModelState(ProductState currentState) {
		this.environmentModel.update(currentState);
	}
	
	/** Update the internal product history
	 * @param currentState
	 */
	private void updateProductHistory(DirectedSparseGraph<ProductState, ResourceEvent> systemOutput,
			ProductState currentState, ArrayList<ResourceEvent> occuredEvents) {
		this.productHistory.update(systemOutput,currentState,occuredEvents);	
	}
	
	
	//================================================================================
    // Exploration
    //================================================================================
	
	private class Exploration extends OneShotBehaviour {

		private static final long serialVersionUID = 7631418228880040041L;

		@Override
		public void action() {
			
			//Initialize bid collection, current state, and a new environment model
			bids = new ArrayList<Bid>();
			ProductState currentState = productHistory.getCurrentState();
			newEnvironmentModel = new EnvironmentModel(myAgent.getAID(),currentState,productHistory.getLastEvent().getEventAgent()); //New environment model
			
			//Get the RA in charge of starting the bid exploration
			AID contactRA = productHistory.getLastEvent().getEventAgent();
			
			//Ask for bids for each desired property
			for (PhysicalProperty desiredProperty : getDesiredProperties()){
				Bid bid = new Bid();
				bid.addVertex(currentState);
				
				//Ask for bids in the future (allow exploration/waiting time)
				int timeoutOffsets = explorationWaitTime+planningWaitTime+nextExecutionStartTime;
				
				//Create a new bid request
				RequestBid bidRequest = new RequestBid(myAgent.getAID(), desiredProperty, currentState, bidTime+timeoutOffsets, 
						bid, getCurrentTime()+timeoutOffsets);
				
				//Allow for bid acceptance
				AcceptBids acceptBidsBehaviour = new AcceptBids();
				addBehaviour(acceptBidsBehaviour);
				
				//Query for start of exploration (request for bids)
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(contactRA);		
				try { msg.setContentObject(bidRequest);}
				catch (IOException e) {e.printStackTrace();}
				send(msg);
				
				System.out.println(getLocalName().substring(getLocalName().length()-3, getLocalName().length()) + ",Exploration_start," + getCurrentTime());
			
				//Wait for bids to come in
				addBehaviour(new CheckExploration(myAgent,explorationWaitTime,acceptBidsBehaviour));
				//Remove this behavior (CheckExploration will restart this process if possible/necessary)
				removeBehaviour(this);
			}
		}
	}
	
	/**
	 * Checks if the exploration worked out
	 */
	private class CheckExploration extends WakerBehaviour{

		private static final long serialVersionUID = -8591867333126826117L;
		private AcceptBids acceptBidsBehaviour;

		/**
		 * @param agent
		 * @param timeout
		 * @param acceptBidsBehaviour the behavior that waits for bids to be accepted
		 */
		public CheckExploration(Agent a, long timeout, AcceptBids acceptBidsBehaviour) {
			super(a, timeout);
			this.acceptBidsBehaviour = acceptBidsBehaviour;
		}
		
		protected void onWake() {
			//After the timeout, stop accepting bids
	        myAgent.removeBehaviour(acceptBidsBehaviour);	
	        
	        //Build the new environment model from the obtained bids
			for (Bid bid:bids) {
				newEnvironmentModel.update(bid);
			}
			
			//If necessary (new model is empty) and possible (max time not reached), restart exploration
			if(newEnvironmentModel.isEmpty() && bidTime <= getMaxBidTime()) {
				bidTime = bidTime+getBidTimeChange();
				myAgent.addBehaviour(new Exploration());
			}
			//Otherwise send what you have to the decision director
			else {
				bidTime = getStartBidTime();
				myAgent.addBehaviour(new DecisionDirector("Exploration"));
			}
	      }
	}
	
	/**
	 * Waits for bids from the RAs
	 */
	private class AcceptBids extends CyclicBehaviour {
		private static final long serialVersionUID = 8202029907040536901L;

		@Override
		public void action() {
			
			//Obtain any submitted bids and add them to the list
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){	
				try {
					if (msg.getContentObject().getClass().getName().contains("Bid")) {
						bids.add((Bid) msg.getContentObject());
						System.out.println(getLocalName().substring(getLocalName().length()-3, getLocalName().length()) + ",Exploration_end," + getCurrentTime() + ", bid size: " + ((Bid) msg.getContentObject()).getEdgeCount());
					}

					else {
						putBack(msg);
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
	
	/** The function for the product agent to set the starting bid time
	 */
	private int getStartBidTime() {
		return 300000;
	}
	
	/** Function to increase the bid time
	 */
	private int getBidTimeChange() {
		return 300000;
	}
	
	/** The function for the product agent to set the max bid time
	 */
	private int getMaxBidTime() {
		return 910000;
	}
	
	
	//================================================================================
    // Planning/Scheduling
    //================================================================================
	
	private class Planning extends OneShotBehaviour {

		private static final long serialVersionUID = -7620693558974050017L;

		@Override
		public void action() {
			List<ResourceEvent> bestPath = getBestPath(getDesiredProperties(),environmentModel);
			
			//Create a new plan for the substring of the best path that needs to be scheduled
			PAPlan newPlanAttempt = new PAPlan(myAgent.getAID());
			
			int time = getCurrentTime()+planningWaitTime+nextExecutionStartTime ;
			int epsilon = 1000; // Allow small time changes in event duration
			int scheduleBound = Math.min(getMaxScheduledEvents(),bestPath.size());
			
			//Create the new plan based on the best path
			for (int i = 0; i< scheduleBound;i++){
				ResourceEvent scheduleEvent = bestPath.get(i);
				int eventEndTime = time+scheduleEvent.getEventTime()+epsilon;
				//Create a plan
				newPlanAttempt.addEvent(scheduleEvent, time, eventEndTime);
				time = eventEndTime;
			}
			
			//Remove any future events in the current plan
			removeScheduledEvents(getCurrentTime(),plan);
		
			//Schedule all of the events in the new plan
			boolean badPathFlag = scheduleEvents(getCurrentTime(),newPlanAttempt);
			
			if(!badPathFlag){
				removeScheduledEvents(getCurrentTime(),newPlanAttempt);
				newPlanAttempt = new PAPlan(myAgent.getAID());
			}
			
			newPlan = newPlanAttempt;
			
			//Need a check to see if all events were actually planned
			addBehaviour(new WakerBehaviour(myAgent,500){
				private static final long serialVersionUID = 1618239324677520811L;

				protected void onWake() {
					addBehaviour(new DecisionDirector("Planning"));
				}
			});
			
		}
	}


	/** Maximum number of events to schedule
	 * @return integer
	 */
	private int getMaxScheduledEvents() {
		return 1;
	}
	
	/** Remove the events after a certain time
	 * @param time
	 */
	private boolean removeScheduledEvents(int time, PAPlan plan){
		
		int nextPlannedEventIndex = plan.getIndexOfNextEvent(time);
		ResourceEvent nextEvent = plan.getIndexEvent(nextPlannedEventIndex);
		boolean flag = true;
		
		//Remove any of the current plan's scheduled events s
		while(nextEvent!=null){

			RequestReschedule rescheduleRequest = new RequestReschedule(getAID(),  
					plan.getIndexStartTime(nextPlannedEventIndex), plan.getIndexEndTime(nextPlannedEventIndex));
			
			//Query for start of exploration (request for bids)
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(nextEvent.getEventAgent());		
			try { msg.setContentObject(rescheduleRequest);}
			catch (IOException e) {e.printStackTrace();}
			send(msg);
			
			//NEED CHECK IF THIS DOESN'T WORK
			
			nextPlannedEventIndex+=1;
			nextEvent = plan.getIndexEvent(nextPlannedEventIndex);
		}
		
		return flag;
	}
	
	/** Schedule events after a certain time
	 * @param time
	 */
	private boolean scheduleEvents(int time, PAPlan plan){
		int nextPlannedEventIndex = plan.getIndexOfNextEvent(time);
		ResourceEvent nextEvent = plan.getIndexEvent(nextPlannedEventIndex);
		boolean flag = true;
		
		//Remove any of the current plan's scheduled events 
		while(nextEvent!=null){
			
			RequestSchedule scheduleRequest = new RequestSchedule(getAID(), nextEvent, 
					plan.getIndexStartTime(nextPlannedEventIndex), plan.getIndexEndTime(nextPlannedEventIndex));
			
			//Query for start of exploration (request for bids)
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(nextEvent.getEventAgent());		
			try { msg.setContentObject(scheduleRequest);}
			catch (IOException e) {e.printStackTrace();}
			send(msg);
			
			System.out.println(getLocalName().substring(getLocalName().length()-3, getLocalName().length()) + ",Planning," + getCurrentTime());
			
			//NEED CHECK IF THIS DOESN'T WORK
			
			nextPlannedEventIndex+=1;
			nextEvent = plan.getIndexEvent(nextPlannedEventIndex);
		}
		
		return flag;
	}
	
	/** Finding the "best" (according to the weight transformer function) path
	 * @param environmentModel
	 * @param desiredProperties 
	 * @return A list of Capabilities Edges that correspond to the best path
	 */
	private List<ResourceEvent> getBestPath(ArrayList<PhysicalProperty> desiredProperties, EnvironmentModel environmentModel){
		//Find the shortest path
		DijkstraShortestPath<ProductState, ResourceEvent> shortestPathGetter = 
				new DijkstraShortestPath<ProductState, ResourceEvent>(environmentModel, this.getWeightFunction());
		shortestPathGetter.reset();
		
		//Set initial values
		int dist = 999999999; //a very large number
		ProductState desiredNodeFinal = null;
		
		//Find the desired distance with the shortest distance
		
		ArrayList<ProductState> desiredNodes = new ArrayList<ProductState>();
		
		//Find the desired nodes in the environment model
		for (PhysicalProperty property: desiredProperties){
			for (ProductState node : environmentModel.getVertices()){
				if (node.getPhysicalProperties().contains(property)){
					desiredNodes.add(node);
				}
			}
		}
		
		//Find the fastest path to one of the desired nodes
		for (ProductState desiredNode: desiredNodes){
			int compareDist = shortestPathGetter.getDistanceMap(environmentModel.getCurrentState()).get(desiredNode).intValue();
			if (compareDist < dist){
				dist = compareDist;
				desiredNodeFinal = desiredNode;
			}
		}
		
		return shortestPathGetter.getPath(environmentModel.getCurrentState(), desiredNodeFinal);
	}
	
	/** The transformer for the capabilities of the resource agent to the desires of the product agent
	 * @return 
	 */
	private Function<ResourceEvent, Integer> getWeightFunction(){
		return new Function<ResourceEvent,Integer>(){
			public Integer apply(ResourceEvent event) {
				return event.getEventTime();
			}
		};
	}
	
	//================================================================================
    // Planning/Scheduling
    //================================================================================

	/**
	 * Listens to the output from the Resource Agents in the system about events that occur on the part
	 */
	private class AcceptSystemOutput extends CyclicBehaviour {

		private static final long serialVersionUID = 3662148034811593229L;

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF);
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){	
				try {
					if (msg.getContentObject().getClass().getName().contains("SystemOutput")) {
						SystemOutput systemOutput = (SystemOutput) msg.getContentObject();

						//Update the models on the latest information about the product state
						updateEnvironmentModelState(systemOutput.getCurrentState());
						updateProductHistory(systemOutput.getGraph(),systemOutput.getCurrentState(),
								systemOutput.getOccuredEvents());
						
						//If there is no next action, find a new plan by telling the Decision Director that Execution has been finished
						if (plan.isEmpty(getCurrentTime())){
							myAgent.addBehaviour(new DecisionDirector("Execution"));
						}
						else{
							//Start the next step of execution
							addBehaviour(new Execution());
						}
					}
					else {
						putBack(msg);
					}
				} catch (UnreadableException e) {
					myAgent.doDelete(); e.printStackTrace();
				}
			}
			else {
				block();
			}
		}
	
	}
	
	/**
	 * Execute the next action by calling queryResource during the scheduled time
	 */
	public class Execution extends OneShotBehaviour{

		private static final long serialVersionUID = 4475886365215155940L;

		@Override
		public void action() {
			int nextIndex = plan.getIndexOfNextEvent((int) getCurrentTime());
			ResourceEvent nextAction = plan.getIndexEvent(nextIndex);
			
			//Find the event time
			int nextEventTime = plan.getIndexStartTime(nextIndex);

			//Schedule querying the resource for the next action
			addBehaviour(new waitForQuery(myAgent,nextEventTime-getCurrentTime(), nextAction));
		}
	}
	
	/**
	 * Schedules a query in (timeout) time
	 */
	public class waitForQuery extends WakerBehaviour{

		private static final long serialVersionUID = -3683057277678149213L;
		final ResourceEvent nextAction;
		
		public waitForQuery(Agent a, long timeout, ResourceEvent nextAction) {
			super(a, timeout);
			this.nextAction = nextAction;
			
			lastActionQueriedTime = getCurrentTime();
			
			// Print it out if the PA is waiting too long
			if(timeout<0 || timeout>4000) {
				System.out.println("Next query for " + myAgent.getLocalName() + " is in " +timeout+ " milliseconds.");
			}
		}
		
		protected void onWake (){
			//Set the queried edge		
			queriedEdge = nextAction;
			
			RequestAction requestAction = new RequestAction(nextAction, myAgent.getAID());
			
			//Query for start of execution (request for bids)
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(nextAction.getEventAgent());		
			try { msg.setContentObject(requestAction);}
			catch (IOException e) {e.printStackTrace();}
			send(msg);
			
			System.out.println(getLocalName().substring(getLocalName().length()-3, getLocalName().length()) + ",Execution," + getCurrentTime());
			
			myAgent.addBehaviour(new checkForNoRaResponse(myAgent,
					requestAction.getQueriedEdge().getEventTime()+actionTimeout, lastActionQueriedTime));
			
//			if (!queried){
//				exit();
//				System.out.println("" + this + " query did not work for " + resourceAgent + " " + edge);
//			}
		}
	}
	
	public class checkForNoRaResponse extends WakerBehaviour{

		private static final long serialVersionUID = -3683057277678149213L;
		final int checkActionQueriedTime;
		
		public checkForNoRaResponse(Agent a, long timeout, int checkActionQueriedTime) {
			super(a, timeout);
			this.checkActionQueriedTime = checkActionQueriedTime;
		}
		
		protected void onWake (){
			if (checkActionQueriedTime==lastActionQueriedTime) {
				System.out.println("No response from RAs for" + myAgent.getLocalName());
				exit();
			}
		}
	}
	
	//================================================================================
    // Helper algorithms/methods
    //================================================================================
	            
	/** Compare the product history to the production plan to obtain a desired set of physical states
	 * @return The desired set of properties for the product agent
	 */
	private ArrayList<PhysicalProperty> getDesiredProperties() {
		
		ArrayList<PhysicalProperty> incompleteProperties = new ArrayList<PhysicalProperty>();
		
		//Obtain the states that have occurred on the physical product using the product history
		ArrayList<ProductState> checkStates = new ArrayList<ProductState>();
		for (ResourceEvent event: this.productHistory.getOccurredEvents()){
			checkStates.add(event.getChild());
		}
		
		//Compare the production plan to the occurred states
		for(HashSet<PhysicalProperty> set:this.productionPlan.getSetList()){
			int highestIndex = -1; // the highest index when a desired property occurred
						
			//For each desired physical property
			for (PhysicalProperty desiredProperty : set){
				//Check if it has occurred
				boolean propertyComplete = false;
				for (int index = 0; index<checkStates.size();index++){
					if (checkStates.get(index).getPhysicalProperties().contains(desiredProperty)){
						//If it's occurred, overwrite the highest index, if appropriate
						if (index>highestIndex){
							highestIndex = index;
						}
						propertyComplete = true;
						break;
					}
				}
				
				//Add to incomplete properties if the property hasn't previously occurred in the product history
				if(!propertyComplete){
					incompleteProperties.add(desiredProperty);
				}
			}
			
			//If there are incomplete properties, then return these
			if (!incompleteProperties.isEmpty()){
				break;
			}
			//If there aren't incomplete properties, then go onto the next set of properties
			//Note: need to remove all of the properties that are associated with the previous set to continue
			else{
				for (int j=0;j<highestIndex;j++){
					checkStates.remove(0);
				}
			}
		}
		
		return incompleteProperties;
	}

	public int getCurrentTime() {
		return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
	}

	//================================================================================
    // PA communication
    //================================================================================
	
	public int getPriority() {
		return this.priority;
	}

	public void rescheduleRequest(ResourceAgent resourceAgent, int startTime) {

	}

	public ArrayList<ResourceEvent> getProductHistory() {
		return this.productHistory.getOccurredEvents();
	}
	
}