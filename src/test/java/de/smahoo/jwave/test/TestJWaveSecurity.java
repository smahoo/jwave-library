package de.smahoo.jwave.test;

import java.util.Collection;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.cmd.JWaveCommandClassSpecification;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.cmd.JWaveNodeCommandFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


import de.smahoo.jwave.node.JWaveNode;
import de.smahoo.jwave.node.JWaveNodeFactory;
import de.smahoo.jwave.security.JWaveSecurityCommunicationProcessor;
import de.smahoo.jwave.security.JWaveSecurityCommunicationProcessorListener;
import de.smahoo.jwave.security.JWaveSecurityDefaultCryptoEngine;
import de.smahoo.jwave.security.JWaveSecurityMessageEncapsulator;
import de.smahoo.jwave.security.JWaveSecurityNetworkKey;
import de.smahoo.jwave.security.JWaveSecurityNonce;
import de.smahoo.jwave.utils.ByteArrayGeneration;

public class TestJWaveSecurity {

	JWaveNodeCommandFactory factory;
	JWaveNodeFactory  nodeFactory;
	
	
	@Before
	public void init(){		
		
		JWaveController.doLogging(true);
		
		String sep = System.getProperty("file.separator");
		String filename = System.getProperty("user.dir")+sep+"cnf"+sep+"cmd_classes.xml";
		JWaveCommandClassSpecification spec = null;
		try {
			spec = new JWaveCommandClassSpecification(filename);
		} catch (Exception exc){
			exc.printStackTrace();
			return;
		}
		factory = new JWaveNodeCommandFactory(spec);
		nodeFactory = new JWaveNodeFactory(spec);
		
	}
	
	@Ignore
	@Test
	public void testNodeCmd_SecurityMessageEncasulator_oneFrame() {

		
		byte[] tmpKeyBytes = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		
		JWaveSecurityNetworkKey networkKey = new JWaveSecurityNetworkKey(tmpKeyBytes,null,null);
		
		JWaveNodeCommand setKeyCmd = factory.generateCmd_SecurityKeySet(networkKey.getKey());
	
		
		JWaveSecurityMessageEncapsulator encapsulator = new JWaveSecurityMessageEncapsulator(setKeyCmd,networkKey);
		
		assert !encapsulator.isSecondFrameNeeded();
		
		byte[] payload = encapsulator.getFirstPayloadPart();
		
		assert payload.length == tmpKeyBytes.length + 2;
		
		assert payload[0] == setKeyCmd.getCommandClassKey();
		assert payload[1] == setKeyCmd.getCommandKey();
		
	}
	

	@Test
	public void test_DefaultCryptoEngine_decrypt() {
	
		byte[] msg = ByteArrayGeneration.generateByteArray("aa b5 d0 d3 97 9b 10 34 da 61 46 1b d8 fb e4 02 bc 20 16");
		byte[] nkBytes = {0x00,0x11,0x22,0x33,0x44,0x55,0x66,0x77,(byte)0x88,(byte)0x99,(byte)0xaa,(byte)0xbb,(byte)0xcc,(byte)0xdd,(byte)0xee,(byte)0xff};
		//byte[] usedNonceBytes = ByteArrayGeneration.generateByteArray("c1 b5 2c 96 1a 4c dd 8e");
		//byte[] mac = ByteArrayGeneration.generateByteArray("1d 9a 05 bf c2 b7 80 8f");
		byte[] iv = ByteArrayGeneration.generateByteArray("54 d4 f3 2b 07 f1 e3 58 c1 b5 2c 96 1a 4c dd 8e");
		
		JWaveSecurityDefaultCryptoEngine cryptoEngine = new JWaveSecurityDefaultCryptoEngine();
		
		try{
//			byte[] result = cryptoEngine.decrypt_ofb(msg, new JWaveSecurityNetworkKey(nkBytes), new JWaveSecurityNonce(usedNonceBytes), mac, iv);
//			System.out.println(ByteArrayGeneration.toHexString(result));
			byte[] result2 = cryptoEngine.decrypt(msg, cryptoEngine.generateNetworkKey(nkBytes), iv);
			//System.out.println(ByteArrayGeneration.toHexString(result2));
		} catch (Exception exc){
			exc.printStackTrace();
		}
		//secProc.test();
	}
	
	
	@Test
	public void test_DefaultCryptoEngine_encrypt() {
	
		byte[] msg = ByteArrayGeneration.generateByteArray("00 86 13 86");
		byte[] nkBytes = {0x00,0x11,0x22,0x33,0x44,0x55,0x66,0x77,(byte)0x88,(byte)0x99,(byte)0xaa,(byte)0xbb,(byte)0xcc,(byte)0xdd,(byte)0xee,(byte)0xff};
		//byte[] usedNonceBytes = ByteArrayGeneration.generateByteArray("bc ef 56 bd 30 86 7b a9");
		//byte[] mac = ByteArrayGeneration.generateByteArray("1d 9a 05 bf c2 b7 80 8f");
		byte[] iv = ByteArrayGeneration.generateByteArray("bc ef 56 bd 30 86 7b a9 7f da b8 30 52 32 53 ea");
		
		JWaveSecurityDefaultCryptoEngine cryptoEngine = new JWaveSecurityDefaultCryptoEngine();
		
		try{
			byte[] result2 = cryptoEngine.encrypt(msg, cryptoEngine.generateNetworkKey(nkBytes).getEncryptionKey(),iv);
			//System.out.println(ByteArrayGeneration.toHexString(result2));
		} catch (Exception exc){
			exc.printStackTrace();
		}
		//secProc.test();
	}
	
	@Ignore
	@Test 
	public void test_SecurityInlusionTimout(){
		
	}
}
