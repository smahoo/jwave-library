package de.smahoo.jwave.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.cmd.*;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.event.JWaveEventType;
import de.smahoo.jwave.event.JWaveNodeDataEvent;
import de.smahoo.jwave.event.JWaveNodeEvent;
import de.smahoo.jwave.event.JWaveNodeEventListener;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveNode {
	protected int nodeId		= -1;
	protected List<JWaveCommandClass> cmdClasses		= null;
	protected Collection<JWaveCommandClass> secSuppCmdClasses = null;
	//private boolean initialized = false;
	protected List<JWaveNodeEventListener> eventListeners = null;

	protected JWaveNodeCommunicator nodeCommunicator = null;
	
	protected JWaveGenericDeviceType genDevType   = null;
	protected JWaveSpecificDeviceType specDevType = null;
	protected JWaveBasicDeviceType basicDeviceType = null;
	
	protected byte capability	 = -1;
	protected byte status		 = -1;
	protected byte security		 = -1;	
	protected int manufactureId  = -1;
	protected int productTypeId  = -1;
	protected int productId      = -1;
	
	protected JWaveNodeSleepMode sleepMode = null;
	
	
	
	protected JWaveNode(){
		sleepMode = JWaveNodeSleepMode.SLEEP_MODE_NONE;
		cmdClasses = new ArrayList<JWaveCommandClass>();
		secSuppCmdClasses = new ArrayList<JWaveCommandClass>();
		eventListeners = new ArrayList<JWaveNodeEventListener>();
	}
	
	
	protected synchronized void setSecuritySupportedCmdClasses(Collection<JWaveCommandClass> cmdClasses){
		secSuppCmdClasses = new ArrayList<JWaveCommandClass>();
		synchronized(secSuppCmdClasses){
			for (JWaveCommandClass cc : cmdClasses){
				if (cc != null){
					secSuppCmdClasses.add(cc);
				}
			}
		}
	}
		
	protected synchronized void setCmdClasses(List<JWaveCommandClass> cmdClasses){
		JWaveController.log(LogTag.DEBUG,"setting Command Classes to Z-Wave node with id "+this.getNodeId());
		this.cmdClasses = new ArrayList<JWaveCommandClass>();
		synchronized (cmdClasses) {		
			for (JWaveCommandClass cc : cmdClasses){
				if (cc != null) {
					this.cmdClasses.add(cc);
				} else {
					JWaveController.log(LogTag.DEBUG, "One given JWaveCommandClass is null, will be ignored");
				}
			}
		}
		//this.cmdClasses = cmdClasses;			
		if (supportsClassWakeUp()){
			sleepMode = JWaveNodeSleepMode.SLEEP_MODE_AWAKE;
		} else {
			sleepMode = JWaveNodeSleepMode.SLEEP_MODE_NONE;
		}
		dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_CONFIG_CMD_CLASSES_SET, this));
	}
		
	protected JWaveCommandClass getCommandClass(int key){
		for (JWaveCommandClass cc : getCommandClasses()){
			if (cc.getKey() == key){
				return cc;
			}
		}
		return null;
	}
	
//	protected synchronized void setStatus(int status){
//		if ((byte)status != this.status){
//			this.status = (byte)status;
//			dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_STATUS_CHANGED, this));
//		}
//				
//	}
	
//	protected void setSecurity(int security){
//		if ((byte)security != this.security){
//			this.security = (byte)security;
//			dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_CONFIG_SECURITY_SET, this));
//		}
//	}
	
