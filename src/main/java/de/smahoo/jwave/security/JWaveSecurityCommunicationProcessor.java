package de.smahoo.jwave.security;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.JWaveException;
import de.smahoo.jwave.cmd.JWaveCommand;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.cmd.JWaveNodeCommandFactory;
import de.smahoo.jwave.cmd.report.JWaveReport;
import de.smahoo.jwave.cmd.report.JWaveReportFactory;
import de.smahoo.jwave.cmd.report.JWaveReportSecurityCommandsSupported;
import de.smahoo.jwave.cmd.report.JWaveReportSecurityNonce;
import de.smahoo.jwave.cmd.report.JWaveReportSecurityScheme;
import de.smahoo.jwave.event.JWaveErrorEvent;
import de.smahoo.jwave.event.JWaveEventType;
import de.smahoo.jwave.node.JWaveNode;
import de.smahoo.jwave.utils.ByteArrayGeneration;
import de.smahoo.jwave.utils.logger.LogTag;


/**
 * The SecurityCommunicationProcessor is responsible for a secure communication with any node using the Z-Wave security concept. 
 * It handles the secure include of a node, the nonces, decryption as well as encryption.
 * 
 * Make use of SecurityCommunicationProcessor by setting a JWaveSecurityCommunicationProcessorListener for callback
 * as well as calling public method processSecurityNodeCommand(...)
 * 
 * @author Mathias Runge
 *
 */
public class JWaveSecurityCommunicationProcessor {
	
	
	private final static int 	SIZE_CRYPTOBLOCK = 16;
	private final static long 	TIMEOUT_NONCE_REQUEST = 5000;
	private final static int	TIMEOUT_SECURITY_INCLUSION = 10000;
	private final static int    MAX_NONCE_REQUESTS_BEFORE_STOP = 2;
	
	/*
	 * whenever node with security capability is added than this processor needs to be informed
	 * 
	 * 		- processor is including secure node (sending network key, getting nonce)
	 * 		- list nonces received from node
	 * 		- next nonce to be used for each node
	 * 		- cmdclasses that are supported for secure mode need to be stored somewhere
	 * 
	 * when sendSecure is called, generate a JWaveSecurityMessageEncapsulation 
	 * 
	 */
	boolean nonceRequested = false;
	
	JWaveSecurityNetworkKey networkKey;
	
	JWaveSecurityScheme securityScheme;
	JWaveNodeCommandFactory cmdFactory;
	JWaveSecurityCommunicationProcessorListener procListener;
	JWaveNodeCommand currentCmdToSend;

	Map<Integer,JWaveSecurityNonce> usedReceiversNonces;
	Map<Integer,JWaveSecurityNonce> usedSendersNonces;
	JWaveSecurityNonce currentReceiversNonce;
	int nonceRequestedCount;
	//JWaveSecurityNonce currentSendersNonce;
	JWaveNode node;
	
	List<JWaveNodeCommand> cmdsToSend;
	List<JWaveSecurityMessageEncapsulator> msgEncToSend;
	JWaveSecurityMessageEncapsulator currentMessageEncapsulatorForSending;
	JWaveSecurityMessageEncapsulationBuilder currentMessageBuilder;
	JWaveSecurityCryptoEngine cryptoEngine;
	
	Timer nonceTimer;
	Timer secureTimeout;
	
	boolean includeIsRunning = false;
	
	
	public JWaveSecurityCommunicationProcessor(JWaveNode node, byte[] networkKey, JWaveNodeCommandFactory cmdFactory, JWaveSecurityCommunicationProcessorListener listener){
		
		this.cmdFactory = cmdFactory;
		this.procListener = listener;
		currentReceiversNonce = null;
		usedReceiversNonces = new HashMap<Integer,JWaveSecurityNonce>();
		usedSendersNonces   = new HashMap<Integer,JWaveSecurityNonce>();
		cmdsToSend = new ArrayList<JWaveNodeCommand>();
		currentMessageBuilder = null;
		currentMessageEncapsulatorForSending = null;
		currentCmdToSend = null;
		msgEncToSend = new ArrayList<JWaveSecurityMessageEncapsulator>();
		this.node = node;
		cryptoEngine = new JWaveSecurityDefaultCryptoEngine();
		try {
			this.networkKey = cryptoEngine.generateNetworkKey(networkKey);
		} catch (Exception exc){
			JWaveController.log(LogTag.DEBUG,"Unable to initiate JWaveSecurityCommunicationProcessor. No Network key could be generated."+exc.getMessage(),exc);
		}
		nonceTimer = null;
		secureTimeout = null;
		nonceRequestedCount = 0;
	}
	
