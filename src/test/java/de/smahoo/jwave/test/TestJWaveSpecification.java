package de.smahoo.jwave.test;

import static org.junit.Assert.*;

import java.io.IOException;


import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.cmd.*;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Document;

import de.smahoo.jwave.utils.logger.LogTag;
import de.smahoo.jwave.utils.xml.XmlConvertionException;
import de.smahoo.jwave.utils.xml.XmlUtils;

@RunWith(JUnit4.class)
public class TestJWaveSpecification {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	

	protected Document generateXmlDocument() throws Exception{
		String content = generateXmlContent_correct();		
		Document doc = null;
					doc = XmlUtils.parseDoc(content);
		
		return doc;
	}
	
	// ++++++++++++++++++++++++ positive Tets +++++++++++++++++++++++++
	
	@Test
	public void testSpecification_cmdClass_positive() throws Exception{
		JWaveCommandClassSpecification specification = null;
		
		specification = new JWaveCommandClassSpecification(generateXmlDocument());
		
		assertNotNull(specification);
		assertEquals(specification.getCommandClasses().size(),1);
		
		JWaveCommandClass cmdClass = specification.getCommandClass("COMMAND_CLASS_ASSOCIATION",1);
		assertNull(cmdClass);
		cmdClass = specification.getCommandClass("COMMAND_CLASS_ASSOCIATION");
		assertNull(cmdClass);
		cmdClass = specification.getCommandClass("COMMAND_CLASS_ASSOCIATION",2);
		assertNotNull(cmdClass);
		assertEquals(cmdClass.getCommandList().size(),8);
		
		JWaveCommand cmd = checkCommand(cmdClass,specification,"COMMAND_CLASS_ASSOCIATION", 2, "ASSOCIATION_SET", 0x01, "Association Set");		
		
		assertEquals(cmd.getParamList().size(),2);
		checkCommandParam(cmd,0,"Grouping identifier","BYTE",0x01);
		checkCommandParam(cmd,1,"Node ID","VARIANT",0x0C);
		
		cmd = checkCommand(cmdClass,specification,"COMMAND_CLASS_ASSOCIATION", 2, "ASSOCIATION_GET", 0x02, "Association Get");
		assertEquals(cmd.getParamList().size(),1);
		checkCommandParam(cmd,0,"Grouping Identifier","BYTE",0x01);
		
		cmd = checkCommand(cmdClass,specification,"COMMAND_CLASS_ASSOCIATION", 2, "ASSOCIATION_REPORT", 0x03, "Association Report");
		assertEquals(cmd.getParamList().size(),4);	
		checkCommandParam(cmd,0,"Grouping Identifier","BYTE",0x01);
		checkCommandParam(cmd,1,"Max Nodes Supported","BYTE",0x01);
		checkCommandParam(cmd,2,"Reports to Follow","BYTE",0x01);
		checkCommandParam(cmd,3,"NodeID","VARIANT",0x0C);
		
		cmd = checkCommand(cmdClass,specification,"COMMAND_CLASS_ASSOCIATION", 2, "ASSOCIATION_REMOVE", 0x04, "Association Remove");	
		assertEquals(cmd.getParamList().size(),2);	
		checkCommandParam(cmd,0,"Grouping identifier","BYTE",0x01);
		checkCommandParam(cmd,1,"Node ID","VARIANT",0x0C);
				
		cmd = checkCommand(cmdClass,specification,"COMMAND_CLASS_ASSOCIATION", 2, "ASSOCIATION_GROUPINGS_GET", 0x05, "Association Groupings Get");
		assertEquals(cmd.getParamList().size(),0);
		
		
		cmd = checkCommand(cmdClass,specification,"COMMAND_CLASS_ASSOCIATION", 2, "ASSOCIATION_GROUPINGS_REPORT", 0x06, "Association Groupings Report");
		assertEquals(cmd.getParamList().size(),1);	
		checkCommandParam(cmd,0,"Supported Groupings","BYTE",0x01);
		
		cmd = checkCommand(cmdClass,specification,"COMMAND_CLASS_ASSOCIATION", 2, "ASSOCIATION_SPECIFIC_GROUP_GET", 0x0B, "Association Specific Group Get");
		assertEquals(cmd.getParamList().size(),0);
		
		cmd = checkCommand(cmdClass,specification,"COMMAND_CLASS_ASSOCIATION", 2, "ASSOCIATION_SPECIFIC_GROUP_REPORT", 0x0C, "Association Specific Group Report");
		assertEquals(cmd.getParamList().size(),1);
		checkCommandParam(cmd,0,"Group","BYTE",0x01);
		
		JWaveCommandClass cmdClass2 = specification.getCommandClass(0x85);
		assertNotNull(cmdClass2);
		assertEquals(cmdClass,cmdClass2);
		JWaveCommand cmd2 = specification.getCommand(0x85, 2,0x02);
		assertNotNull(cmd2);
		assertEquals(cmd2.getName(),"ASSOCIATION_GET");
		
		JWaveCommandClass cmdClassFail = specification.getCommandClass(0x42);
		assertNull(cmdClassFail);
		
		cmdClassFail = specification.getCommandClass(0x85,42);
		assertNull(cmdClassFail);
		
		cmdClassFail = specification.getCommandClass("Not Existing");
		assertNull(cmdClassFail);
		
		cmdClassFail = specification.getCommandClass("COMMAND_CLASS_ASSOCIATION",42);
		assertNull(cmdClassFail);
				
		JWaveCommand cmdFail = specification.getCommand(0x85, 1,0x01);
		assertNull(cmdFail);
		
		cmdFail = specification.getCommand(0x85, 2,0xFF);
		assertNull(cmdFail);
		
		cmdFail = specification.getCommand("COMMAND_CLASS_ASSOCIATION", 2,"Not Existing");
		assertNull(cmdFail);
		
		cmdFail = specification.getCommand("COMMAND_CLASS_ASSOCIATION",42 ,"ASSOCIATION_GET");
		assertNull(cmdFail);
	}
	
