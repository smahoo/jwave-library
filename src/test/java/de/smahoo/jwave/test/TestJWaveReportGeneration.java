package de.smahoo.jwave.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import de.smahoo.jwave.cmd.*;
import org.junit.Before;
import org.junit.Test;

import de.smahoo.jwave.cmd.report.JWaveReport;
import de.smahoo.jwave.cmd.report.JWaveReportAlarmSupported;
import de.smahoo.jwave.cmd.report.JWaveReportBasic;
import de.smahoo.jwave.cmd.report.JWaveReportBattery;
import de.smahoo.jwave.cmd.report.JWaveReportConfiguration;
import de.smahoo.jwave.cmd.report.JWaveReportDoorLockConfiguration;
import de.smahoo.jwave.cmd.report.JWaveReportDoorLockOperation;
import de.smahoo.jwave.cmd.report.JWaveReportFactory;
import de.smahoo.jwave.cmd.report.JWaveReportManufacturerSpecific;
import de.smahoo.jwave.cmd.report.JWaveReportMeter;
import de.smahoo.jwave.cmd.report.JWaveReportSecurityCommandsSupported;
import de.smahoo.jwave.cmd.report.JWaveReportSensorBinary;
import de.smahoo.jwave.cmd.report.JWaveReportSensorMultilevel;
import de.smahoo.jwave.cmd.report.JWaveReportSwitchBinary;
import de.smahoo.jwave.cmd.report.JWaveReportSwitchMultilevel;
import de.smahoo.jwave.cmd.report.JWaveReportThermostatSetpoint;
import de.smahoo.jwave.cmd.report.JWaveReportWakeUpInterval;
import de.smahoo.jwave.cmd.report.JWaveReportDoorLockConfiguration.OperationType;
import de.smahoo.jwave.cmd.report.JWaveReportDoorLockOperation.DoorLockMode;
import de.smahoo.jwave.io.JWaveDatagram;
import de.smahoo.jwave.io.JWaveDatagramFactory;
import de.smahoo.jwave.utils.ByteArrayGeneration;


/**
 * Nodes are communication with dataframes with the type CMD_APPLICATION_HANDLER. These frames will be transferred to JWaveReports. 
 * This class tests the numerous report generations from datagrams.
 * 
 * @author Mathias Runge
 *
 */
public class TestJWaveReportGeneration {

	JWaveNodeCommandFactory factory = null;
	private JWaveNodeCommand nodeCmd;