//	protected void setCapabilities(byte capabilities){
//		this.capability = capabilities;
//		if (this.isListening()){
//			sleepMode = JWaveNodeSleepMode.SLEEP_MODE_NONE;
//		} else {
//			sleepMode = JWaveNodeSleepMode.SLEEP_MODE_AWAKE;
//		}
//		dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_CONFIG_CAPABILITIES_SET, this));		
//	}	
	
	
	
	protected void setDeviceType( JWaveBasicDeviceType basic, JWaveGenericDeviceType generic, JWaveSpecificDeviceType specific){	
		
		if (genDevType == generic){
			if (specDevType == specific) {
				if (basicDeviceType == basic){
					return; // nothing to change
				}
			}
		}		
		genDevType = generic;
		specDevType = specific;
		this.basicDeviceType = basic;
		dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_CONFIG_DEVICE_TYPE_SET, this));		
		
	}
	
	public Collection<JWaveCommandClass> getSecuritySupportedCmdClasses(){
		Collection<JWaveCommandClass> result = new ArrayList<JWaveCommandClass>();
		synchronized(secSuppCmdClasses){
			result.addAll(secSuppCmdClasses);
		}
		return result;
	}
	
	public Collection<JWaveCommandClass> getNonSecuritySupportedCmdClasses(){
		Collection<JWaveCommandClass> result = new ArrayList<JWaveCommandClass>();
		synchronized(cmdClasses){
			result.addAll(cmdClasses);
		}
		return result;
	}
	
	public boolean supportsCommandClassSecure(int key){

		for (JWaveCommandClass cc : this.getSecuritySupportedCmdClasses()){
			if (cc.getKey() == key) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean supportsCommandClassNonSecure(int key){
		synchronized (cmdClasses){
			for (JWaveCommandClass cc : this.cmdClasses){
				if (cc.getKey() == key) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean supportsCommandClass(int key){
		
		for (JWaveCommandClass cc : this.getCommandClasses()){
			if (cc.getKey() == key) {
				return true;
			}
		}
		return supportsCommandClassSecure(key);
	}
	
	public boolean supportsClassMultiCmd(){
		return supportsCommandClass(0x8f);
	}
	
	public boolean supportsClassBattery(){		
		 return supportsCommandClass(0x80);
	}
	
	public boolean supportsClassWakeUp(){
		return supportsCommandClass(0x84);
	}
	
	public boolean supportsClassSecurity(){
		return supportsCommandClass(0x98);
	}
	
	public boolean supportsClassConfiguration(){
		return supportsCommandClass(0x70);
	}
	
	public boolean supportsClassAssociation(){
		return supportsCommandClass(0x85);
	}
	
	public boolean supportsClassManufactureSpecific(){
		return supportsCommandClass(0x72);
	}
	
	public boolean supportsClassVersion(){
		return supportsCommandClass(0x86);
	}
	
	public List<JWaveCommandClass> getCommandClasses(){
		ArrayList<JWaveCommandClass> classes = new ArrayList<JWaveCommandClass>();
		synchronized (cmdClasses ){		
		   classes.addAll(cmdClasses);
		}
		synchronized (secSuppCmdClasses) {
			classes.addAll(secSuppCmdClasses);
		}
		return classes;
	}
	
		
	public int getNodeId(){
		return nodeId;
	}
	
	public boolean hasManufactureDetails(){
		return this.manufactureId != -1;
	}
	
	public int getManufactureId(){
		return manufactureId;
	}
	
	public int getProductTypeId(){
		return productTypeId;
	}
	
	public int getProductId(){
		return productId;
	}
	
	public synchronized void addEventListener(JWaveNodeEventListener listener){
		if (eventListeners.contains(listener)) return;
		eventListeners.add(listener);
	}
	
	public void removeEventListener(JWaveNodeEventListener listener){
		if (eventListeners.isEmpty()) return;
		eventListeners.remove(listener);
	}
	
	public JWaveGenericDeviceType getGenericDeviceType(){
		return genDevType;
	}
	
	public JWaveSpecificDeviceType getSpecificDeviceType(){
		return specDevType;
	}
	
	public JWaveBasicDeviceType getBasicDeviceType(){
		return basicDeviceType;
	}

	public JWaveNodeSleepMode getSleepMode(){
		return this.sleepMode;
	}
	
	public boolean isSleeping(){
		return sleepMode == JWaveNodeSleepMode.SLEEP_MODE_SLEEPING;
	}
	
	protected synchronized void dispatchNodeEvent(JWaveNodeEvent event){
		if (eventListeners.isEmpty()) return;
		List<JWaveNodeEventListener> newList = new ArrayList<JWaveNodeEventListener>();
		
		// 
		for (JWaveNodeEventListener l : eventListeners){
			newList.add(l);
		}
			
		for (JWaveNodeEventListener l : newList){
			l.onNodeEvent(event);
		}
		
	}
	
	public synchronized void sendData(JWaveNodeCommand cmd){
		if (nodeCommunicator == null) return;
	
		nodeCommunicator.sendCmd(cmd);
		
	}
	
	protected synchronized void onNodeCmdReceived(JWaveNodeCommand cmd){
	
		evaluateNodeCmd(cmd);
		this.dispatchNodeEvent(new JWaveNodeDataEvent(JWaveEventType.NODE_EVENT_DATA_RECEIVED,this, cmd));
	}
	
	private synchronized void evaluateNodeCmd(JWaveNodeCommand cmd){
		if (nodeCommunicator == null) return;
		nodeCommunicator.evaluateReceivedData(cmd);
	}
	
	public boolean isRouting(){
		return false;
	}
	
	public boolean isListening(){		
		return (capability>>>7) != 0;
	}
	
	protected synchronized void setSleepMode(JWaveNodeSleepMode mode){	
		if (this.sleepMode != mode){
			this.sleepMode = mode;
			if (mode == JWaveNodeSleepMode.SLEEP_MODE_SLEEPING){
				this.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_SLEEP,this));
			}
			if (mode == JWaveNodeSleepMode.SLEEP_MODE_AWAKE){
				this.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_WAKEUP,this));
			}
		}
	}
	
	public void setNodeToSleep(){
		nodeCommunicator.setNodeToSleep();
	}
	
	public boolean isNodeCmdBufferEmpty(){
		return nodeCommunicator.isBufferEmpty();
	}
	
	public List<JWaveNodeCommand> getNodeCmdBuffer(){
		return nodeCommunicator.getCmdBuffer();
	}
	
	public void resetCmdBuffer(){
		nodeCommunicator.resetCmdBuffer();
	}
	
}
