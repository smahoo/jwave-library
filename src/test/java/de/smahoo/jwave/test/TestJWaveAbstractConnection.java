package de.smahoo.jwave.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.JWaveControllerMode;
import de.smahoo.jwave.cmd.JWaveCommandClassSpecification;
import de.smahoo.jwave.cmd.JWaveNodeCommandFactory;
import de.smahoo.jwave.event.JWaveEvent;
import de.smahoo.jwave.event.JWaveEventListener;
import de.smahoo.jwave.event.JWaveEventType;
import de.smahoo.jwave.node.JWaveNodeFactory;
import de.smahoo.jwave.simulation.JWaveMockedConnection;
import de.smahoo.jwave.simulation.JWaveStreamReplyRule;

public abstract class TestJWaveAbstractConnection {

	JWaveController cntrl = null;
	JWaveNodeCommandFactory factory =  null;
	JWaveNodeFactory  nodeFactory = null;
	JWaveMockedConnection connection = null;
	List<JWaveEvent> eventList = null;
	
	@Before
	public void init() throws Exception{		
		//JWaveController.doLogging(true);		
		String sep = System.getProperty("file.separator");
		String filename = System.getProperty("user.dir")+sep+"cnf"+sep+"cmd_classes.xml";
		JWaveCommandClassSpecification spec = null;
		spec = new JWaveCommandClassSpecification(filename);

		
		factory = new JWaveNodeCommandFactory(spec);
		nodeFactory = new JWaveNodeFactory(spec);
		eventList = new ArrayList<JWaveEvent>();	
		connection = new JWaveMockedConnection();
		cntrl = new JWaveController(spec);
		cntrl.addCntrlListener(new JWaveEventListener() {
			
			@Override
			public void onJWaveEvent(JWaveEvent event) {				
				synchronized(eventList){
					eventList.add(event);
				}
				evalJWaveEvent(event);
			}
		});
		addStandardControllerReplyRules();		
		prepare();
	}
	
	
	protected void addStandardControllerReplyRules(){		
		JWaveStreamReplyRule rule = new JWaveStreamReplyRule("01 03 00 15 e9");  // CMD_JWave_GET_VERSION
		rule.addReaction("01 10 01 15 5a 2d 57 61 76 65 20 33 2e 34 32 00 01 93");			
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 03 00 07 fb");  // CMD_SERIAL_GET_CAPABILITIES
		rule.addReaction("01 2b 01 07 04 02 01 15 00 02 00 03 fe 00 16 80 0c 00 00 00 e3 97 7d 80 07 00 00 80 00 00 00 00 00 00 00 00 00 00 02 00 00 80 07 00 2e");			
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 03 00 20 dc");  // CMD_MEMORY_GET_ID (HomeId)
		rule.addReaction("01 08 01 20 d2 7f ff 78 01 fd");			
		connection.addStreamRule(rule);		
		
		rule = new JWaveStreamReplyRule("01 03 00 02 fe");  // CMD_SERIAL_GET_INIT_DATA
		rule.addReaction("01 25 01 02 05 00 1d 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 01 c2");			
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 04 00 41 01 bb");  // CMD_JWave_GET_NODE_PROTOCOL_INFO
		rule.addReaction("01 09 01 41 93 16 00 02 02 01 32");			
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 04 01 42 02 ##");   // CMD_JWave_SET_DEFAULT
		rule.addReaction("01 04 00 42 02 #LRC");
		connection.addStreamRule(rule);
		
		// ---------- ControllerModes ----------- 
		
		rule = new JWaveStreamReplyRule("01 05 00 4b 01 %funcId_remove_start ##");  // CMD_JWave_REMOVE_NODE_FROM_NETWORK -  start
		rule.addReaction("01 07 00 4b %funcId_remove_start 01 00 00 #LRC");			
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 05 00 4b 05 %funcId_remove_cancel ##");  // CMD_JWave_REMOVE_NODE_FROM_NETWORK  - cancel
		rule.addReaction("01 07 00 4b %funcId_remove_cancel 06 00 00 #LRC");			
		connection.addStreamRule(rule);
			
		rule = new JWaveStreamReplyRule("01 05 00 4a 81 %funcId_add_start ##");  // CMD_JWave_ADD_NODE_TO_NETWORK  - start
		rule.addReaction("01 07 00 4a %funcId_add_start 01 00 00 #LRC");			
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 05 00 4a 05 %funcId_add_cancel ##");  // CMD_JWave_ADD_NODE_TO_NETWORK  - cancel
		rule.addReaction("01 07 00 4a %funcId_add_cancel 06 00 00 #LRC");			
		connection.addStreamRule(rule);
		
	}
	
	protected boolean waitForControllerMode(JWaveControllerMode mode, int timeout) throws InterruptedException{
		long startMillies = new Date().getTime();
		long currentMillies = new Date().getTime();
		while ((currentMillies - startMillies) < timeout){
			Thread.sleep(10);
		//	synchronized(cntrl){
				if (cntrl.getControllerMode() == mode){
					return true;
				}
			}
			currentMillies = new Date().getTime();
		//}
		return false;
	}
	
	protected boolean waitForJWaveEventType(JWaveEventType type, int timeout) throws InterruptedException{
		long startMillies = new Date().getTime();
		long currentMillies = new Date().getTime();
		while ((currentMillies - startMillies) < timeout){			
			Thread.sleep(10);			
			synchronized(eventList){			
				for (JWaveEvent event : eventList){
					
					if (event.getEventType() == type){
						return true;
					}
				}
			}
			currentMillies = new Date().getTime();
		}
		return false;
	}
	
	protected String getByteStr(int value){
		String str = ""+value;
		if (str.length()<2){
			str = "0"+str;
		}
		return str;
	}
	
	abstract protected void prepare();
	abstract protected void evalJWaveEvent(JWaveEvent event);
	
}
