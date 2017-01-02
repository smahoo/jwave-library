package de.smahoo.jwave.test;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import de.smahoo.jwave.JWaveControllerMode;
import de.smahoo.jwave.event.JWaveControlEvent;
import de.smahoo.jwave.event.JWaveEvent;
import de.smahoo.jwave.event.JWaveEventType;
import de.smahoo.jwave.simulation.JWaveStreamReplyRule;

public class TestJWaveControlEmptyEEPROM extends TestJWaveAbstractConnection{

	@Override
	protected void prepare(){
		//
	}
	
	@Override
	protected void evalJWaveEvent(JWaveEvent event) {
	   //
	}
	
	@Override
	protected void addStandardControllerReplyRules(){		
		JWaveStreamReplyRule rule = new JWaveStreamReplyRule("01 03 00 15 e9");  // CMD_JWave_GET_VERSION
		rule.addReaction("01 10 01 15 5a 2d 57 61 76 65 20 33 2e 36 37 00 02 97");			
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 03 00 07 fb");  // CMD_SERIAL_GET_CAPABILITIES
		rule.addReaction("01 2b 01 07 03 5e 00 00 00 01 00 01 de 80 3f 88 0f 00 00 00 fb 97 6c a0 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 cc");			
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 03 00 20 dc");  // CMD_MEMORY_GET_ID (HomeId)
		rule.addReaction("01 08 01 20 ff ff ff ff ff 29");			
		connection.addStreamRule(rule);		
		
		rule = new JWaveStreamReplyRule("01 03 00 02 fe");  // CMD_SERIAL_GET_INIT_DATA
		rule.addReaction("01 25 01 02 05 00 1d ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff ff 03 01 3c");			
		connection.addStreamRule(rule);
						
		rule = new JWaveStreamReplyRule("01 04 01 42 02 ##");   // CMD_JWave_SET_DEFAULT
		rule.addReaction("01 04 00 42 02 #LRC");
		connection.addStreamRule(rule);		
				
	}
	
	
	@Test
	public void test_initController() throws Exception{
		assertNotNull(cntrl);			
		cntrl.init(connection);		
		// FIXME: wait for error event
		// 
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_ERROR,1000));
	}

	
	

}
