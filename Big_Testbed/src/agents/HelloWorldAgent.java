package agents;

import jade.core.Agent;
import javafish.clients.opc.ReadWriteJADE;

public class HelloWorldAgent extends Agent
{
	protected void setup()
	{
		System.out.println("Hello, I am an Agent!!!\n" + "My local-name is "+getAID().getLocalName());
		System.out.println("My GUID is "+getAID().getName());
		ReadWriteJADE hello = new ReadWriteJADE();
		System.out.println(hello.readTag("CNC2Machined2"));
		System.out.println(hello.writeTag("C1RobotStop.Ret",1));
		hello.uninit();
	}
}