	@Test
	public void testSpecification_genDevType_positive() throws Exception{
		JWaveCommandClassSpecification specification = null;
		specification = new JWaveCommandClassSpecification(generateXmlDocument());		
		assertNotNull(specification);
		assertEquals(specification.getGenericDeviceTypes().size(),1);
		assertEquals(specification.getBasicDeviceTypes().size(),4);
		
		JWaveBasicDeviceType basDevType = null;
		basDevType = specification.getBasicDeviceType(0xFF);
		assertNull(basDevType);
	
		checkBasDevType(specification, 0x01, "BASIC_TYPE_CONTROLLER", false, "Controller", "Node is a portable controller ");
		checkBasDevType(specification, 0x02, "BASIC_TYPE_STATIC_CONTROLLER", false, "Static Controller", "Node is a static controller");
		checkBasDevType(specification, 0x03, "BASIC_TYPE_SLAVE", false, "Slave", "Node is a slave");
		checkBasDevType(specification, 0x04, "BASIC_TYPE_ROUTING_SLAVE", false, "Routing Slave", "Node is a slave with routing capabilities");
			
		JWaveGenericDeviceType genDevType = specification.getGenericDeviceType(0x10);
		assertEquals(genDevType.getKey(),0x10);
		assertEquals(genDevType.getName(),"GENERIC_TYPE_SWITCH_BINARY");
		assertEquals(genDevType.getComment(),"Binary Switch");
		assertEquals(genDevType.getHelp(),"Switch Binary");
		assertEquals(genDevType.isReadOnly(),false);
		
		assertEquals(genDevType.getSpecificDeviceTypes().size(),7);
		
		checkSpecDevType(genDevType, 0x00,"SPECIFIC_TYPE_NOT_USED","Not Used","Specific Device Class not used");
		checkSpecDevType(genDevType, 0x01,"SPECIFIC_TYPE_POWER_SWITCH_BINARY","Power Switch Binary","On/Off Power Switch Device Type");
		checkSpecDevType(genDevType, 0x02,"SPECIFIC_DEVICE_BINARY_TUNABLE_COLOR_LIGHT","Binary Tunable Color Light","");
		checkSpecDevType(genDevType, 0x03,"SPECIFIC_TYPE_SCENE_SWITCH_BINARY","Scene Switch Binary","Binary Scene Switch");
		checkSpecDevType(genDevType, 0x04,"SPECIFIC_TYPE_POWER_STRIP","Power Strip","Power Strip Device Type");
		checkSpecDevType(genDevType, 0x05,"SPECIFIC_TYPE_SIREN","Siren","Siren Device Type");
		checkSpecDevType(genDevType, 0x06,"SPECIFIC_TYPE_VALVE_OPEN_CLOSE","Valve Open/Close","Valve (open/close) Device Type");

		
	}

