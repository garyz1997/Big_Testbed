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
		System.out.println(hello.readTag("RFID_N056:I.Channel[1].TagPresent"));
		System.out.println(hello.writeTag("Fanuc_Rbt_C2:O.Data[0].3",1));
		hello.uninit();
	}
}