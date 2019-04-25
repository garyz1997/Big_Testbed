package agents;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import javafish.clients.opc.ReadWriteJADE;

/*
 * Please don't delete this. Very useful for debugging and analysis purposes
 */
public class HelloWorldAgent2 extends Agent
{
	protected void setup()
	{
		System.out.println("Hello, I am an Agent!!!\n" + "My local-name is "+getAID().getLocalName());
		System.out.println("My GUID is "+getAID().getName());
		
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
		
		//edit::: plcConnection.uninit();
		this.addBehaviour(new requestResponse());
		this.addBehaviour(new requestWrite());
		this.addBehaviour(new receiveResponse());
		
		//hello.uninit();
	}
	
	private class requestWrite extends OneShotBehaviour {
		@Override
		public void action() {
			String variableName = "CNC1Machined2";
			ACLMessage reqWriteTag = new ACLMessage(ACLMessage.REQUEST);
			reqWriteTag.setSender(myAgent.getAID());
			reqWriteTag.addReceiver(new AID("OPCLayer", AID.ISLOCALNAME));
			reqWriteTag.setContent(variableName);
			reqWriteTag.setOntology("Write"); // so that OPC Agent knows that the request is a read tag request
			reqWriteTag.setConversationId("agent2-write");
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
			String[] tagListforConnection = {"CNC1Machined0","CNC1Machined1","CNC1Machined2"};
			for (String i : tagListforConnection) {
				ACLMessage reqReadTag = new ACLMessage(ACLMessage.REQUEST);
				reqReadTag.setSender(myAgent.getAID());
				reqReadTag.addReceiver(new AID("OPCLayer", AID.ISLOCALNAME));
				reqReadTag.setContent(i);
				reqReadTag.setOntology("Read"); // so that OPC Agent knows that the request is a read tag request
				reqReadTag.setConversationId("agent2-read");
				//reqReadTag.setReplyWith("read" + System.currentTimeMillis()); //to make the request unique
				long start = System.currentTimeMillis();
				System.out.println("agent send time for tag "+ i + " : "+ String.valueOf(start));
				myAgent.send(reqReadTag);
			}
		}
	}
	
	
	private class receiveResponse extends CyclicBehaviour {
		@Override
		public void action() {
			//System.out.print("waiting");
			ACLMessage response = receive();
			if (response!=null) {
				String tag = response.getContent();
				long end = System.currentTimeMillis();
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