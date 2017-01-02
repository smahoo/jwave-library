package de.smahoo.jwave.test;

import static org.junit.Assert.*;

import org.junit.Test;

import de.smahoo.jwave.cmd.JWaveCommandType;

public class TestJWaveCommandType {

	@Test
	public void test() {
		JWaveCommandType commandType;
		for (int i= 0; i<255; i++){
			commandType = JWaveCommandType.getCommandType(i);
			if (commandType != JWaveCommandType.CMD_UNKNOWN){
				assertEquals(i,(int)(JWaveCommandType.getByte(commandType)&0xFF));
				
			}
		}
		assertEquals(-1,JWaveCommandType.getByte(JWaveCommandType.CMD_UNKNOWN));
		assertTrue(JWaveCommandType.getCommandType(-1) == JWaveCommandType.CMD_UNKNOWN);
		assertTrue(JWaveCommandType.getCommandType(0xFF) == JWaveCommandType.CMD_UNKNOWN);
	}

}