	@Before
	public void init(){		
		String sep = System.getProperty("file.separator");
		String filename = System.getProperty("user.dir")+sep+"cnf"+sep+"cmd_classes.xml";
		JWaveCommandClassSpecification spec = null;
		
		try {
			spec = new JWaveCommandClassSpecification(filename);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		factory = new JWaveNodeCommandFactory(spec);
	}
	
	@Test
	public void testNodeCmd_BatteryReport(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 09 00 04 00 08 03 80 03 64 1e"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x80);
		
		JWaveReportBattery report = null;
		try {
			report = JWaveReportFactory.generateBatteryReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertEquals(report.getBattery(),100);
	}
	
	//@Test
	public void testNodeCmd_BasicReport(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray(""));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x20);
		
		JWaveReportBasic report = null;
		try {
			report = JWaveReportFactory.generateBasicReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertEquals(report.getValue(),0xFF);
	}
	
	@Test
	public void testNodeCmd_BasicSet_on(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 09 00 04 00 02 03 20 01 ff 2d"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);		
		assertEquals(nodeCmd.getCommandClass().getKey(),0x20);
		
		assertEquals(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0)), 0xff);
	}
	
	@Test
	public void testNodeCmd_BasicSet_off(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 09 00 04 00 02 03 20 01 00 2d"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x20);		
		assertEquals(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0)), 0);
		
		
	}
		
	
	@Test
	public void testNodeCmd_ReportThermostatSetPoint_Celsius(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0c 00 04 00 03 06 43 03 01 42 08 34 cd"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x43);
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportThermostatSetpoint);
		JWaveReportThermostatSetpoint setpointRep = (JWaveReportThermostatSetpoint)report;
		assertEquals(setpointRep.getValue(),2100);
		assertEquals(setpointRep.getPrecision(),100);
		assertEquals(setpointRep.getSetpointType(),1);
		assertEquals(setpointRep.getTemperature(),21.0,0.0);
	}	
	
	// @Test  TODO
	public void testNodeCmd_ReportThermostatSetPoint_Fahrenheit(){
		
	}	
	
	

	
	@Test
	public void testNodeCmd_ReportSensorMultilevel_temperature(){
		assertNotNull(factory);		
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0c 00 04 00 04 06 31 05 01 22 01 05 e6"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x31);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportSensorMultilevel);
		JWaveReportSensorMultilevel multiRep = (JWaveReportSensorMultilevel)report;
		assertEquals(multiRep.getSensorType(),JWaveReportSensorMultilevel.TYPE_TEMPERATURE);  // is temperature report
		assertEquals(multiRep.getValue(),261);
	}

	@Test
	public void testNodeCmd_ReportSensorMultilevel_humidity(){
		assertNotNull(factory);		
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0b 00 04 00 04 05 31 05 05 01 35 f4"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x31);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportSensorMultilevel);
		JWaveReportSensorMultilevel multiRep = (JWaveReportSensorMultilevel)report;
		assertEquals(multiRep.getSensorType(),JWaveReportSensorMultilevel.TYPE_RELATIVE_HUMIDITY);  // is humidity report
		assertEquals(multiRep.getValue(),53);
	}

	@Test
	public void testNodeCmd_ReportSensorMultilevel_power(){
		assertNotNull(factory);		
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0c 00 04 00 02 06 31 05 04 22 01 73 93"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x31);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportSensorMultilevel);
		JWaveReportSensorMultilevel multiRep = (JWaveReportSensorMultilevel)report;
		assertEquals(multiRep.getSensorType(),JWaveReportSensorMultilevel.TYPE_POWER);  // is power report
		assertEquals(multiRep.getValue(),371);
	}

	@Test
	public void testNodeCmd_ReportWakeUp(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0c 00 04 00 06 06 84 06 00 00 f0 01 84"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x84);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportWakeUpInterval);
		assertEquals(((JWaveReportWakeUpInterval)report).getInterval(),240);
	}
	
	//@Test
	public void testNodeCmd_ReportSensorBinary_on(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray(""));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x20);
		
		JWaveReportSensorBinary report = null;
		try {
			report = JWaveReportFactory.generateSensorBinaryReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report.getValue());
	}
	
	
	@Test
	public void testNodeCmd_ReportSecurityCommandsSupported(){
		assertNotNull(factory);
		JWaveCommandClass cmdClass = factory.getCmdClassSpecification().getCommandClass(0x98);
		JWaveCommand cmd = cmdClass.getCommand(0x03);
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValues(ByteArrayGeneration.generateByteArray("00 72 86 98 62 4c 4e 63 8b 85 71 70 75 80 8a ef"));			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x98);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateSecurityReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assert report instanceof JWaveReportSecurityCommandsSupported;
		JWaveReportSecurityCommandsSupported secSuppRep = (JWaveReportSecurityCommandsSupported)report;
		
		assertEquals(secSuppRep.getReportsToFollow(),0);
		
		
		
