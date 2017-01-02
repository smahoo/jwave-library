package de.smahoo.jwave.test.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import de.smahoo.jwave.utils.ByteArrayGeneration;

public class TestByteArrayGeneration {

	@Test
	public void test_toHexString() {
		String result = "01 42 cf";
		byte[] bytes = {0x01,0x42,(byte)0xCF};
		int[]  ints  = {0x01,0x42,0xCF};
		
		String str = ByteArrayGeneration.toHexString(bytes);		
		assertEquals(str,result);
		
		str = ByteArrayGeneration.toHexString(ints);
		assertEquals(str, result);
		
		str = ByteArrayGeneration.toHexString((byte[])null);
		assertEquals(str,"");
		
		str = ByteArrayGeneration.toHexString((int[])null);
		assertEquals(str,"");
		
		str = ByteArrayGeneration.toHexString((byte[])null,0,10);
		assertEquals(str,"");
		
		str = ByteArrayGeneration.toHexString((int[])null,0,10);
		assertEquals(str,"");
		
		ByteArrayGeneration gen = new ByteArrayGeneration();
		assertNotNull(gen);
	}

}
