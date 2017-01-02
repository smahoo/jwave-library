package de.smahoo.jwave.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.smahoo.jwave.cmd.JWaveBasicDeviceType;
import de.smahoo.jwave.cmd.JWaveCommandClass;
import de.smahoo.jwave.cmd.JWaveCommandClassSpecification;
import de.smahoo.jwave.cmd.JWaveGenericDeviceType;
import de.smahoo.jwave.cmd.JWaveNodeCommandFactory;
import de.smahoo.jwave.cmd.JWaveSpecificDeviceType;
import de.smahoo.jwave.event.JWaveEventType;
import de.smahoo.jwave.event.JWaveNodeEvent;
import de.smahoo.jwave.node.JWaveNode;
import de.smahoo.jwave.node.JWaveNodeFactory;
import de.smahoo.jwave.node.JWaveNodeSleepMode;

public class TestJWaveNodeGeneration {

	
	JWaveNodeCommandFactory factory;
	JWaveNodeFactory  nodeFactory;
	JWaveCommandClassSpecification spec;
	
	@Before
	public void init() throws Exception{		
		String sep = System.getProperty("file.separator");
		String filename = System.getProperty("user.dir")+sep+"cnf"+sep+"cmd_classes.xml";
		
		spec = new JWaveCommandClassSpecification(filename);
		assertNotNull(spec);
		factory = new JWaveNodeCommandFactory(spec);
		nodeFactory = new JWaveNodeFactory(spec);
		assertNotNull(factory);
		assertNotNull(nodeFactory);
	}
	
	@Test
	public void testCreateNode_IdOnly() {
		JWaveNode node = nodeFactory.createNode(42);
		assertNotNull(node);
		assertEquals(node.getNodeId(), 42);
	}
	
	@Test
	public void testCreateNode_Complete() {
		List<JWaveCommandClass> cmdClasses = new ArrayList<JWaveCommandClass>();
		cmdClasses.add(spec.getCommandClass(0x70));
		cmdClasses.add(spec.getCommandClass(0x20));
		cmdClasses.add(spec.getCommandClass(0x80));
				
		JWaveNode node = nodeFactory.createNode(cmdClasses, 42, 1, 0x04, 0x08, 0x04);
		assertNotNull(node);
		assertEquals(node.getNodeId(), 42);
		
		
		JWaveGenericDeviceType genType = node.getGenericDeviceType();
		JWaveSpecificDeviceType specType = node.getSpecificDeviceType();
		JWaveBasicDeviceType basType = node.getBasicDeviceType();
		
		assertNotNull(genType);
		assertNotNull(specType);
		assertNotNull(basType);
		
		assertEquals(genType.getName(),"GENERIC_TYPE_THERMOSTAT");
		assertEquals(specType.getName(),"SPECIFIC_TYPE_SETPOINT_THERMOSTAT");
		assertEquals(basType.getName(),"BASIC_TYPE_ROUTING_SLAVE");
		
		assertEquals(false,node.hasManufactureDetails());
		assertEquals(false,node.supportsClassAssociation());
		assertEquals(true,node.supportsClassBattery());
		assertEquals(true,node.supportsClassConfiguration());
		assertEquals(false,node.supportsClassMultiCmd());
		assertEquals(false,node.supportsClassWakeUp());
		assertEquals(true,node.supportsCommandClass(0x70));
		assertEquals(false,node.supportsCommandClass(0x44));
		
		assertEquals(JWaveNodeSleepMode.SLEEP_MODE_NONE,node.getSleepMode());
		
		
	}
	
	@Test
	public void testNodeEvent(){
		JWaveNode node = nodeFactory.createNode(42);
		assertNotNull(node);
		assertEquals(node.getNodeId(), 42);
		JWaveNodeEvent nodeEvent = new JWaveNodeEvent(JWaveEventType.NODE_EVENT_NODE_ADDED,node);
		assertNotNull(nodeEvent);
		assertEquals(nodeEvent.getNode(),node);
	}

}
