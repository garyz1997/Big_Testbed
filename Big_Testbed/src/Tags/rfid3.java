package Tags;

import jade.core.Agent;
import jade.core.behaviours.*;


public class rfid3 extends Agent{
	private Client opc = null;
	
	protected void setup(){
		System.out.println(getAID().getName()+" is ready.");
		String[] allTags=new String[]{"Test.PLC.Message1","Test.PLC.Message2","Test.PLC.Message3"};
		opc=new Client(allTags);
		opc.init();
		opc.connect();
		opc.doRead();
		try{
		Thread.sleep(5000);
		}catch(Exception e){}
		System.out.println(opc.getValue("Test.PLC.Message1"));
		System.out.println(opc.getValue("Test.PLC.Message2"));
		System.out.println(opc.getValue("Test.PLC.Message3"));
		addBehaviour(new checkPartPresent(this,100));
		opc.closeConnection();
	}
	
	private class checkPartPresent extends TickerBehaviour{
		public checkPartPresent(Agent a, long period) {
			super(a, period);
		}
		protected void onTick(){
			String partPresent=opc.getValue("Conv_N053:I.Data[3].1");
			String tagPresent=null;
			String processNumber=null;
			String partNumber=null;
			//wait 250 milliseconds if there is a part present
			if(partPresent.equals("1")){
				try {
					Thread.sleep(250);
					tagPresent = opc.getValue("RFID_N0555:I.Channel[0].TagPresent");
				} catch (InterruptedException e) {
				}
			}
			//If there is an empty pallet, 
			if(tagPresent.equals("0")){
				//Send message to CNC 3 and 4 to see if they have part waiting
				//Send message to robots to see if they are free
				//If both true, tell robot to pick part and drop on conveyer
			}
			//////////////////////////////////////
			//Left off Here
			
			
			
			
			if(tagPresent.equals("1")){
				processNumber=opc.getValue("R3J_Current_Process_NO");
				partNumber=opc.getValue("R3J_Current_Part_NO");
			}
		}
	}
	protected void takeDown() {
		// Printout a dismissal message
		opc.closeConnection();
		System.out.println(getAID().getName()+" terminating.");
	}

}
