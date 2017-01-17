package de.smahoo.jwave.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.JWaveException;
import de.smahoo.jwave.cmd.*;
import de.smahoo.jwave.cmd.JWaveCommand;
import de.smahoo.jwave.event.JWaveErrorEvent;
import de.smahoo.jwave.event.JWaveEventType;
import de.smahoo.jwave.event.JWaveNodeDataEvent;
import de.smahoo.jwave.event.JWaveNodeEvent;
import de.smahoo.jwave.event.JWaveNodeEventListener;
import de.smahoo.jwave.io.JWaveDatagram;
import de.smahoo.jwave.io.JWaveDatagramFactory;
import de.smahoo.jwave.io.JWaveDatagramTransceiver;
import de.smahoo.jwave.security.JWaveSecurityCommunicationProcessor;
import de.smahoo.jwave.security.JWaveSecurityCommunicationProcessorListener;
import de.smahoo.jwave.security.JWaveSecurityException;
import de.smahoo.jwave.utils.ByteArrayGeneration;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveNodeCommunicator {
	
	private static final int NODE_MODE_NORMAL_INTERVIEW = 1;
	private static final int NODE_MODE_NORMAL_INTERVIEW_FINISHED = 2;
	private static final int NODE_MODE_SECURITY_INTERVIEW = 3;
	
	protected JWaveCommandClassSpecification JWaveDefs = null;
	protected JWaveNode node = null;
	protected JWaveDatagramTransceiver transceiver = null;
	protected JWaveNodeEventListener nodeEventListener = null;
	protected List<JWaveNodeCommand> cmdToSendBuffer = null;
	protected JWaveNodeCommand currCmd = null;
	protected boolean waitingForApplCommandHandler = false;
	protected int responsesLeft = 1;
	protected Timer timeOutWatchdog = null;
	protected int mode = NODE_MODE_NORMAL_INTERVIEW;
	protected JWaveNodeInterviewOptions currentInterviewOptions;
	protected boolean securityEnabled = true;
	protected int resend = 0;
	
	protected JWaveSecurityCommunicationProcessor securityProcessor = null;
	
	// JWaveSecurityCommunicationProcessor

	public JWaveNodeCommunicator(){
		// Do NOT use this contructor!
		// It's for testing only 
		this(null,null,null,null);
	}
	
	public JWaveNodeCommunicator(JWaveNode node, JWaveDatagramTransceiver transceiver, JWaveCommandClassSpecification defs, byte[] networkKey){
		JWaveDefs = defs;
		cmdToSendBuffer = new ArrayList<JWaveNodeCommand>();
		this.node = node;
		if (node != null){
			node.nodeCommunicator = this;
		}
		this.transceiver = transceiver;
		securityProcessor = new JWaveSecurityCommunicationProcessor(node,networkKey,new JWaveNodeCommandFactory(defs), new JWaveSecurityCommunicationProcessorListener() {
			
			
			@Override
			public void onSendCommand(JWaveNodeCommand cmd) {
				sendCmd(cmd);				
			}
			
			@Override
			public void onCommandReceived(JWaveNodeCommand cmd) {
				JWaveController.log(LogTag.DEBUG,"decrypted "+cmd.getCommandClass().getName()+" "+cmd.getCommand().getName()+"  ("+ByteArrayGeneration.toHexString(cmd.toByteArray())+")");
				getNode().onNodeCmdReceived(cmd);
				//node.onNodeCmdReceived(cmd);
				//evaluateReceivedData(cmd);				
			}

			@Override
			public void onSecureInclusionFinished(Collection<JWaveCommandClass> supportedCmdClasses) {
				processSecureInterviewFinished(supportedCmdClasses);		
			}

			@Override
			public void onSecureInclusionError(String cause) {
				processSecureInterviewFailed(cause);
				
			}
			
			@Override
			public void onJWaveError(JWaveErrorEvent evnt){
				processSecurityError(evnt);
			}
		});
		
		timeOutWatchdog = new Timer();		
		
	}
	
	
	/**
	 * Starting the interview with the node according to the given interviewOptions. If node supports COMMAND_CLASS_SECURITY the security include process
	 * will be started before interviewing. 
	 * @param interviewOptions request Manufacture Details, command class versions
	 * @throws JWaveException 
	 */
	public void startInterview(JWaveNodeInterviewOptions interviewOptions) throws JWaveException{
		this.currentInterviewOptions = interviewOptions;
	
		if (securityEnabled && (getNode().supportsClassSecurity())){
			startSecurityInclusion();
		} else {
			startNormalInterview();
		}
	}
	
	/**
	 * Will be called after the secure interview has been finished. The normal interview will be started according to the interview options
	 */
	protected void processSecureInterviewFinished(Collection<JWaveCommandClass> supportedCmdClasses){
		node.setSecuritySupportedCmdClasses(supportedCmdClasses);
		node.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.CNTRL_EVENT_SECURITY_INCLUDE_SUCCESS, node));
		try {
			startNormalInterview();
		} catch (JWaveException e) {
			JWaveController.log(LogTag.ERROR,e.getMessage(),e);
		}		
	}
	
	/**
	 * Will be called after the secure interview has been finished. The normal interview will be started according to the interview options
	 */
	protected void processSecureInterviewFailed(String cause){
		node.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.CNTRL_EVENT_SECURITY_INCLUDE_FAILED, node));
		JWaveController.log(LogTag.ERROR,"Security Inclusion failded ("+JWaveEventType.CNTRL_EVENT_SECURITY_INCLUDE_FAILED.name()+"). "+cause);
		try {
			startNormalInterview();
		} catch (JWaveException e) {
			JWaveController.log(LogTag.ERROR,e.getMessage(),e);
		}		
	}
	
	
	protected void processSecurityError(JWaveErrorEvent event){
		JWaveController.log(LogTag.ERROR, ""+event.getMessage());
	}
	
	/**
	 * Will interview the current node according the the interview options
	 * @throws JWaveException
	 */
	protected void startNormalInterview() throws JWaveException{
		mode = NODE_MODE_NORMAL_INTERVIEW;
		node.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_INTERVIEW_STARTED, node));
		if (currentInterviewOptions.isNoInterview()){
			
			sendNext(); // when no commands need to be sent anymore, than sendNext() automatically dispatches the event Interview finished.
						// but only if mode is 1
			return;
		}
		
		if (currentInterviewOptions.isRequestManufactureDetails()){
			if (getNode().supportsCommandClass( 0x72)){
				requestManufactureDetails();
			}
		}	
		if (currentInterviewOptions.isRequestCmdClassVersions()){
			if (getNode().supportsCommandClass(0x86)){
				requestNodeVersion();
			}			
		}	
	}
	
	
	/**
	 * Requesting the Manufacture details like Manufacture ID, Product ID, ProductTypeId
	 * @throws JWaveException
	 */
	protected void requestManufactureDetails() throws JWaveException{		
		
		if (!node.supportsClassManufactureSpecific()){
			JWaveController.log(LogTag.WARN,"Node "+node.getNodeId()+"does not support COMMAND_CLASS_MANUFACTURER_SPECIFIC");
			sendNext(); // when no commands need to be sent anymore, than sendNext() automatically dispatches the event Interview finished.
			return;
		}
		
		try {			
			JWaveCommand cmd = JWaveDefs.getCommand("COMMAND_CLASS_MANUFACTURER_SPECIFIC","MANUFACTURER_SPECIFIC_GET");
			JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
			node.sendData(nodeCmd);			
		}catch (Exception exc){
			throw new JWaveException("Unable to request manufacture details from node "+node.getNodeId()+"!", exc);
		}
	}
	
	public void requestNodeVersion() throws JWaveException{
		
		if (!node.supportsClassVersion()){
			JWaveController.log(LogTag.WARN,"Node "+node.getNodeId()+"does not support COMMAND_CLASS_VERSION");
			sendNext(); // when no commands need to be sent anymore, than sendNext() automatically dispatches the event Interview finished.
			return;
		}
		
		try {
			
		//	dispatchEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_INTERVIEW_STARTED,node));
			JWaveCommandClass cmdClassVersion = this.JWaveDefs.getCommandClass(134);
			if (cmdClassVersion == null){
				// FIXME: throw Exception and handle that! 134 | 0x86 = COMMAND_CLASS_VERSION
			}	

			JWaveCommand getVersion = cmdClassVersion.getCommand(0x11);			// 0x11 COMMAND_GET_VERSION
		
			JWaveNodeCommand getVersionCmd = new JWaveNodeCommand(getVersion);
			node.sendData(getVersionCmd);
		
			JWaveCommand getClassVersion = cmdClassVersion.getCommand(0x13);	// 0x13 COMMAND_CLASS_GET_VERSION
			JWaveNodeCommand getClassVersionCmd;
		
			for (JWaveCommandClass cmdClass : node.getCommandClasses()){
				getClassVersionCmd = new JWaveNodeCommand(getClassVersion);
				getClassVersionCmd.setParamValue(0x00,(byte)cmdClass.getKey());
				node.sendData(getClassVersionCmd);
			}
			
		} catch (Exception exc){
			throw new JWaveException("Unable to request Version details from node "+node.getNodeId()+".",exc);
		}		
	}
	
	private void startSecurityInclusion(){
		node.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.CNTRL_EVENT_SECURITY_INCLUDE_START, node));
		mode = NODE_MODE_SECURITY_INTERVIEW;
		securityProcessor.startSecureIncludeProcess();
	}
	
	public void dispose(){
		if (timeOutWatchdog != null){
			timeOutWatchdog.cancel();
		}
	}
	
	public JWaveNode getNode(){
		return node;
	}
	
	public synchronized boolean isBufferEmpty(){
		return cmdToSendBuffer.isEmpty();
	}
	
	public synchronized List<JWaveNodeCommand> getCmdBuffer(){
		List<JWaveNodeCommand> res = new ArrayList<JWaveNodeCommand>();
		res.addAll(cmdToSendBuffer);
		return res;
	}
	
	public synchronized void resetCmdBuffer(){
		cmdToSendBuffer.clear();
	}
	

	/**
	 * 
	 * @param cmd
	 */
	protected synchronized void evaluateReceivedData(JWaveNodeCommand cmd){
		
		// If it is a MultiCmd, then evaluate it differently 
		if (cmd.getCommandClassKey() == 0x8f){
			evaluateMultiCmd(cmd);
			return;
		}
		
		// if  it is a Securtiy Message, give it to the securityProcessor
		if (cmd.getCommandClassKey() == 0x98){
			try {
				securityProcessor.processSecurityNodeCommand(cmd);
			//	node.setSleepMode(JWaveNodeSleepMode.SLEEP_MODE_AWAKE);
			} catch (JWaveSecurityException secExc){
				secExc.printStackTrace();
			}
			
			
		}
		
		if (cmd.getCommandClassKey() == 0x84){
			// WAKE_UP
			if (cmd.getCommandKey() == 0x07){
				node.setSleepMode(JWaveNodeSleepMode.SLEEP_MODE_AWAKE);
				sendNext();
			}
		}
		if (cmd.getCommandClassKey() == 0x72){ // COMMAND_CLASS_MANUFACTURER_SPECIFIC 
			if (cmd.getCommandKey() == 0x05){	// REPORT
				evaluateManufactureSpecificCmd(cmd);
			}
			if (cmd.getCommandKey() == 0x07){	// DEVICE_SPECIFIC_REPORT
				evaluateDeviceSpecificCmd(cmd);
			}
		}
		
		if (waitingForApplCommandHandler){
			if (currCmd != null){
				if (currCmd.getCommandClassKey() == cmd.getCommandClassKey()){
					responsesLeft--;
					if (responsesLeft <= 0){
						currCmd = null;
						waitingForApplCommandHandler = false;			
						sendNext();	
					}
				} else {
					JWaveController.log(LogTag.WARN,"NODE "+this.getNode().getNodeId()+" | Wrong response! expected response of type "+currCmd.getCommandClassKey()+" but got response of type "+cmd.getCommandClassKey());
					if (resend < 3){
						resendCurrentCommand();
						resend++;
					}
				}
			} else {
				JWaveController.log(LogTag.DEBUG,"NODE "+getNode().getNodeId()+" | CurrCmd is null -> unable to check if response belongs to currCmd");
			}						
		}
	}
	
	protected void evaluateMultiCmd(JWaveNodeCommand cmd){
		//List<JWaveNodeCommand> cmds = new ArrayList<>();
//		
//		int numberOfCommands = JWaveCommandParameterType.toInteger(cmd.getParamValue(0));
//		byte[] data = cmd.getParamValue(1);
//		
//		if (data == null){
//			JWaveController.log(LogTag.WARN, "received multi command has no data to evaluate");
//			return;
//		}
//		if (data.length <= 0){
//			JWaveController.log(LogTag.WARN, "received multi command has no data to evaluate");
//			return;
//		}
//		int cmdSize;
//		int cmdStart = 0;
//		JWaveNodeCommand tmpCmd;
//		for (int i = 0; i<numberOfCommands; i++) {
//			 cmdSize = data[0+cmdStart];
//			 tmpCmd = generateJWaveNodeCmd(data, cmdStart+1, cmdSize);
//			 if (tmpCmd != null){
//				 evaluateReceivedData(tmpCmd);				 
//			 }
//			 cmdStart = cmdStart + cmdSize;
//		}	
		if (!(cmd instanceof JWaveMultiNodeCommand)){
			JWaveController.log(LogTag.WARN, "evaluating multi command, but nodeCmd is no instance of JWaveMultiNodeCommand");
			return;
		}
		JWaveMultiNodeCommand multiCmd = (JWaveMultiNodeCommand)cmd;
		for (JWaveNodeCommand nc : multiCmd.getNodeCmdList()){
			node.onNodeCmdReceived(nc);
			//evaluateReceivedData(nc);
		}
	}
	
	protected JWaveNodeCommand generateJWaveNodeCmd(byte[] data, int start, int length){
		byte[] nodeCmdValues = new byte[length-2];
		for (int i=0; i<length-2; i++){
			nodeCmdValues[i] = data[start+2+i];
		}
		int commandClassKey = 0xff & data[start];
		int commandKey = data[start + 1];
		JWaveCommand cmd = JWaveDefs.getCommand(commandClassKey,1,commandKey);
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValues(nodeCmdValues);	
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR,"unable to generate NodeCmd from MultiCmd",exc);
			return null;
		}
		JWaveController.log(LogTag.DEBUG,"JWaveNodeCommand generated from MultiCommand - "+nodeCmd.getCommandClass().getName());
		return nodeCmd;
	}
	
	protected void evaluateManufactureSpecificCmd(JWaveNodeCommand cmd){
		try {
			node.manufactureId = JWaveCommandParameterType.toInteger(cmd.getParamValue(0));
			node.productTypeId = JWaveCommandParameterType.toInteger(cmd.getParamValue(1));
			node.productId	   = JWaveCommandParameterType.toInteger(cmd.getParamValue(2));
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// handle that shit! Must be always 3 params for this command
		}
		
	}
	
	protected void evaluateDeviceSpecificCmd(JWaveNodeCommand cmd){
		//
	}
	
	public synchronized void sendCmd(JWaveNodeCommand cmd){
		if (securityEnabled){
			if ((cmd.getCommandClassKey()!=0x98) && (node.supportsCommandClassSecure(cmd.getCommandClassKey()))){		
				try {
					securityProcessor.sendSecure(cmd);
				} catch (Exception exc){
					JWaveController.log(LogTag.DEBUG,"unable to send cmd securely",exc);
				}
				return;
			}
		}
		//JWaveController.log(LogTag.DEBUG,"sending cmd "+cmd.getCommandClass().getName()+" "+cmd.getCommand().getName()+" unsecure");
		cmdToSendBuffer.add(cmd);
		sendNext();		
	}
	
	public synchronized void onTransmissionFailed(){
		if (currCmd != null){
			node.dispatchNodeEvent(new JWaveNodeDataEvent(JWaveEventType.NODE_EVENT_DATA_SEND_FAILED, node,currCmd));
		}		
		// FIXME: Transmission failed. this event is triggered when CAN or NACK were received. 
		waitingForApplCommandHandler = false;
		currCmd = null;
		sendNext();
	}
	
	public synchronized void onTransmissionSuccess(){
		if (currCmd != null){
			node.dispatchNodeEvent(new JWaveNodeDataEvent(JWaveEventType.NODE_EVENT_DATA_SENT, node,currCmd));
		}
		
		if (!waitingForApplCommandHandler){
			currCmd = null;
			this.
			sendNext();
		}
	}
	
	protected void sendNext(){
		
		if (currCmd != null){
			return;
		}	
		
		if (waitingForApplCommandHandler){
			return;
		}
		
		if (cmdToSendBuffer.isEmpty()) {			
			if (securityProcessor.isCmdBufferEmpty()){
				if (mode == NODE_MODE_NORMAL_INTERVIEW){
					if (this.currentInterviewOptions != null) {
						if (this.currentInterviewOptions.isRequestManufactureDetails()) {
							if (getNode().hasManufactureDetails()) {
								mode = NODE_MODE_NORMAL_INTERVIEW_FINISHED;
								node.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_INTERVIEW_FINISHED, node));
							}
						} else {
							mode = NODE_MODE_NORMAL_INTERVIEW_FINISHED;
							node.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_INTERVIEW_FINISHED, node));
						}
					}
				}
			 
				
			}
			return;
		}			
		
		if (this.getNode().isSleeping()){
			JWaveController.log(LogTag.DEBUG,"NODE "+getNode().getNodeId()+" | <- Node is sleeping | BufferSize ="+cmdToSendBuffer.size());
			return;
		}
		
		
		currCmd = cmdToSendBuffer.remove(0);
		resend = 0;
		setWaitingForCommandHandler(currCmd);
		if (currCmd.getCommandClassKey()==0x84){
			if (currCmd.getCommandKey()==0x08){
				JWaveController.log(LogTag.DEBUG,"NODE "+getNode().getNodeId()+" | Node goes to sleep mode");
				node.setSleepMode(JWaveNodeSleepMode.SLEEP_MODE_SLEEPING);
			}
		}
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateSendDataDatagram(node, currCmd,(byte)(node.getNodeId()&0xFF));		
		transceiver.send(datagram);		
		
	}
	
	protected void resendCurrentCommand(){
		if (currCmd == null){
			return;
		}
		JWaveController.log(LogTag.DEBUG,"resending last command");
		JWaveDatagram datagram = JWaveDatagramFactory.generateSendDataDatagram(node, currCmd,(byte)(node.getNodeId()&0xFF));		
		transceiver.send(datagram);		
	}
	
	protected void setWaitingForCommandHandler(JWaveNodeCommand cmd){
		if ((cmd.getCommand().getName().contains("GET_")) ||  (cmd.getCommand().getName().contains("_GET"))){
			waitingForApplCommandHandler = true;			
			responsesLeft = cmd.getExpectedResponses();
			timeOutWatchdog.schedule(new TimeOutTask(), 15000);
			return;
		}
		waitingForApplCommandHandler = false;
	}
	
	protected void setNodeToSleep(){
		
		JWaveCommand cmd = JWaveDefs.getCommand("COMMAND_CLASS_WAKE_UP","WAKE_UP_NO_MORE_INFORMATION");
		if (cmd == null){
			new Exception("NODE "+getNode().getNodeId()+" | unable to find command WAKE_UP_NO_MORE_INFORMATION").printStackTrace();
		}
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		this.sendCmd(nodeCmd);		
		//transceiver.send(JWaveDatagramFactory.generateSendDataDatagram(node, nodeCmd,(byte)(node.getNodeId()&0xFF)));
		
	}
	
	protected void setSleepTimeInterval(int seconds){
		JWaveCommand cmd = JWaveDefs.getCommand("COMMAND_CLASS_WAKE_UP","WAKE_UP_INTERVAL_SET");
		if (cmd == null){
			new JWaveException("NODE "+getNode().getNodeId()+" | unable to find command WAKE_UP_INTERVAL_SET for class COMMAND_CLASS_WAKE_UP");
		}
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValue(0x00,seconds);
			nodeCmd.setParamValue(0x01,0x01);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}		
	}
	
	protected class TimeOutTask extends TimerTask{		
		public void run(){			
			if (waitingForApplCommandHandler){
				JWaveController.log(LogTag.WARN,"NODE "+getNode().getNodeId()+" | Cancel waiting for response "+currCmd.getCommand().getName());
				if (mode == 1){
					if (node != null){
						node.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_INTERVIEW_ERROR,node));
					} else {
						node.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_DATA_NO_RESPONSE,node));						
					}					
				}
				// FIXME: inform currCmd that it not wasn't answering
				
				currCmd=null;
				
				waitingForApplCommandHandler = false;
				
				sendNext();
				this.cancel();
			}
		}
	}

	public void setSecurityEnabled(boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
		
	}
	
	
	
}