	// ++++++++++++++++++ negative Tests +++++++++++++++++++++++++
	
	@Test
	public void testSpecification_init_DocumentIsNull() throws XmlConvertionException{	
		thrown.expect(NullPointerException.class);
		new JWaveCommandClassSpecification((Document)null);			
	}
	
	@Test
	public void testSpecification_init_specFileNotExists() throws IOException, XmlConvertionException{
		thrown.expect(IOException.class);
		JWaveController.log(LogTag.DEBUG, "!!!! The following IOException is part of the test and was expected to be thrown!");
		new JWaveCommandClassSpecification("file not exists");
	}
	

	@Test
	public void testSpecification_cmdClass_noName() throws Exception{
		checkForXmlConvertionException(generateXmlContent_cmd_noName());		
	}
	
	@Test
	public void testSpecificatin_init_basDev_NAN() throws Exception {
		thrown.expectCause(CoreMatchers.<Throwable>instanceOf(NumberFormatException.class));
		checkForXmlConvertionException(generateXmlContent_basDev_nan());		
	}
	
	@Test
	public void testSpecification_init_basDev_noKey() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_basDev_noKey());		
	}

	@Test
	public void testSpecification_init_basDev_noName() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_basDev_noName());		
	}
	
	@Test
	public void testSpecification_init_genDev_noKey() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_genDev_noKey());		
	}

	@Test
	public void testSpecification_init_genDev_noName() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_genDev_noName());		
	}
	
	@Test
	public void testSpecification_init_specDev_noName() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_specDev_noName());
	}
	
	@Test
	public void testSpecification_init_specDev_noKey() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_specDev_noKey());
	}
	
	@Test
	public void testSpecification_init_cmdClass_noName() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_cmdClass_noName());
	}
	
	@Test
	public void testSpecification_init_cmdClass_noKey() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_cmdClass_noKey());				
	}
	
	@Test
	public void testSpecification_init_cmdClass_noVersion() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_cmdClass_noVersion());				
	}
	
	
	@Test
	public void testSpecification_init_cmd_noKey() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_cmd_noKey());
	}
	
	@Test
	public void testSpecification_init_cmd_noName() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_cmd_noKey());				
	}
	
	@Test
	public void testSpecification_init_cmdParam_noKey() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_cmdParam_noKey());				
	}
	
	@Test
	public void testSpecification_init_cmdParam_noName() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_cmdParam_noName());
	}
	
	@Test
	public void testSpecification_init_cmdParam_noType() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_cmdParam_noType());				
	}
	
	@Test
	public void testSpecification_init_cmdParam_noTypeHashcode() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_cmdParam_noTypeHashcode());		
	}
	
	@Test
	public void testSpecification_init_cmdParamValueAttr_noKey() throws Exception{	
		checkForXmlConvertionException(generateXmlContent_cmdParamValueAttr_noKey());
		
	}
	
	// ++++++++++++++++++++++ HELPERS ++++++++++++++++++++++++++++
	
	protected void checkForXmlConvertionException(String xmlContent) throws Exception{
		Document doc = XmlUtils.parseDoc(xmlContent);
		thrown.expect(XmlConvertionException.class);
		new JWaveCommandClassSpecification(doc);		
	}
	
	protected void checkCommandParam(JWaveCommand cmd, int position, String name, String type, int typehashcode){
		JWaveCommandParameter param = cmd.getParam(position);
		
		//assertTrue(param.hasValues());
		assertEquals(param.getKey(),position);
		assertEquals(param.getName(),name);
		assertEquals(param.getType(),JWaveCommandParameterType.valueOf(type));
		assertEquals(param.getTypeHashCode(),typehashcode);
	}
	
	protected JWaveCommand checkCommand(JWaveCommandClass cmdClass, JWaveCommandClassSpecification specification, String cmdClassStr,int version, String cmdStr,int key, String helpStr){
		JWaveCommand cmd = specification.getCommand(cmdClassStr,version,cmdStr);
		assertNotNull(cmd);
		assertEquals(cmd.getHelp(),helpStr);
		assertEquals(cmd.getKey(),key);
		assertEquals(cmd.getCommandClass(),cmdClass);
		return cmd;
	}
	
	protected void checkSpecDevType(JWaveGenericDeviceType genDevType, int key, String name, String help, String comment){
		JWaveSpecificDeviceType specDevType = genDevType.getSpecificDeviceType(key);		
		assertNotNull(specDevType);
		assertEquals(specDevType.getName(),name);
		assertEquals(specDevType.getComment(),comment);
		assertEquals(specDevType.getHelp(),help);
		assertEquals(specDevType.toString(),name);
	}
	
	protected void checkBasDevType(JWaveCommandClassSpecification specification, int key, String name, boolean readonly, String help, String comment){
		JWaveBasicDeviceType basDevType = specification.getBasicDeviceType(key);		
		assertNotNull(basDevType);
		assertEquals(basDevType.getKey(),key);
		assertEquals(basDevType.getName(),name);
		assertEquals(basDevType.getComment(), comment);
		assertEquals(basDevType.getHelp(),help);
		assertEquals(basDevType.isReadOnly(),readonly);
		assertEquals(basDevType.toString(),name);
	}

	// ++++++++++++++++++++++ XML contents ++++++++++++++++++++++++
	
	protected  String generateXmlContent_correct(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_CONTROLLER\" key=\"0x01\" help=\"Controller\" comment=\"Node is a portable controller \" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_ROUTING_SLAVE\" key=\"0x04\" help=\"Routing Slave\" comment=\"Node is a slave with routing capabilities\" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_SLAVE\" key=\"0x03\" help=\"Slave\" comment=\"Node is a slave\" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_STATIC_CONTROLLER\" key=\"0x02\" help=\"Static Controller\" comment=\"Node is a static controller\" />" + "\r\n"+
						 "  <gen_dev key=\"0x10\" name=\"GENERIC_TYPE_SWITCH_BINARY\" help=\"Switch Binary\" read_only=\"False\" comment=\"Binary Switch\">" + "\r\n"+
						 "     <spec_dev key=\"0x00\" name=\"SPECIFIC_TYPE_NOT_USED\" help=\"Not Used\" comment=\"Specific Device Class not used\" />" + "\r\n"+
						 "     <spec_dev key=\"0x01\" name=\"SPECIFIC_TYPE_POWER_SWITCH_BINARY\" help=\"Power Switch Binary\" comment=\"On/Off Power Switch Device Type\" />" + "\r\n"+
						 "     <spec_dev key=\"0x03\" name=\"SPECIFIC_TYPE_SCENE_SWITCH_BINARY\" help=\"Scene Switch Binary\" comment=\"Binary Scene Switch\" />" + "\r\n"+
						 "     <spec_dev key=\"0x04\" name=\"SPECIFIC_TYPE_POWER_STRIP\" help=\"Power Strip\" comment=\"Power Strip Device Type\" />" + "\r\n"+
						 "     <spec_dev key=\"0x05\" name=\"SPECIFIC_TYPE_SIREN\" help=\"Siren\" comment=\"Siren Device Type\" />" + "\r\n"+
						 "     <spec_dev key=\"0x06\" name=\"SPECIFIC_TYPE_VALVE_OPEN_CLOSE\" help=\"Valve Open/Close\" comment=\"Valve (open/close) Device Type\" />" + "\r\n"+
						 "     <spec_dev key=\"0x02\" name=\"SPECIFIC_DEVICE_BINARY_TUNABLE_COLOR_LIGHT\" help=\"Binary Tunable Color Light\" comment=\"\" />" + "\r\n"+
						 "  </gen_dev>" + "\r\n"+
						 "  <cmd_class key=\"0x85\" version=\"2\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "    <cmd key=\"0x02\" name=\"ASSOCIATION_GET\" help=\"Association Get\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" name=\"Grouping Identifier\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+
						 "    <cmd key=\"0x05\" name=\"ASSOCIATION_GROUPINGS_GET\" help=\"Association Groupings Get\" comment=\"\" />" + "\r\n"+
						 "    <cmd key=\"0x06\" name=\"ASSOCIATION_GROUPINGS_REPORT\" help=\"Association Groupings Report\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" name=\"Supported Groupings\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "        <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+
						 "    <cmd key=\"0x04\" name=\"ASSOCIATION_REMOVE\" help=\"Association Remove\" comment=\"\">" + "\r\n"+
						 "  	<param key=\"0x00\" name=\"Grouping identifier\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 " 		<param key=\"0x01\" name=\"Node ID\" type=\"VARIANT\" typehashcode=\"0x0C\" comment=\"\">" + "\r\n"+
						 "         <variant paramoffs=\"255\" showhex=\"False\" signed=\"True\" sizemask=\"0x00\" sizeoffs=\"0\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+
						 "    <cmd key=\"0x03\" name=\"ASSOCIATION_REPORT\" help=\"Association Report\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" name=\"Grouping Identifier\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "      <param key=\"0x01\" name=\"Max Nodes Supported\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "      <param key=\"0x02\" name=\"Reports to Follow\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "      <param key=\"0x03\" name=\"NodeID\" type=\"VARIANT\" typehashcode=\"0x0C\" comment=\"\">" + "\r\n"+
						 "         <variant paramoffs=\"255\" showhex=\"False\" signed=\"True\" sizemask=\"0x00\" sizeoffs=\"0\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+
						 "    <cmd key=\"0x01\" name=\"ASSOCIATION_SET\" help=\"Association Set\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" name=\"Grouping identifier\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "      <param key=\"0x01\" name=\"Node ID\" type=\"VARIANT\" typehashcode=\"0x0C\" comment=\"\">" + "\r\n"+
						 "         <variant paramoffs=\"255\" showhex=\"False\" signed=\"True\" sizemask=\"0x00\" sizeoffs=\"0\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+
						 "    <cmd key=\"0x0B\" name=\"ASSOCIATION_SPECIFIC_GROUP_GET\" help=\"Association Specific Group Get\" comment=\"\" />" + "\r\n"+
						 "    <cmd key=\"0x0C\" name=\"ASSOCIATION_SPECIFIC_GROUP_REPORT\" help=\"Association Specific Group Report\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" name=\"Group\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_cmd_noKey(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <cmd_class key=\"0x85\" version=\"2\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "    <cmd name=\"ASSOCIATION_GET\" help=\"Association Get\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" name=\"Grouping Identifier\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+					
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_cmd_noName(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <cmd_class key=\"0x85\" version=\"2\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "    <cmd key=\"0x02\" help=\"Association Get\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" name=\"Grouping Identifier\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+					
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";		
		return content;
	}
	
	protected  String generateXmlContent_cmdParam_noKey(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <cmd_class key=\"0x85\" version=\"2\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "    <cmd key=\"0x02\" name=\"ASSOCIATION_GET\" help=\"Association Get\" comment=\"\">" + "\r\n"+
						 "      <param  name=\"Grouping Identifier\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+					
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_cmdParam_noName(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <cmd_class key=\"0x85\" version=\"2\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "    <cmd key=\"0x02\" name=\"ASSOCIATION_GET\" help=\"Association Get\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+					
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_cmdParam_noType(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <cmd_class key=\"0x85\" version=\"2\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "    <cmd key=\"0x02\" name=\"ASSOCIATION_GET\" help=\"Association Get\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" name=\"Grouping Identifier\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+					
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_cmdParam_noTypeHashcode(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <cmd_class key=\"0x85\" version=\"2\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "    <cmd key=\"0x02\" name=\"ASSOCIATION_GET\" help=\"Association Get\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" name=\"Grouping Identifier\" type=\"BYTE\" comment=\"\">" + "\r\n"+
						 "         <valueattrib key=\"0x00\" hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+					
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_cmdParamValueAttr_noKey(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <cmd_class key=\"0x85\" version=\"2\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "    <cmd key=\"0x02\" name=\"ASSOCIATION_GET\" help=\"Association Get\" comment=\"\">" + "\r\n"+
						 "      <param key=\"0x00\" name=\"Grouping Identifier\" type=\"BYTE\" typehashcode=\"0x01\" comment=\"\">" + "\r\n"+
						 "         <valueattrib hasdefines=\"False\" showhex=\"True\" />" + "\r\n"+
						 "      </param>" + "\r\n"+
						 "    </cmd>" + "\r\n"+					
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";		
		return content;
	}
	
		
//	protected  String generateXmlContent_cmdClass_noCommands(){
//		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
//						 "<zw_classes>" + "\r\n"+
//						 "  <cmd_class key=\"0x85\" version=\"2\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
//						 "  </cmd_class>" + "\r\n"+
//						 "</zw_classes>";		
//		return content;
//	}
	
	protected  String generateXmlContent_cmdClass_noKey(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <cmd_class version=\"2\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_cmdClass_noName(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_CONTROLLER\" key=\"0x01\" help=\"Controller\" comment=\"Node is a portable controller \" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_ROUTING_SLAVE\" key=\"0x04\" help=\"Routing Slave\" comment=\"Node is a slave with routing capabilities\" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_SLAVE\" key=\"0x03\" help=\"Slave\" comment=\"Node is a slave\" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_STATIC_CONTROLLER\" key=\"0x02\" help=\"Static Controller\" comment=\"Node is a static controller\" />" + "\r\n"+
						 "  <gen_dev key=\"0x10\" name=\"GENERIC_TYPE_SWITCH_BINARY\" help=\"Switch Binary\" read_only=\"False\" comment=\"Binary Switch\">" + "\r\n"+
						 "     <spec_dev key=\"0x00\" name=\"SPECIFIC_TYPE_NOT_USED\" help=\"Not Used\" comment=\"Specific Device Class not used\" />" + "\r\n"+
						 "  </gen_dev>" + "\r\n"+
						 "  <cmd_class key=\"0x85\" version=\"2\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_cmdClass_noVersion(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_CONTROLLER\" key=\"0x01\" help=\"Controller\" comment=\"Node is a portable controller \" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_ROUTING_SLAVE\" key=\"0x04\" help=\"Routing Slave\" comment=\"Node is a slave with routing capabilities\" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_SLAVE\" key=\"0x03\" help=\"Slave\" comment=\"Node is a slave\" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_STATIC_CONTROLLER\" key=\"0x02\" help=\"Static Controller\" comment=\"Node is a static controller\" />" + "\r\n"+
						 "  <gen_dev key=\"0x10\" name=\"GENERIC_TYPE_SWITCH_BINARY\" help=\"Switch Binary\" read_only=\"False\" comment=\"Binary Switch\">" + "\r\n"+
						 "     <spec_dev key=\"0x00\" name=\"SPECIFIC_TYPE_NOT_USED\" help=\"Not Used\" comment=\"Specific Device Class not used\" />" + "\r\n"+
						 "  </gen_dev>" + "\r\n"+
						 "  <cmd_class key=\"0x85\" name=\"COMMAND_CLASS_ASSOCIATION\" help=\"Command Class Association\" read_only=\"False\" comment=\"\">" + "\r\n"+
						 "  </cmd_class>" + "\r\n"+
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_specDev_noKey(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_CONTROLLER\" key=\"0x01\" help=\"Controller\" comment=\"Node is a portable controller \" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_ROUTING_SLAVE\" key=\"0x04\" help=\"Routing Slave\" comment=\"Node is a slave with routing capabilities\" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_SLAVE\" key=\"0x03\" help=\"Slave\" comment=\"Node is a slave\" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_STATIC_CONTROLLER\" key=\"0x02\" help=\"Static Controller\" comment=\"Node is a static controller\" />" + "\r\n"+
						 "  <gen_dev key=\"0x10\" name=\"GENERIC_TYPE_SWITCH_BINARY\" help=\"Switch Binary\" read_only=\"False\" comment=\"Binary Switch\">" + "\r\n"+
						 "     <spec_dev name=\"SPECIFIC_TYPE_NOT_USED\" help=\"Not Used\" comment=\"Specific Device Class not used\" />" + "\r\n"+
						 "  </gen_dev>" + "\r\n"+						
						 "</zw_classes>";		
		return content;
	}
	
	protected  String generateXmlContent_specDev_noName(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_CONTROLLER\" key=\"0x01\" help=\"Controller\" comment=\"Node is a portable controller \" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_ROUTING_SLAVE\" key=\"0x04\" help=\"Routing Slave\" comment=\"Node is a slave with routing capabilities\" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_SLAVE\" key=\"0x03\" help=\"Slave\" comment=\"Node is a slave\" />" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_STATIC_CONTROLLER\" key=\"0x02\" help=\"Static Controller\" comment=\"Node is a static controller\" />" + "\r\n"+
						 "  <gen_dev key=\"0x10\" name=\"GENERIC_TYPE_SWITCH_BINARY\" help=\"Switch Binary\" read_only=\"False\" comment=\"Binary Switch\">" + "\r\n"+
						 "     <spec_dev key=\"0x00\" help=\"Not Used\" comment=\"Specific Device Class not used\" />" + "\r\n"+
						 "  </gen_dev>" + "\r\n"+						
						 "</zw_classes>";		
		return content;
	}
	
	protected  String generateXmlContent_basDev_nan(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <bas_dev read_only=\"False\" key=\"0xs1\" name=\"BASIC_TYPE_CONTROLLER\" help=\"Controller\" comment=\"Node is a portable controller \" />" + "\r\n"+						
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_basDev_noKey(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_CONTROLLER\" help=\"Controller\" comment=\"Node is a portable controller \" />" + "\r\n"+						
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_basDev_noName(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <bas_dev read_only=\"False\" key=\"0x01\" help=\"Controller\" comment=\"Node is a portable controller \" />" + "\r\n"+						
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_genDev_noKey(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_CONTROLLER\" key=\"0x01\" help=\"Controller\" comment=\"Node is a portable controller \" />" + "\r\n"+
						 "  <gen_dev name=\"GENERIC_TYPE_SWITCH_BINARY\" help=\"Switch Binary\" read_only=\"False\" comment=\"Binary Switch\">" + "\r\n"+						 
						 "  </gen_dev>" + "\r\n"+						 
						 "</zw_classes>";
		
		return content;
	}
	
	protected  String generateXmlContent_genDev_noName(){
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n"+
						 "<zw_classes>" + "\r\n"+
						 "  <bas_dev read_only=\"False\" name=\"BASIC_TYPE_CONTROLLER\" key=\"0x01\" help=\"Controller\" comment=\"Node is a portable controller \" />" + "\r\n"+
						 "  <gen_dev key=\"0x10\" help=\"Switch Binary\" read_only=\"False\" comment=\"Binary Switch\">" + "\r\n"+
						 
						 "  </gen_dev>" + "\r\n"+
						 
						 "</zw_classes>";
		
		return content;
	}
	
	
}