//		System.out.println("  supported "+ByteArrayGeneration.toHexString(secSuppRep.getSecuritySupportedCommandClassKeys()));
//		Collection<JWaveCommandClass> cmdClasses = secSuppRep.getSupportedCommandClasses(factory.getCmdClassSpecification());
//		for (JWaveCommandClass cc : cmdClasses ){
//			System.out.println(" "+cc.getName());
//		}
		
		
		
	}
	
	//@Test
	public void testNodeCmd_ReportSensorBinary_off(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray(""));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x20);
		
		JWaveReportSensorBinary report = null;
		try {
			report = JWaveReportFactory.generateSensorBinaryReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertFalse(report.getValue());
	}
	
	
	@Test
	public void testNodeCmd_ReportManufacturerSpecific(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0e 00 04 00 05 08 72 05 01 0f 06 00 10 00 97"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x72);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportManufacturerSpecific);
		JWaveReportManufacturerSpecific manuRep = (JWaveReportManufacturerSpecific)report;
		assertEquals(manuRep.getManufacturerId(),271);
		assertEquals(manuRep.getProductTypeId(), 1536);
		assertEquals(manuRep.getProductId(),4096);
	}
	
	
	@Test
	public void testNodeCmd_ReportAlarm_alarm(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0d 00 04 00 09 07 9c 02 09 00 ff 00 00 90"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x9c);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		
	}
	
	@Test
	public void testNodeCmd_ReportAlarm_noAlarm(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0d 00 04 00 09 07 9c 02 09 00 00 00 00 90"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x9c);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		
	}
	
	@Test
	public void testNodeCmd_ReportAlarmSupported_01(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0a 00 04 00 07 04 9c 04 01 01 6a"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x9c);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportAlarmSupported);
		JWaveReportAlarmSupported supRep = (JWaveReportAlarmSupported)report;
		Collection<Integer> supportedTypes = supRep.getSupportedTypes();
		assertTrue(supportedTypes.size() == 1);
		assertTrue(supportedTypes.contains(1));		
	}
	
	@Test
	public void testNodeCmd_ReportAlarmSupported_01_03_04(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0a 00 04 00 07 04 9c 04 04 0D 6a"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x9c);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportAlarmSupported);
		JWaveReportAlarmSupported supRep = (JWaveReportAlarmSupported)report;
		Collection<Integer> supportedTypes = supRep.getSupportedTypes();
		assertTrue(supportedTypes.size() == 3);
		assertTrue(supportedTypes.contains(1));	
		assertTrue(supportedTypes.contains(3));
		assertTrue(supportedTypes.contains(4));	
	}
	
	@Test
	public void testNodeCmd_ReportSwitchBinaray_on(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 09 00 04 00 02 03 25 03 ff 2a"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x25);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportSwitchBinary);
		JWaveReportSwitchBinary rep = (JWaveReportSwitchBinary)report;
		assertTrue(rep.getValue());
	}
	
	@Test
	public void testNodeCmd_ReportSwitchBinaray_off(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 09 00 04 00 02 03 25 03 00 d5"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x25);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}	
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportSwitchBinary);
		JWaveReportSwitchBinary rep = (JWaveReportSwitchBinary)report;
		assertFalse(rep.getValue());
	}
	
	@Test
	public void testNodeCmd_ReportSwitchMultilevel_50(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 09 00 04 00 03 03 26 03 32 e5"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x26);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportSwitchMultilevel);
		JWaveReportSwitchMultilevel rep = (JWaveReportSwitchMultilevel)report;
		assertTrue(rep.isOn());
		assertEquals(rep.getLevel(),50);
	}
	
	@Test
	public void testNodeCmd_ReportSwitchMultilevel_100(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 09 00 04 00 03 03 26 03 64 b4"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
			fail(exc.getMessage());
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x26);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
			fail(exc.getMessage());
		}
		
		assertNotNull(report);
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportSwitchMultilevel);
		JWaveReportSwitchMultilevel rep = (JWaveReportSwitchMultilevel)report;
		assertTrue(rep.isOn());
		assertEquals(rep.getLevel(),100);
		
	}
	
	@Test
	public void testNodeCmd_ReportSwitchMultilevel_off(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 09 00 04 00 03 03 26 03 00 d7"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x26);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportSwitchMultilevel);
		JWaveReportSwitchMultilevel rep = (JWaveReportSwitchMultilevel)report;
		assertFalse(rep.isOn());
		assertEquals(rep.getLevel(),0);
	}
	
	@Test
	public void testNodeCmd_ReportConfiguration(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0b 00 04 00 0b 05 70 06 3e 01 08 bf"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x70);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportConfiguration);
		JWaveReportConfiguration rep = (JWaveReportConfiguration)report;
		assertEquals(rep.getParamId(),62);
		assertEquals(rep.getValue(),8);
	}
	
	@Test
	public void testNodeCmd_ReportConfiguration_4Bytes(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0e 00 04 00 02 08 70 06 01 04 00 00 27 10 bb"));		
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x70);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportConfiguration);
		JWaveReportConfiguration rep = (JWaveReportConfiguration)report;
		assertEquals(rep.getParamId(),1);
		assertEquals(10000,rep.getValue());
	}
	
	@Test
	public void testNodeCmd_ReportMeter_V2(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 14 00 04 00 07 0e 32 02 21 74 00 00 32 e6 00 00 00 00 00 00 57"));
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x32);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportMeter);
		JWaveReportMeter rep = (JWaveReportMeter)report;		
		assertEquals(rep.getValue(),13030);
		assertEquals(rep.getScale(),JWaveReportMeter.SCALE_W);		
		assertEquals(rep.getPrecission(), 1000);
		assertEquals(rep.getMeterType(), JWaveReportMeter.METER_TYPE_ELECTRIC);
		assertEquals(rep.getRateType(),JWaveReportMeter.RATE_TYPE_IMPORT);
	
	}
	
	
	@Test
	public void testNodeCmd_ReportMeter_V2_NorthQ(){
		assertNotNull(factory);
	
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 14 00 04 00 03 0e 32 02 21 64 00 00 05 c0 00 00 00 00 00 00 52"));
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);			
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		assertEquals(nodeCmd.getCommandClass().getKey(),0x32);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(report);
		assertTrue(report instanceof JWaveReportMeter);
		JWaveReportMeter rep = (JWaveReportMeter)report;
		assertEquals(rep.getValue(),1472);
		assertEquals(rep.getScale(),JWaveReportMeter.SCALE_KWH);
		assertEquals(rep.getPrecission(), 1000);
		assertEquals(rep.getMeterType(), JWaveReportMeter.METER_TYPE_ELECTRIC);
		assertEquals(rep.getRateType(),JWaveReportMeter.RATE_TYPE_IMPORT);
	
	}
	
	@Test
	public void testNodeCmd_ReportDoorLockOperation(){
		assertNotNull(factory);
		

		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(ByteArrayGeneration.generateByteArray("62 03 ff 00 00 fe c2"));
		} catch (Exception exc){
			exc.printStackTrace();
		}
	
		assertNotNull(nodeCmd);
		
				
		assertEquals(nodeCmd.getCommandClass().getKey(),0x62);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
	
		assertNotNull(report);
		
		assert report instanceof JWaveReportDoorLockOperation;
		
		JWaveReportDoorLockOperation lockRep = (JWaveReportDoorLockOperation)report;
		assertEquals(lockRep.getDoorLockMode(),DoorLockMode.DLM_SECURED);
	
		//assert report instanceof JWaveReport
		
	}
	
	@Test
	public void testNodeCmd_ReportDoorLockConfiguration(){
		assertNotNull(factory);
		

		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(ByteArrayGeneration.generateByteArray("62 06 02 00 fe c0"));
		} catch (Exception exc){
			exc.printStackTrace();
		}
	
		assertNotNull(nodeCmd);
		
				
		assertEquals(nodeCmd.getCommandClass().getKey(),0x62);
		
		JWaveReport report = null;
		try {
			report = JWaveReportFactory.generateReport(nodeCmd);
		} catch (Exception exc){
			exc.printStackTrace();
		}
	
		assertNotNull(report);
		
		assertTrue(report instanceof JWaveReportDoorLockConfiguration);
		JWaveReportDoorLockConfiguration confRep = (JWaveReportDoorLockConfiguration)report;
		
		assertEquals(confRep.getOperationType(), OperationType.OT_TIMED);
	
		
	}
	
}