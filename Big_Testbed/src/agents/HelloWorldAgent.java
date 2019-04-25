package agents;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import javafish.clients.opc.ReadWriteJADE;
import resourceAgent.Robot1Agent.resetSignal;

/*
 * Please don't delete this. Very useful for debugging and analysis purposes
 */
public class HelloWorldAgent extends Agent
{
	long start1 = 0;
	long finish1 = 0;
	long start2 = 0;
	long finish2 = 0;
	
	protected void setup()
	{
		System.out.println("Hello, I am an Agent!!!\n" + "My local-name is "+getAID().getLocalName());
		System.out.println("My GUID is "+getAID().getName());
		String[] tagListforConnection = {"Fanuc_Rbt_C1:O.Data[0].1","Fanuc_Rbt_C1:O.Data[0].3","Fanuc_Rbt_C1:O.Data[0].2","Fanuc_Rbt_C1:O.Data[0].0", "Robot1_DroppedP1","Robot1_DroppedP2"};
		/*
		String registerMessage = "["+this.getLocalName()+"] Trying to register ";
		for (String tag : tagListforConnection)
		{
			registerMessage += tag + ", ";
		}
		System.out.println(registerMessage);
		ReadWriteJADE hello = new ReadWriteJADE(tagListforConnection,0,"hello");
		*/
		/*try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//System.out.println(hello.readTag("t1.DN"));
		System.out.println("reading tag Fanuc_Rbt_C1:O.Data[0].3");
		String variableName = "Fanuc_Rbt_C1:O.Data[0].3";
		
		
		ACLMessage reqReadTag = new ACLMessage(ACLMessage.REQUEST);
		reqReadTag.setSender(this.getAID());
		reqReadTag.addReceiver(new AID("OPCLayer", AID.ISLOCALNAME));
		reqReadTag.setContent(variableName);
		reqReadTag.setOntology("Read"); // so that OPC Agent knows that the request is a read tag request
		reqReadTag.setConversationId("agent1-read");
		//start2 = System.currentTimeMillis();
		long start = System.currentTimeMillis();
		System.out.println("agent send time for tag "+ variableName + " : "+ String.valueOf(start));
		this.send(reqReadTag);
		System.out.println("Sent read request for the tag: " + variableName);
		 //request for another tag
		
		
		
		//edit::: plcConnection.uninit();
		this.addBehaviour(new requestWrite());
		this.addBehaviour(new requestResponse());
		
		this.addBehaviour(new receiveResponse());
		
		
		
		//hello.uninit();
	}
	
	private class requestWrite extends OneShotBehaviour {
		@Override
		public void action() {
			String variableName = "Fanuc_Rbt_C1:O.Data[0].2";
			ACLMessage reqWriteTag = new ACLMessage(ACLMessage.REQUEST);
			reqWriteTag.setSender(myAgent.getAID());
			reqWriteTag.addReceiver(new AID("OPCLayer", AID.ISLOCALNAME));
			reqWriteTag.setContent(variableName);
			reqWriteTag.setOntology("Write"); // so that OPC Agent knows that the request is a read tag request
			reqWriteTag.setConversationId("agent1-write");
			//reqReadTag.setReplyWith("read" + System.currentTimeMillis()); //to make the request unique
			long start = System.currentTimeMillis();
			System.out.println("[WRITE] agent send time for tag "+ variableName + " : "+ String.valueOf(start));
			myAgent.send(reqWriteTag);
			//System.out.println("Sent read request for the tag: " + variableName);
		}
	}
	
	
	private class requestResponse extends OneShotBehaviour {
		@Override
		public void action() {
			String variableName = "Fanuc_Rbt_C1:O.Data[0].2";
			ACLMessage reqReadTag = new ACLMessage(ACLMessage.REQUEST);
			reqReadTag.setSender(myAgent.getAID());
			reqReadTag.addReceiver(new AID("OPCLayer", AID.ISLOCALNAME));
			reqReadTag.setContent(variableName);
			reqReadTag.setOntology("Read"); // so that OPC Agent knows that the request is a read tag request
			reqReadTag.setConversationId("agent1-read");
			//reqReadTag.setReplyWith("read" + System.currentTimeMillis()); //to make the request unique
			long start = System.currentTimeMillis();
			System.out.println("agent send time for tag "+ variableName + " : "+ String.valueOf(start));
			myAgent.send(reqReadTag);
			start1 = System.currentTimeMillis();
			//System.out.println("Sent read request for the tag: " + variableName);
		}
	}
	
	
	private class receiveResponse extends CyclicBehaviour {
		@Override
		public void action() {
			//System.out.print("waiting");
			ACLMessage response = receive();			
			if (response!=null) {
				long diff=0;
				long end = System.currentTimeMillis();
				String tag = response.getContent();
				/*
				if (response.getOntology().equals("Fanuc_Rbt_C1:O.Data[0].2")) {
					finish1 = System.currentTimeMillis();
					diff = finish1-start1;
					System.out.println("Measurements for Fanuc_Rbt_C1:O.Data[0].2: ");
					System.out.println("start time: " + String.valueOf(start1));
					System.out.println("end time: " + String.valueOf(finish1));
					System.out.println("Timme elapsed for Fanuc_Rbt_C1:O.Data[0].2: "+ String.valueOf(diff));
				}
				if (response.getOntology().equals("Fanuc_Rbt_C1:O.Data[0].3")) {
					finish2 = System.currentTimeMillis();
					diff = finish2-start2;
					System.out.println("Measurements for Fanuc_Rbt_C1:O.Data[0].3: ");
					System.out.println("start time: " + String.valueOf(start2));
					System.out.println("end time: " + String.valueOf(finish2));
					System.out.println("Timme elapsed for Fanuc_Rbt_C1:O.Data[0].3: "+ String.valueOf(diff));
				}*/
				if (response.getContent().equals("1")) {
					System.out.println("[WRITE] agent receive time for tag "+ response.getOntology() + " : "+ String.valueOf(end));
				}
				else {
					System.out.println("agent receive time for tag "+ response.getOntology() + " : "+ String.valueOf(end));
					System.out.println("Tag value received for tag " + response.getOntology() + ": " + tag);
				}
			}
		}
	}
	
	
}