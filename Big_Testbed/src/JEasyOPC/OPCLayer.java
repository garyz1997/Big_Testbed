package JEasyOPC;
import java.util.HashMap;
import java.util.Map;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javafish.clients.opc.ReadWriteJADE;
import javafish.clients.opc.exception.SynchWriteException;
import sharedInformation.Bid;

public class OPCLayer extends Agent 
{
	public class Tags
	{
		private HashMap<String, Integer> tags = new HashMap<>();
		private String agentName;
		
		public Tags(String agentName)
		{
			this.agentName = agentName;
		}
		
		public String getAgentName()
		{
			return this.agentName;
		}
		
		public int getTagValue(String tagName)
		{
			if (!tags.containsKey(tagName)) {
				return -1;
			}
			return tags.get(tagName);
		}
		
		public void writeTag(String tagName, int value)
		{
			tags.put(tagName, value);
		}
		

	}
	
	//Tags{
	//  Agent name
	//  HashMap<String, Int> tags = {Tag_name, value}
	//}
	
	//Hashmap<String,Tags> OPCList = {Agent_name, Tags} 
	private HashMap<String, Tags> OPCList = new HashMap<>();
	ReadWriteJADE plcConnection;//delete the helloAgent after testing
	String[] agentNames = {"Robot1Agent", "CNC1Agent", "CNC2Agent", "Robot2Agent", "CNC3Agent", "CNC4Agent", "ConveyorAgent"};
	String[][] agentTags = {//TODO: add watchvariable tags
									{"Fanuc_Rbt_C1:O.Data[0].1","Fanuc_Rbt_C1:O.Data[0].3","Fanuc_Rbt_C1:O.Data[0].2","Fanuc_Rbt_C1:O.Data[0].0", "Robot1_DroppedP1","Robot1_DroppedP2"},
									{"CNC1Machined0","CNC1Machined1","CNC1Machined2"},
									{"CNC2Machined1","CNC2Machined2","CNC2Machined3"},
									{"Fanuc_Rbt_C2:O.Data[0].0","Fanuc_Rbt_C2:O.Data[0].2","Fanuc_Rbt_C2:O.Data[0].3","Fanuc_Rbt_C2:O.Data[0].1", "Robot2_DroppedP3","Robot2_DroppedP4"},
									{"CNC3Machined2","CNC3Machined3","CNC3Machined4"},
									{"CNC4Machined3","CNC4Machined4","CNC4Machined5"},
									{"C1HoldBack.Ret","C2HoldBack.Ret","C3HoldBack.Ret","t1.DN", "C1_N011:I.Data[6].7","Conv_N053:I.Data[3].1","Conv_N053:I.Data[3].3", "ConveyorEnd"}
								};
	public OPCLayer()
	{
		
	}
	
