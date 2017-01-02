package de.smahoo.jwave.test;

import static org.junit.Assert.*;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.smahoo.jwave.cmd.JWaveConvertException;

@RunWith(JUnit4.class)
public class TestJWaveCommandParamType {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	

	
	@Test
	public void test_toByteArray() throws JWaveConvertException{
		byte[] array = JWaveCommandParameterType.toByteArray(JWaveCommandParameterType.BYTE, "10");
		assertEquals(array.length,1);
		assertEquals(array[0],0x10);
		
		array = JWaveCommandParameterType.toByteArray(JWaveCommandParameterType.WORD, "10 00");
		assertEquals(array.length,2);
		assertEquals(array[0],0x10);
		assertEquals(array[1],0x00);
		
		array = JWaveCommandParameterType.toByteArray(JWaveCommandParameterType.BIT_24, "00 10 00");
		assertEquals(array.length,3);
		assertEquals(array[0],0x00);
		assertEquals(array[1],0x10);
		assertEquals(array[2],0x00);
		
		array = JWaveCommandParameterType.toByteArray(JWaveCommandParameterType.BIT_32, "00 00 10 00");
		assertEquals(array.length,4);
		assertEquals(array[0],0x00);
		assertEquals(array[1],0x00);
		assertEquals(array[2],0x10);
		assertEquals(array[3],0x00);
		
		array = JWaveCommandParameterType.toByteArray(JWaveCommandParameterType.BYTE,(String)null);
		assertEquals(array.length, 1);
		assertEquals(array[0],0);
		
		array = JWaveCommandParameterType.toByteArray(JWaveCommandParameterType.STRUCT_BYTE,"0x10");
		assertEquals(array.length, 1);
		assertEquals(array[0],0x10);
	}
	
	@Test
	public void test_getSize(){
		assertEquals(JWaveCommandParameterType.getSize(JWaveCommandParameterType.BYTE),1);
		assertEquals(JWaveCommandParameterType.getSize(JWaveCommandParameterType.WORD),2);
		assertEquals(JWaveCommandParameterType.getSize(JWaveCommandParameterType.BIT_24),3);
		assertEquals(JWaveCommandParameterType.getSize(JWaveCommandParameterType.BIT_32),4);
		assertEquals(JWaveCommandParameterType.getSize(JWaveCommandParameterType.VARIANT),-1);
	}

	@Test
	public void test_toInteger(){
		byte[] array = {0x10};
		int result = JWaveCommandParameterType.toInteger(array);
		assertEquals(result,0x10);
		
		array = new byte[] {0x10,0x42};
		result = JWaveCommandParameterType.toInteger(array);
		assertEquals(result,0x1042);
		
		array = new byte[] {0x10,0x42,0x00};
		result = JWaveCommandParameterType.toInteger(array);
		assertEquals(result,0x104200);
		
		array = new byte[] {0x10,0x42,0x00,0x00};
		result = JWaveCommandParameterType.toInteger(array);
		assertEquals(result,0x10420000);
	}
	
	
	@Test
	public void test_toByte_fail() throws JWaveConvertException{
		
		thrown.expect(JWaveConvertException.class);
		
			byte[] array = {0x10, 0x12};
		JWaveCommandParameterType.toByte(array);
	
	}
	
	@Test
	public void test_toByteArray_fail() throws JWaveConvertException{
		thrown.expect(JWaveConvertException.class);

		JWaveCommandParameterType.toByteArray(JWaveCommandParameterType.BYTE, "nan");
		
	}

}
