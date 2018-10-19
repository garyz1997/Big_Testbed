package sharedInformation;

import java.io.Serializable;
import java.util.HashMap;

import jade.core.AID;

public class WatchRAVariableTable implements Serializable {

	private static final long serialVersionUID = 3527676224041332711L;
	
	private HashMap<String, ProductState> stateMapping;
	private HashMap<String, Integer> monitorPeriodMapping;
	private HashMap<String, Boolean> initializeMapping;
	private HashMap<String, Integer> createPeriodMapping;
	
	public WatchRAVariableTable (){
		this.stateMapping = new HashMap<String, ProductState>();
		this.monitorPeriodMapping = new HashMap<String, Integer>();
		this.initializeMapping = new HashMap<String, Boolean>();
		this.createPeriodMapping = new HashMap<String, Integer>();
	}
	
	public void put(String variableName, ProductState state, int monitorPeriod, boolean initializeAllowed, int createPeriod) {
		this.stateMapping.put(variableName, state);
		this.monitorPeriodMapping.put(variableName, monitorPeriod);
		this.initializeMapping.put(variableName, initializeAllowed);
		this.createPeriodMapping.put(variableName, createPeriod);
	}

	public HashMap<String, Integer> getMonitorPeriodMapping() {
		return monitorPeriodMapping;
	}

	public HashMap<String, ProductState> getStateMapping() {
		return stateMapping;
	}

	public HashMap<String, Boolean> getInitializeMapping() {
		return this.initializeMapping;
	}

	public HashMap<String, Integer> getCreatePeriod() {
		return createPeriodMapping;
	}
	
}