	public void setCryptoEngine(JWaveSecurityCryptoEngine cryptoEngine){
		this.cryptoEngine = cryptoEngine;
	}
	
	
	// +++++++++++++++++++++++++++++++++++++     SECURITY CMD PROCESSING   ++++++++++++++++++++++++++++++++++++
	
	/**
	 * Processing the node command of command class security
	 * @param secNodeCmd
	 * @throws JWaveSecurityException
	 */
	public void processSecurityNodeCommand(JWaveNodeCommand secNodeCmd) throws JWaveSecurityException{
		
		if (secNodeCmd.getCommandClassKey() != 0x98){
			// it's not a security message
			throw new JWaveSecurityException("Given nodeCmd is of '"+secNodeCmd.getCommandClass().getName()+"' ("+
											 Integer.toHexString(secNodeCmd.getCommandClassKey())+"). A nodeCmd of type 'COMMAND_CLASS_SECURITY (0x98)' is expected");
		}
		
		switch (secNodeCmd.getCommandKey()){
			case 0x80:	// NONCE REPORT
					processNonceReport(secNodeCmd);
				break;
			case 0x40:// NONCE GET
					sendNonce(getNewNonce());
				break;
			case 0x05: // SCHEME REPORT
					processSchemeReport(secNodeCmd);
				break;
			case 0x03: // COMMANDS SUPPORTED REPORT
					processSupportedReport(secNodeCmd);					
				break;
			case 0x07: // NETWORK KEY VERIFIED
					processNetworkKeyVerified();
					
				break;
			case 0xC1: // ENRYPTED MESSAGE and new nonce requested					
					processEncryptedMessage(secNodeCmd);
					sendNonce(getNewNonce());
				break;
			case 0x81: // ENRYPTED MESSAGE
					processEncryptedMessage(secNodeCmd); 						
				break;
			default:
					throw new JWaveSecurityException("Command ("+secNodeCmd.getCommand().getName()+") is not handled yet. Unable to process given nodeCmd");				

		}	
	}
	
	
	private void processEncryptedMessage(JWaveNodeCommand secNodeCmd){
		try {
			
			
			byte[] decryptionResult = decrypt(secNodeCmd);
			if (decryptionResult == null){
				JWaveController.log(LogTag.ERROR, "unable do decrypt message");
				return;
			}
			if ((decryptionResult[0] == 0x00) ){
	        	byte[] cmdBytes = new byte[decryptionResult.length-1];
	        	System.arraycopy(decryptionResult, 1, cmdBytes, 0,cmdBytes.length);
	        	JWaveNodeCommand nodeCmd = cmdFactory.generateNodeCmd(cmdBytes);
	        	procListener.onCommandReceived(nodeCmd);
	        	sendNext();
	        } else {
	        	
	        	if (currentMessageBuilder == null){
	        		currentMessageBuilder = new JWaveSecurityMessageEncapsulationBuilder(decryptionResult);
	        	} else {
	        		currentMessageBuilder.addEncryptedData(decryptionResult);
	        		if (currentMessageBuilder.isComplete()){
	        			byte[] bytes = currentMessageBuilder.getCompleteCmdData();
	        			JWaveController.log(LogTag.DEBUG,"building node cmd with bytes "+ByteArrayGeneration.toHexString(bytes));
	        			currentMessageBuilder = null;
	        			JWaveNodeCommand nodeCmd = cmdFactory.generateNodeCmd(bytes);
	        			
	    	        	procListener.onCommandReceived(nodeCmd);
	    	        	
	    	        	sendNext();
	        		}
	        	}
	        	
	        	JWaveController.log(LogTag.DEBUG, "Message Encryption is sequenced...  waiting until all message parts are received");
	        }
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, "unable do decrypt message",exc);
		}
	}
	
	private void processSupportedReport(JWaveNodeCommand nodeCmd){
		JWaveController.log(LogTag.DEBUG,"processing Supported Report message");
		try {
			JWaveReport rep = JWaveReportFactory.generateSecurityReport(nodeCmd);
			if (rep instanceof JWaveReportSecurityCommandsSupported){
				JWaveReportSecurityCommandsSupported supportedRep = (JWaveReportSecurityCommandsSupported)rep;
				
				if (supportedRep.getReportsToFollow() == 0){					
					procListener.onSecureInclusionFinished(supportedRep.getAllCommandClasses(cmdFactory.getCmdClassSpecification()));
				}	
				
			}
		} catch (Exception exc){
			JWaveController.log(LogTag.DEBUG, "unable to generate Security Report",exc);
		}
	}
	
	
	/**
	 * With a network key verified message, the official security inclusion process is finished. Before sending CNTRL_EVENT_SECURITY_INCLUDE_SUCCESS event,
	 * all supported JWave command classes are requested. 
	 * @throws JWaveSecurityException 
	 */
	private void processNetworkKeyVerified() throws JWaveSecurityException{
		includeIsRunning = false;
		stopSecurityTimoutTimer();
		sendSecure(cmdFactory.generateCmd_SecuritySupportedGet());		
	}
	
	/**
	 * When a NonceReport was received, it will be set to currentReceiversNonce. 
	 * @param nodeCmd
	 * @throws JWaveSecurityException
	 */
	private void processNonceReport(JWaveNodeCommand nodeCmd) throws JWaveSecurityException{
		nonceRequested = false;
		nonceRequestedCount = 0;
		try {
			JWaveReport rep = JWaveReportFactory.generateSecurityReport(nodeCmd);
			if (rep instanceof JWaveReportSecurityNonce){
				JWaveReportSecurityNonce nonceRep = (JWaveReportSecurityNonce)rep;
				updateReceiversNonce(nonceRep.getNonce());
			} else {
				 throw new JWaveSecurityException("Report is not an instance of JWaveReportSecurityNonce. Unable to process this command");
			}
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR,"Unable to generate NonceGetReport out of "+ByteArrayGeneration.toHexString(nodeCmd.toByteArray()),exc);
			throw new JWaveSecurityException("Unable to generate NonceGetReport",exc);
		}
	}
	
	
	private void processSchemeReport(JWaveNodeCommand nodeCmd) throws JWaveSecurityException{
		try {
			JWaveReport rep = JWaveReportFactory.generateSecurityReport(nodeCmd);
			if (rep instanceof JWaveReportSecurityScheme){
				JWaveReportSecurityScheme schemeRep = (JWaveReportSecurityScheme)rep;
				setSecurityScheme(schemeRep.getSecurityScheme());
			} else {
				 throw new JWaveSecurityException("Report is not an instance of JWaveReportSecurityScheme. Unable to process this command");
			}
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR,"Unable to generate SchemeReport out of "+ByteArrayGeneration.toHexString(nodeCmd.toByteArray()),exc);
			throw new JWaveSecurityException("Unable to generate SchemeReport",exc);
		}
	}
	
	private JWaveNodeCommand getNextCmdToSend(){
		if (!hasCmdsToSend()){
			return null;
		}
		synchronized(cmdsToSend){
			return cmdsToSend.remove(0);
		}
	}
	
	private boolean hasCmdsToSend(){
		synchronized(cmdsToSend){
			 return !cmdsToSend.isEmpty();
		}
	}
	
	// --------------------------------------     SECURITY CMD PROCESSING   -------------------------------------
	
	// ++++++++++++++++++++++++++++++++++++++++++     NONCE HANDLING   ++++++++++++++++++++++++++++++++++++++++++
	
	
	
	/**
	 * Generates a new Nonce which will be used for next encryption 
	 * @return new generated nonce
	 */
	
	private synchronized JWaveSecurityNonce getNewNonce(){
		
		JWaveSecurityNonce nonce;
		byte[] nonceBytes = new byte[8];
		// generate 8 random bytes (0 - 255)
		for (int i = 0; i<8; i++){
			nonceBytes[i] = (byte)(Math.random() * (0xFF));
		}
		JWaveController.log(LogTag.DEBUG,"generating new nonce "+ByteArrayGeneration.toHexString(nonceBytes));
		nonce = new JWaveSecurityNonce(nonceBytes);
		storeUsedSendersNonce(nonce);
		return nonce;
	}
	
	
	
	/**
	 * stores the currently used nonce to the cache and sets currentSendersNonce to null, so that a new Nonce needs to be generated
	 */
	private void storeUsedSendersNonce(JWaveSecurityNonce nonce){
		System.out.println("storing nonce (id="+nonce.getNonceId()+")"+ByteArrayGeneration.toHexString(nonce.getBytes()));
		usedSendersNonces.put(nonce.getNonceId(),nonce);	
			
	}
	
	private JWaveSecurityNonce getSenderNonce(int nonceId){
		JWaveSecurityNonce nonce = usedSendersNonces.get(nonceId);
		System.out.println("getSendersNonce (id="+nonceId+")"+ByteArrayGeneration.toHexString(nonce.getBytes()));
		return nonce;
	}
	
	/**
	 * Generates a new NONCE_GET message of command class COAMMAND_CLASS_SECURITY
	 */
	private void requestNewNonce(){
		JWaveController.log(LogTag.DEBUG,"requesting new nonce from receiver");
		if (nonceRequested){
			JWaveController.log(LogTag.DEBUG,"requesting new nonce canceled, because it was already requested");
			return;
		}
		currentReceiversNonce = null;
		nonceRequestedCount++;
		procListener.onSendCommand(cmdFactory.generateCmd_SecurityNonceGet());
		setNonceAsRequested();
	}
	
	/**
	 * Will be called when no nonce was sent by node. Thus, the message encapsulation can't be sent. All cmds in queue will be thrown away
	 */
	private void onNoNonceReceived(){
		cmdsToSend.clear();
		procListener.onJWaveError(new JWaveErrorEvent(JWaveEventType.ERROR_NODE_COMMUNICATION, "Security Node was not sending Nonce. Commands in queue can't be sent and will be thrown away.", null));
	}
	
	
	
	/**
	 * new Receivers Nonce will be stored. When node is still in secure include mode, the network key will be sent immediately. Otherwise,
	 * if there are still commands for sending in the cache, the next command will be sent.
	 * @param nonce the new received receivers nonce
	 */
	private synchronized void updateReceiversNonce(JWaveSecurityNonce nonce){	
		JWaveController.log(LogTag.DEBUG,"updating current receivers nonce "+ByteArrayGeneration.toHexString(nonce.getBytes()));
		this.currentReceiversNonce = nonce;
		nonceRequested = false;
		// TODO start nonce alive timer		
		
		// if inlucsion mode
		if (this.includeIsRunning){
			JWaveController.log(LogTag.DEBUG,"In inclusion process, network key will be transmittet now");
			setSecurityTimeoutTimer();
			sendNetworkKey();			
		} else {			
			sendNext();			
		}		
	}
	
	private void sendNext(){
		JWaveController.log(LogTag.DEBUG,"sending next command securely called");
		if (currentCmdToSend != null){
			JWaveController.log(LogTag.DEBUG,"return, because still sending a command ");
			return;
		}
		
		if (currentReceiversNonce == null){
			JWaveController.log(LogTag.DEBUG,"no nonce available (nonce requested = "+nonceRequested+")");
			if (!nonceRequested){
				requestNewNonce();			
			}			
			JWaveController.log(LogTag.DEBUG,"return, because no nonce available");
			return;
		}
		if (!currentReceiversNonce.isValid()){
			JWaveController.log(LogTag.DEBUG,"current nonce is invalid");
			nonceRequested = false;
			requestNewNonce();
			return;
		}
		currentCmdToSend = getNextCmdToSend();
		if (currentCmdToSend != null){
			JWaveController.log(LogTag.DEBUG,"next command is "+currentCmdToSend.getCommand().getName());
			try {
				sendCurrentNodeCmd();
			} catch (Exception exc){
				JWaveController.log(LogTag.ERROR, "unable to send message securely",exc);
				currentCmdToSend = null;
				sendNext();
			}
		} else {
			JWaveController.log(LogTag.DEBUG,"no node commands left for sending");
		}
	}
	
	// ------------------------------------------     NONCE HANDLING   ------------------------------------------
		
	// ++++++++++++++++++++++++++++++++++++++++++++    MESSAGE SENDING   ++++++++++++++++++++++++++++++++++++++++++++
	
	/**
	 * Starting the secure inclusion process (see 3.27 at Command Class Definitions document) 
	 *  
	 */
	public void startSecureIncludeProcess(){
		includeIsRunning = true;
		setSecurityTimeoutTimer();
		procListener.onSendCommand(cmdFactory.generateCmd_SecuritySchemeGet());
	}
	
	private void onSecurityTimeout(){
		if (includeIsRunning){
			includeIsRunning = false;
			procListener.onSecureInclusionError("TIMEOUT! Node ("+node.getNodeId()+") was not responding within "+TIMEOUT_SECURITY_INCLUSION+"ms during secure interview.");
		}
	}
	
	private void stopSecurityTimoutTimer(){
		if (secureTimeout != null){
			try {
				secureTimeout.cancel();
				secureTimeout.purge();
			} catch (Exception exc){
				// do nothing
			}
		}
	}
	
	private void setSecurityTimeoutTimer(){
		stopSecurityTimoutTimer();
		secureTimeout = new Timer();
		secureTimeout.schedule(new SecurityTimeoutTask(),TIMEOUT_SECURITY_INCLUSION);
	}
	
		
	private void setSecurityScheme(JWaveSecurityScheme secScheme){
		this.securityScheme = secScheme;
		if (this.includeIsRunning){
			setSecurityTimeoutTimer();
			requestNewNonce();
		}
	}
		
	
	private void sendNetworkKey(){
		JWaveNodeCommand setKeyCmd = cmdFactory.generateCmd_SecurityKeySet(networkKey.getKey());
		
		byte[] tmpKeyBytes = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		
		try {

			JWaveSecurityNetworkKey temporaryKey = cryptoEngine.generateNetworkKey(tmpKeyBytes);
			sendSecure(setKeyCmd,temporaryKey);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR,"unable to send NetworkKey",exc);
			
		}
	}

	
	public void sendSecure(JWaveNodeCommand nodeCmd) throws JWaveSecurityException{
		synchronized(cmdsToSend){
			this.cmdsToSend.add(nodeCmd);
		}
			
		sendNext();
	}
	
	private void sendCurrentNodeCmd() throws JWaveSecurityException{
		JWaveController.log(LogTag.DEBUG,"Sending current command "+currentCmdToSend.getCommand().getName());
		sendSecure(currentCmdToSend,networkKey);
		currentCmdToSend = null;
	}
	
	private void sendSecure(JWaveNodeCommand nodeCmd, JWaveSecurityNetworkKey networkKey) throws JWaveSecurityException{
		sendNodeCmd(new JWaveSecurityMessageEncapsulator(nodeCmd, networkKey));		
	}
	
	private void sendNodeCmd(JWaveSecurityMessageEncapsulator msgEncapsulator) throws JWaveSecurityException{	
		
		if (currentMessageEncapsulatorForSending != null){
			synchronized (msgEncToSend) {
				msgEncToSend.add(msgEncapsulator);
			}
			return;
		}
		
		
		if (msgEncapsulator.isSecondFrameNeeded()){
			currentMessageEncapsulatorForSending = msgEncapsulator;
		}
		
		JWaveSecurityMessageEncapsulation msgEnc;
		
		try {
			msgEnc = encrypt(msgEncapsulator.getFirstPayloadPart(),msgEncapsulator.getNodeCmd(),msgEncapsulator.getNetworkKey());
		} catch (Exception exc){
			throw new JWaveSecurityException("unable to generate SecurityMessageEncapsulation",exc);
		}
		
		currentReceiversNonce = null;
		currentCmdToSend = null;
		procListener.onSendCommand(msgEnc);
//		if (!includeIsRunning){
//			sendNext();
//		}
	}
	
	private void sendNonce(JWaveSecurityNonce nonce){		
		try {
			JWaveNodeCommand cmd = cmdFactory.generateCmd_SecurityNonceReport(nonce);
			procListener.onSendCommand(cmd);
		} catch (Exception exc){
			JWaveController.log(LogTag.DEBUG,"unable to generate nonce report");
		}
	}
	// --------------------------------------------    MESSAGE SENDING   --------------------------------------------

	// ++++++++++++++++++++++++++++++++++++++     ENCRYPTION / DECRYPTION   +++++++++++++++++++++++++++++++++++++
	
	
	private byte[] decrypt(JWaveNodeCommand secNodeCmd) throws JWaveException{
		if (!(secNodeCmd instanceof JWaveSecurityMessageEncapsulation)){
			throw new JWaveSecurityException("Decryption of nodeCmd failed. NodeCmd is not of type JWaveSecurityMessageEncapsulation!");
		}
		
		JWaveSecurityMessageEncapsulation msgEnc = (JWaveSecurityMessageEncapsulation)secNodeCmd;
		
		int nonceId = msgEnc.getNonceId()&0xFF;
		
		JWaveSecurityNonce usedNonce = getSenderNonce(nonceId);
		
		if (usedNonce == null){
			JWaveController.log(LogTag.WARN,"Node used an unknown nonce (id="+Integer.toHexString(nonceId)+"). Message will be ignored.");
			return null;
		}
		
		if (!usedNonce.isValid()){
			JWaveController.log(LogTag.WARN,"Used nonce is not valid anymore.Message will be ignored");
		}
		
		byte[] initializationVector = new byte[SIZE_CRYPTOBLOCK];
		System.arraycopy(msgEnc.getInitializationVector(), 0, initializationVector, 0,8);
		System.arraycopy(usedNonce.getBytes(),0,initializationVector,8,8);
		
		
		//byte[] decryptionResult = cryptoEngine.decrypt(msgEnc.getEncryptedPayload(), networkKey, initializationVector);
		return cryptoEngine.decrypt(msgEnc.getEncryptedPayload(), networkKey, initializationVector);
		
	
	}
	
	private JWaveSecurityMessageEncapsulation encrypt(byte[] payload, JWaveNodeCommand nodeCmd, JWaveSecurityNetworkKey networkKey) throws JWaveSecurityException{

		JWaveController.log(LogTag.DEBUG,"payload before encryption  "+ByteArrayGeneration.toHexString(payload));
		
		// generating initialization vector (iv)
		byte[] initializationVector = cryptoEngine.generateInitializationVector(getNewNonce(),currentReceiversNonce);	
		
		//byte[] encryptedPayload = encrypt(payload, networkKey.getEncryptionKey(), initializationVector);
		byte[] encryptedPayload = cryptoEngine.encrypt(payload, networkKey.getEncryptionKey(), initializationVector);
		
		//JWaveController.log(LogTag.DEBUG,"encrypted Payload         "+ByteArrayGeneration.toHexString(encryptedPayload));
		JWaveCommand cmd;
		if (hasCmdsToSend()){
			cmd = cmdFactory.getCmdClassSpecification().getCommand("COMMAND_CLASS_SECURITY","SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET");
			setNonceAsRequested();
		} else {
			cmd = cmdFactory.getCmdClassSpecification().getCommand("COMMAND_CLASS_SECURITY","SECURITY_MESSAGE_ENCAPSULATION");
			
		}
				
		//byte[] mac = generateMAC(initializationVector, networkKey, (byte)cmd.getKey(), encryptedPayload);
		byte[] mac = cryptoEngine.generateMAC((byte)node.getNodeId(), initializationVector, networkKey, (byte)cmd.getKey(), encryptedPayload);
		JWaveSecurityMessageEncapsulation msgEnc = new JWaveSecurityMessageEncapsulation(cmd, nodeCmd, initializationVector, encryptedPayload, (byte)currentReceiversNonce.getNonceId(), mac);
			
		return msgEnc;
	}

	public boolean isCmdBufferEmpty() {
		synchronized (cmdsToSend) {
			return this.cmdsToSend.isEmpty();
		} 
	}
	
	 
	// --------------------------------------     ENCRYPTION / DECRYPTION   -------------------------------------
	
	// ++++++++++++++++++++++++++++++++++++++++++   NONCE HANDLING   ++++++++++++++++++++++++++++++++++++++++++++
	
	private void setNonceAsRequested(){
		this.nonceRequested = true;
		try {			
			if (nonceTimer != null){				
				nonceTimer.cancel();
				nonceTimer.purge();
			}
		} catch (Exception exc){
			// do nothing			
		}
		nonceTimer = new Timer();
		JWaveController.log(LogTag.WARN, "Scheduling new NonceTimer");
		nonceTimer.schedule(new WatchdogTimerTask(), TIMEOUT_NONCE_REQUEST);
		
	}
	
	private class WatchdogTimerTask extends TimerTask{
		@Override
		public void run() {
			JWaveController.log(LogTag.WARN, "NonceTimerStarted");
			if (nonceRequested){
				nonceRequested = false;
				if (nonceRequestedCount < MAX_NONCE_REQUESTS_BEFORE_STOP){
					JWaveController.log(LogTag.WARN, "No Nonce was sent, will requested again");
					requestNewNonce();
				} else {
					onNoNonceReceived();
				}
			}			
		}
		
	}
	
	private class SecurityTimeoutTask extends TimerTask{

		@Override
		public void run() {
			onSecurityTimeout();			
		}
		
	}
}
