package agents;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import javafish.clients.opc.ReadWriteJADE;
import resourceAgent.Robot1Agent.resetSignal;

public class HelloWorldAgent extends Agent
{
	protected void setup()
	{
		System.out.println("Hello, I am an Agent!!!\n" + "My local-name is "+getAID().getLocalName());
		System.out.println("My GUID is "+getAID().getName());
		String[] tagListforConnection = {"t1.DN"};
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
		System.out.println("reading tag t1.DN");
		String variableName = "Fanuc_Rbt_C1:O.Data[0].3";
		
		
		ACLMessage reqReadTag = new ACLMessage(ACLMessage.REQUEST);
		reqReadTag.setSender(this.getAID());
		reqReadTag.addReceiver(new AID("OPCAgent", AID.ISLOCALNAME));
		reqReadTag.setContent(variableName);
		reqReadTag.setOntology("Read"); // so that OPC Agent knows that the request is a read tag request
		reqReadTag.setConversationId("agent1-read");
		//reqReadTag.setReplyWith("read" + System.currentTimeMillis()); //to make the request unique
		System.out.println("Sending read request for the tag: " + variableName + ".....");
		this.send(reqReadTag);
		System.out.println("Sent read request for the tag: " + variableName);
		 //request for another tag
		
		
		
		//edit::: plcConnection.uninit();
		this.addBehaviour(new requestResponse());
		this.addBehaviour(new receiveResponse());
		
		//hello.uninit();
	}
	
	private class requestResponse extends OneShotBehaviour {
		@Override
		public void action() {
			String variableName = "Fanuc_Rbt_C1:O.Data[0].2";
			ACLMessage reqReadTag = new ACLMessage(ACLMessage.REQUEST);
			reqReadTag.setSender(myAgent.getAID());
			reqReadTag.addReceiver(new AID("OPCAgent", AID.ISLOCALNAME));
			reqReadTag.setContent(variableName);
			reqReadTag.setOntology("Read"); // so that OPC Agent knows that the request is a read tag request
			reqReadTag.setConversationId("agent1-read");
			//reqReadTag.setReplyWith("read" + System.currentTimeMillis()); //to make the request unique
			System.out.println("Sending read request for the tag: " + variableName + ".....");
			myAgent.send(reqReadTag);
			System.out.println("Sent read request for the tag: " + variableName);
		}
	}
	
	
	private class receiveResponse extends CyclicBehaviour {
		@Override
		public void action() {
			//System.out.print("waiting");
			ACLMessage response = receive();
			if (response!=null) {
				String tag = response.getContent();
				System.out.println("Tag value received for tag " + response.getOntology() + ": " + tag);
			}
		}
	}
	
	
}