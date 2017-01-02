package de.smahoo.jwave.simulation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import de.smahoo.jwave.io.JWaveConnection;
import de.smahoo.jwave.io.JWaveDatagramFactory;
import de.smahoo.jwave.utils.ByteArrayGeneration;

/**
 * MockJWaveConnection emulates input and output streams to simulate a serial z-wave controller. 
 * It is possible to add reply rules. A reply rule sends predefined streams when a related message
 * was received (see StreamReplyRule.java).
 * 
 * 
 * @author mathias.runge@domoone.de
 *
 */
public class JWaveMockedConnection implements JWaveConnection {
	InputStream  inStream  	= null;
	OutputStream outStream	= null;
	
	HashMap<String,JWaveAbstractStreamRule> streamRules = null;
	
	
	JWaveStreamReplyRule currentRuleToExecute = null;
	
	List<String> streamsToSend;
	HashMap<String,String> paramHash = null;
	
	// receiving bytes
	int[] inputBuffer = new int[1024];
	int bufferSize = 0;
	boolean receivingMessage = false;
	int messageLength = -1;
	// sending bytes
	int[] currBytesToSend = null;
	int currentByteIndex = -1;
	boolean sendingAck = false;
	
	
	
	public JWaveMockedConnection(){
		streamRules = new HashMap<String, JWaveAbstractStreamRule>();
		paramHash = new HashMap<String, String>();
		streamsToSend = new ArrayList<String>();
		
		
		inStream = new InputStream() {
			
			@Override
			public int available() throws IOException {
				int bytes = getAvailableBytesToSend();				
				return bytes;
			}
			
			@Override
			public int read() throws IOException {				
				return getNextBytetoSend();				
			}
		};
		
		outStream = new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				newByteReceived(b);		
			}
		};
	}
		

	
	public void clean(){
		// general
		streamRules = new HashMap<String, JWaveAbstractStreamRule>();
		paramHash = new HashMap<String, String>();
		streamsToSend = new ArrayList<String>();
		currentRuleToExecute = null;
		
		// receiving bytes
		inputBuffer = new int[1024];
		bufferSize = 0;
		receivingMessage = false;
		messageLength = -1;
		// sending bytes
		currBytesToSend = null;
		currentByteIndex = -1;
		sendingAck = false;		
	}
	

	public void addStreamRule(JWaveAbstractStreamRule rule){
		streamRules.put(rule.getConditionStr(),rule);
	}
	
	
	
	protected void setCurrentByteIndex(int index){
		synchronized(this){
			currentByteIndex = index;
		}
	}
	
	protected int getCurrentByteIndex(){
		synchronized(this) {
			return currentByteIndex;
		}
	}
	
	protected synchronized void send(int[] bytes){
		if (getCurrentByteIndex() > 0){
		
		} else {
			sendBytes(bytes);
		}
	}
	
	protected synchronized void sendBytes(int[] bytes){
		this.currBytesToSend = bytes;
		setCurrentByteIndex(0);
	}


	@Override
	public InputStream getInputStream() {
		return inStream;
	}


	@Override
	public OutputStream getOutputStream() {
		return outStream;
	}
	
	// ##############  SENDING Z-WAVE BYTES ##################
	
	
	
	protected int getAvailableBytesToSend(){
		synchronized(this){
			if (sendingAck){
				return 1;
			}
			if (currBytesToSend != null){
				return currBytesToSend.length - currentByteIndex;
			}
			return 0;
		}
	}
	
	protected int  getNextBytetoSend(){	
		synchronized(this){			
			if (sendingAck){				
				sendingAck = false;
				return 0x06;
			}
			if (currBytesToSend != null){
				int b = currBytesToSend[currentByteIndex];					
				currentByteIndex++;
				if (currentByteIndex >= currBytesToSend.length){
					currentByteIndex = 0;
					currBytesToSend = null;					
					setNextBytesToSend();
				}
				return b;
			}
		}		
		
		return -1;
	}
	
	protected void addRuleToExecution(JWaveStreamReplyRule rule){
		synchronized(this){
			String tmp = rule.getFirst();
			
			this.streamsToSend.add(tmp);
			while (rule.hasNext()){
				tmp = rule.getNext();
				
				//this.streamsToSend.add(rule.getNext());
			}			
			if (currBytesToSend == null){
				setNextBytesToSend();
			}
		}
	}
	
	
	
	protected void setNextBytesToSend(){
		synchronized(this){			
			if (streamsToSend.isEmpty()){
				return;
			}
			String currStream = streamsToSend.remove(0);
			currBytesToSend = generateIntArray(currStream);
			currentByteIndex = 0;
		}
	}
	
	protected int[] generateIntArray(String stream){
		StringTokenizer tok = new StringTokenizer(stream," ");
		int[] bytes = new int[tok.countTokens()];
		int index = 0;
		String tmp;
		boolean calcLRC = false;
		while (tok.hasMoreElements()){
			tmp = tok.nextToken();
			if (tmp.contains("%")){
				tmp = paramHash.get(tmp);
			}
			if (tmp.equalsIgnoreCase("#LRC")){
				tmp = "00";
				calcLRC = true;
			}
			bytes[index] = Integer.parseInt(tmp,16);
			index++;
		}			
		if (calcLRC){
			bytes[bytes.length-1] = JWaveDatagramFactory.calculateLRC(bytes,01,bytes.length);
		}
		return bytes;
	}
	
	public void send(String byteStr){
		synchronized (this) {
			streamsToSend.add(byteStr);
			if (currBytesToSend == null){
				setNextBytesToSend();
			}
		}
	}
	
	// ##############  RECEIVING Z-WAVE BYTES ################
	
	
	
	
	protected void newByteReceived(int b){		
		if (bufferSize == 0){
			switch(b){
			case 0x06 : 
				onAckReceived();
				return;
			case 0x01 :
				receivingMessage = true;				
				break;
			}			
		}
		
		
		if (!receivingMessage){
			return;
		}
		
		if (bufferSize == 1){
			messageLength = b;
		}
		
		inputBuffer[bufferSize] = b;		
		bufferSize++;
			
		if ((messageLength > 0)&&(bufferSize == messageLength+2)&&(bufferSize > 0)){
			evaluateMessage();
			bufferSize = 0;
			messageLength = -1;
		}		
	}
	
	protected void onAckReceived(){
		//System.out.println("received ACK");
		// prepare next message for sending from current StreamReactionRule
	}
	
	protected void evaluateMessage(){
		String message = ByteArrayGeneration.toHexString(inputBuffer, 0,bufferSize);
		//System.out.println("received message : " +message);
		synchronized(this){
			sendingAck = true;
		}		
		
		// adding standard CMD_JWave_SEND_DATA replies
		int[] array = ByteArrayGeneration.generateIntArray(message);
		if (array.length > 4){
			if (array[3]== 0x13){
				streamsToSend.add("01 04 01 13 01 e8");
				streamsToSend.add("01 05 00 13 "+getFuncIdStr(array)+" 00 #LRC");
				setNextBytesToSend();
			}
		}
		
		List<JWaveAbstractStreamRule> rules = getRules(message);
		if (!rules.isEmpty()){
			for (JWaveAbstractStreamRule rule : rules){
			
				if (rule instanceof JWaveStreamReplyRule){
				
					addRuleToExecution((JWaveStreamReplyRule)rule);
				}
				if (rule instanceof JWaveStreamNotificationRule){
					((JWaveStreamNotificationRule)rule).informListener(message);
				}
			}
		}		
	}
	
	protected String getFuncIdStr(int[] array){
		String str = Integer.toHexString(array[array.length-3]);
		if (str.length()<2){
			str = "0"+str;
		}
		return str;
	}
	
	protected List<JWaveAbstractStreamRule> getRules(String message){
		
		List<JWaveAbstractStreamRule> rules = new ArrayList<JWaveAbstractStreamRule>();
		
		for (String condition : streamRules.keySet()){			
			if (condition.equalsIgnoreCase(message)){
				//return streamRules.get(condition);
				rules.add(streamRules.get(condition));
				
			}
			StringTokenizer tokMessage = new StringTokenizer(message," ");
			StringTokenizer tokCondition = new StringTokenizer(condition," ");
			if (tokCondition.countTokens() == tokMessage.countTokens()){
				String itemCondition;
				String itemMessage;
				boolean hasParams = false;
				boolean ruleMatches = true;
				
				while (tokCondition.hasMoreTokens()){
					itemCondition = tokCondition.nextToken();
					itemMessage = tokMessage.nextToken();
					if ((!itemCondition.equalsIgnoreCase("##")) && (!itemCondition.equalsIgnoreCase("#LRC"))){ // when no wildcard
						if (itemCondition.contains("%")){
							hasParams = true;
						} else {
							ruleMatches = ruleMatches && (itemCondition.equalsIgnoreCase(itemMessage)); 
						}
					} 
				}
				if (ruleMatches){
					if (hasParams){
						setParams(condition,message);
					}
					//return streamRules.get(condition);
					rules.add(streamRules.get(condition));
				}
			}
			
			
		}
		return rules;		
	}
	
	protected void setParams(String condition, String message){
		StringTokenizer tokMessage = new StringTokenizer(message," ");
		StringTokenizer tokCondition = new StringTokenizer(condition," ");
		String itemCondition;
		String itemMessage;
		while (tokCondition.hasMoreTokens()){
			itemCondition = tokCondition.nextToken();
			itemMessage   = tokMessage.nextToken();
			if (itemCondition.contains("%")){
				paramHash.put(itemCondition, itemMessage);
			}
		}
	}
	
}
