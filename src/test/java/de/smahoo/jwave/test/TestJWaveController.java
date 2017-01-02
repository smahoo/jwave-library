package de.smahoo.jwave.test;

import static org.junit.Assert.*;

import java.util.Date;

import de.smahoo.jwave.event.JWaveControlEvent;
import de.smahoo.jwave.event.JWaveEvent;
import org.junit.Test;

import de.smahoo.jwave.JWaveControllerMode;
import de.smahoo.jwave.event.JWaveEventType;

public class TestJWaveController extends TestJWaveAbstractConnection{

	@Override
	protected void prepare(){
		//
	}
	
	@Override
	protected void evalJWaveEvent(JWaveEvent event) {
	   //
	}
	
	@Test
	public void test_ControllerIsInitialized() throws Exception{
		assertNotNull(cntrl);	
		assertFalse(cntrl.isInitialized());
		cntrl.init(connection);		
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,2000));
		assertTrue(cntrl.isInitialized());
	}
	
	
	@Test
	public void test_ControllerModes() throws Exception{
		assertNotNull(cntrl);		
		cntrl.init(connection);
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,2000));		
		cntrl.setExlusionMode();
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_EXCLUSION,1000));
		cntrl.setNormalMode();
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,1000));
		cntrl.setInclusionMode();
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_INCLUSION,1000));
		cntrl.setNormalMode();
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,1000));
		
		// check directly change from inclusion to exclusion mode
		cntrl.setInclusionMode();
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_INCLUSION,1000));
		cntrl.setExlusionMode();
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_EXCLUSION,1000));
		cntrl.setNormalMode();
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,1000));
		
		
		// check network wide inclusion
//		cntrl.setNetworkWideInclusionMode();
//		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NWI,1000));
//		cntrl.setNormalMode();
//		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,1000));
		
		// checking whether timeout is working
		cntrl.setControllerModeTimeout(1500);
		cntrl.setInclusionMode();		
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_INCLUSION,1000));
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,2000));
		cntrl.setExlusionMode();
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_EXCLUSION,1000));
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,2000));
		
		
	}

	@Test
	public void test_ControllerReset() throws Exception{ 
		assertNotNull(cntrl);			
		cntrl.init(connection);		
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,1000));
		connection.clean();
		addStandardControllerReplyRules();
		cntrl.resetController();
		assertTrue(waitForJWaveEventType(JWaveEventType.CNTRL_EVENT_CONTROLLER_RESET,1000));
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_INITIALIZING,1000));
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,2000));
	}

	@Test
	public void test_JWaveControllerEvent(){
		Date before = new Date();
		JWaveControlEvent cntrlEvent = new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_INIT_COMPLETED,cntrl);
		assertNotNull(cntrlEvent);
		assertEquals(cntrlEvent.getCntrl(),cntrl);
		assertEquals(cntrlEvent.getEventType(),JWaveEventType.CNTRL_EVENT_INIT_COMPLETED);
		Date timestamp = cntrlEvent.getTimestamp();
		assertNotNull(timestamp);
		long difference = timestamp.getTime() - before.getTime();
		// assert that there are 10 ms difference in maximum between timestamp of event and measured time before generation
		assertTrue(difference < 10); 
		assertTrue(difference >= 0);
	}
	
	
	

}