	protected void setup()
	{
		System.out.println("CREATED: OPCLayer "+getAID().getLocalName());

		doSuspend();//Suspend Agent upon creation. Resume Agent via GUI to start it up.
		//initialize all agents and their tags
		for (int i = 0; i < agentTags.length;i++)
		{
			Tags t = new Tags(agentNames[i]);
			for (int j = 0; j < agentTags[i].length;j++)
			{
				t.writeTag(agentTags[i][j], 0);//initialize all tags to 0, cyclic behavior will update to actual values
			}
			OPCList.put(agentNames[i], t);
		}
		//TODO: get all the tag values and put them in tags
		String[] tagListforConnection = {"Fanuc_Rbt_C1:O.Data[0].1","Fanuc_Rbt_C1:O.Data[0].3","Fanuc_Rbt_C1:O.Data[0].2","Fanuc_Rbt_C1:O.Data[0].0",
				"Robot1_DroppedP1","Robot1_DroppedP2",
				"CNC1Machined0","CNC1Machined1","CNC1Machined2",
				"CNC2Machined1","CNC2Machined2","CNC2Machined3",				
				"Fanuc_Rbt_C2:O.Data[0].0","Fanuc_Rbt_C2:O.Data[0].2","Fanuc_Rbt_C2:O.Data[0].3","Fanuc_Rbt_C2:O.Data[0].1",
				"Robot2_DroppedP3","Robot2_DroppedP4",
				"CNC3Machined2","CNC3Machined3","CNC3Machined4",
				"CNC4Machined3","CNC4Machined4","CNC4Machined5",
				"C1HoldBack.Ret","C2HoldBack.Ret","C3HoldBack.Ret","t1.DN",
				"C1_N011:I.Data[6].7","Conv_N053:I.Data[3].1","Conv_N053:I.Data[3].3", "ConveyorEnd"
				};
		
		String registerMessage = "["+this.getLocalName()+"] Trying to register ";
		for (String tag : tagListforConnection)
		{
			registerMessage += tag + ", ";
		}
		//System.out.println(registerMessage);
		plcConnection = new ReadWriteJADE(tagListforConnection, 1000, "OPCLayer");
		System.out.println("registered!");
		
		//initialize all tags
		for (int i = 0; i < agentTags.length;i++)
		{
			for (int j = 0; j < agentTags[i].length;j++)
			{
				if (agentTags[i][j] == "") {
					continue;
				}
				//System.out.println("here " + agentTags[i][j]);
				//System.out.println("read tag : " + plcConnection.readTag(agentTags[i][j]));
				int tagValue = plcConnection.readTagInt(agentTags[i][j]);
				
				//System.out.println("agent name " + agentNames[i] + " with tag " + tagValue);
				OPCList.get(agentNames[i]).writeTag(agentTags[i][j], tagValue);
			}
		}
		
		
		//Start all of the behaviors
		this.addBehaviour(new updateTags());
		this.addBehaviour(new readWriteTags());
		
	
	}
	
	//IMPLEMENTED RECEPTION OF REQUEST TODO: BUT HAVE NOT SENT THIS INFO BACK TO THE SENDER AGENT
	private class readWriteTags extends CyclicBehaviour {
		@Override
		public void action() {
			//Check for requests from agents to read or write to tags
			int data;
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null){
				//prepare message response
				ACLMessage response = new ACLMessage(ACLMessage.INFORM);
				response.addReceiver(msg.getSender());
				response.setSender(myAgent.getAID());
				
				if (msg.getOntology().contains("Read")) {
					//Read the tag specified in the content for the sender agent
					//System.out.println("requesting tag " + msg.getContent() + " from " + msg.getSender().getLocalName());
					
					if (OPCList.get(msg.getSender().getLocalName()).getTagValue(msg.getContent()) != -1) {
						data = OPCList.get(msg.getSender().getLocalName()).getTagValue(msg.getContent());
						response.setOntology(msg.getContent());
						if (data == 1) {
							response.setContent("true");
						}
						else {
							response.setContent("false");
						}
						myAgent.send(response);					
						//System.out.println("SENTTTTTTT");
					}
					
							//tags.get(msg.getContent());
					//TODO: send back a reply to the sender agent with the read tagvalue
					
					
				}
				else if (msg.getOntology().contains("Write")) {
					//TODO: code to check agent and write tag value
					String variableName = msg.getContent();
					response.setOntology("Write");
					try {
						plcConnection.writeTag(variableName, 1);//TODO:get actual variable name and value
						response.setContent("1");
						send(response);
					} catch (Exception e) {
						e.printStackTrace();
						response.setContent("0");
						send(response);
					}					
				}
				else {
					putBack(msg);
				}
				
			}
			else{
				block();
			}
		}
	}
	
	private class updateTags extends CyclicBehaviour {
		@Override
		public void action() {
			//update each tag
			for (int i = 0; i < agentTags.length;i++)
			{
				for (int j = 0; j < agentTags[i].length;j++)
				{
					if (agentTags[i][j] == "") {
						continue;
					}
					//System.out.println("here " + agentTags[i][j]);
					//System.out.println("read tag : " + plcConnection.readTag(agentTags[i][j]));
					int tagValue = plcConnection.readTagInt(agentTags[i][j]);
					
					//System.out.println("agent name " + agentNames[i] + " with tag " + tagValue);
					OPCList.get(agentNames[i]).writeTag(agentTags[i][j], tagValue);
				}
			}
			//System.out.println(OPCList.get("Robot1Agent").getTagValue("Fanuc_Rbt_C1:O.Data[0].2"));
		}
	}
}
