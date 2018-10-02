package Tags;

import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.*;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.lib.common.AlreadyConnectedException;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.common.NotConnectedException;
import org.openscada.opc.lib.da.AccessBase;
import org.openscada.opc.lib.da.AddFailedException;
import org.openscada.opc.lib.da.DataCallback;
import org.openscada.opc.lib.da.DuplicateGroupException;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.da.SyncAccess;

public class Client {
	static ConnectionInformation ci = new ConnectionInformation();
 	static Server server;
 	private ArrayList<String> allTags = new ArrayList<String>();
 	private ArrayList<String> tagValues = new ArrayList<String>();
 	private AccessBase access_read=null;
 	private ArrayList<Item> writeTagGroup= new ArrayList<Item>();
 	public Hashtable<String, String> opcValue = new Hashtable<String, String>();
 	
 	public Client(String[] tags){
 		for (int i=0; i<tags.length;i++){
 			allTags.add(tags[i]);
 			tagValues.add("Error");
 			opcValue.put(tags[i],"Error");
 		}
 		try{
 			JISystem.setInBuiltLogHandler(false);
 		}catch(Exception e){
 			e.printStackTrace();
 			//Catch IO exception?
 		}
 	}
 	
	public void init()
    {
		//get password from txt file
		String password=null;
		try{
			FileReader filereader=new FileReader("resources/password.txt");
			BufferedReader bufferedReader = new BufferedReader(filereader);
			password = bufferedReader.readLine();
		}
		catch(FileNotFoundException ex){
			System.out.println("Can't open password.txt");
		}
		catch(IOException ex){
			System.out.println("Error reading password.txt");
		}
    	// create connection information 
    	ci.setHost("localhost");
        ci.setDomain("");
        ci.setUser("VRMILLING");
        ci.setPassword(password);
        //set ProgID OR ClsID
        ci.setProgId("RSLinx OPC Server");
        //ci.setClsid("A05BB6D5-2F8A-11D1-9BB0-080009D01446");
    	//itemId = "Test.PLC.Message1";
    	// create a new server
    	server = new Server(ci, Executors.newSingleThreadScheduledExecutor());
    }
	
	public void connect() 
    {
    	try 
    	{
    		// connect to server
    		server.connect();
    		access_read = new SyncAccess(server,1000);
    		//This function adds the appropriate tags to a group for write access
    		addWriteItemsToGroup();
    	}	 
    	catch (Exception e) 
    	{
    		e.printStackTrace();
    	}
    }
	
	public void doRead()
    {
    	// add sync access, poll every 1000 ms
        for (int i=0; i<allTags.size();i++){
        	//cant use local variable in the lambda function?
        	final int i_inner=i;
        	String itemId;
        	itemId=allTags.get(i);
        	try {
				access_read.addItem(itemId, new DataCallback() {
					//This is a lambda function that specifies what to do after item is read
				    public void changed(Item item, ItemState state) 
				    {
				        //System.out.println(state);
				    	String tagValue = null;
				    	try{
				    		tagValue=state.getValue().getObjectAsString2();
				    	}catch(Exception e){
				    		//just need to catch the JIException somehow
				    	}
				    	//Using parallel lists
				    	tagValues.set(i_inner, tagValue);
				    	opcValue.put(allTags.get(i_inner),tagValue);
				    }
				});
			} catch (JIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AddFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        // start reading
        access_read.bind();
    }
	
	//This method looks up a tag name and provides its value
	public String getValue(String tagName){
		for(int i=0;i<allTags.size();i++){
			if(tagName.equals(allTags.get(i))){
				return tagValues.get(i);
			}
		}
		String error="error";
		return error;
	}
	
	public void addWriteItemsToGroup(){
		try{
			final Group group = server.addGroup("writeGroup");
			for (int i=0; i<allTags.size();i++){
	        	String itemId;
	        	itemId=allTags.get(i);
	        	//This array list contains all the opc items to be written to
	        	// Add a new item to the group
	        	//example: final Item item = group.addItem(itemId);
	        	writeTagGroup.add(group.addItem(itemId));	
	        	}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
    public void doWrite(String tagName, String newValue) 
    {
    	for (int i=0; i<allTags.size();i++){
        	String itemId;
        	itemId=allTags.get(i);
        	if(tagName.equals(itemId)){
        		//write value to tag
        		final JIVariant value = new JIVariant(newValue);
        		try {
					writeTagGroup.get(i).write(value);
				} catch (JIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
    }
    
	public void closeConnection()
	{
		try {
			access_read.unbind();
		} catch (JIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
