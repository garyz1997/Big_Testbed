package Tags;

public class Caller {
	public static void main(String[] args) throws Exception{
		String[] allTags=new String[]{"Test.PLC.Message1","Test.PLC.Message2","Test.PLC.Message3"};
		Client clientClass=new Client(allTags);
		clientClass.init();
		clientClass.connect();
		clientClass.doRead();
		Thread.sleep(1000);
		System.out.println("\n\n");
		System.out.println(clientClass.opcValue.get("Test.PLC.Message1"));
		//System.out.println(clientClass.getValue("Test.PLC.Message1"));
		//System.out.println(clientClass.getValue("Test.PLC.Message2"));
		//System.out.println(clientClass.getValue("Test.PLC.Message3"));
		//System.out.println("Done Done Done Done");
		System.out.println("\n\n");
		for(int i=0;i<10;i++){
			clientClass.doWrite("Test.PLC.Message1", String.format("%s", i));
			Thread.sleep(1000);
			System.out.println(clientClass.getValue("Test.PLC.Message1"));
		}
		Thread.sleep(10000);
		clientClass.closeConnection();
	}
}
