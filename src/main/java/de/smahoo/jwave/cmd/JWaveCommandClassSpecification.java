package de.smahoo.jwave.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import de.smahoo.jwave.JWaveController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.smahoo.jwave.utils.logger.LogTag;
import de.smahoo.jwave.utils.xml.XmlConvertionException;
import de.smahoo.jwave.utils.xml.XmlUtils;



/**
 * The Z-Wave Alliance specified the complete Command Classes within one XML file. 
 * 
 * @author Mathias Runge (mathias.runge@smahoo.de)
 *
 */
public class JWaveCommandClassSpecification {
	
	protected List<JWaveCommandClass> commandClasses;
	protected HashMap<Integer, JWaveGenericDeviceType> deviceTypes;
	protected List<JWaveBasicDeviceType> basicDevTypes;
	
	public JWaveCommandClassSpecification(Document doc) throws XmlConvertionException{
		if (doc == null){
			throw new NullPointerException("Given XML document is null");
		}
		System.out.println("Initializing JWaveCommandClassSpecification");
		init(doc);
		
	}
	
	public JWaveCommandClassSpecification(String filename) throws IOException,XmlConvertionException{
		try {
			load(filename);
		}catch (IOException exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			throw new IOException("Unable to generate command classes from given specification file '"+filename+"'."+exc.getMessage(),exc);
		}
	}
	
	public Collection<JWaveGenericDeviceType> getGenericDeviceTypes(){		
		return deviceTypes.values();
	}
	
	public JWaveGenericDeviceType getGenericDeviceType(int key){
		return deviceTypes.get(key);
	}
	
	public List<JWaveBasicDeviceType> getBasicDeviceTypes(){
		return basicDevTypes;
	}
	
	public JWaveBasicDeviceType getBasicDeviceType(int key){		
		for (JWaveBasicDeviceType bt : basicDevTypes){
			if (bt.getKey() == key){
				return bt;
			}
		}
		return null;
	}
	
	public List<JWaveCommandClass> getCommandClasses(){
		return commandClasses;
	}
	
	public JWaveCommandClass getCommandClass(int key){
		for (JWaveCommandClass cmdClass : commandClasses){
			if (cmdClass.getKey() == key){
				return cmdClass;
			}
		}
		return null;
	}
	
	public JWaveCommand getCommand(int cl_key, int version, int cmd_key){
		JWaveCommandClass cmdClass = getCommandClass(cl_key,version);
		if (cmdClass != null){
			return cmdClass.getCommand(cmd_key);
		}
		return null;
	}
	
	public JWaveCommand getCommand(String className, int version, String cmdName){
		JWaveCommandClass cmdClass = getCommandClass(className,version);
		if (cmdClass != null){
			return cmdClass.getCommand(cmdName);
		}
		return null;
	}
	
	public JWaveCommand getCommand(String className, String cmdName){
		return getCommand(className, 1, cmdName);
	}
	
	public JWaveCommandClass getCommandClass(String name){
		return getCommandClass(name,1);
	}
	
	public JWaveCommandClass getCommandClass(String name, int version){
		for (JWaveCommandClass cmdClass : commandClasses){
			if (cmdClass.getName().equalsIgnoreCase(name)){
				if (cmdClass.getVersion() == version){
					return cmdClass;
				}
			}
		}
		return null;
	}
	
	public JWaveCommandClass getCommandClass(int key, int version){
		JWaveCommandClass tmp = null;
		for (JWaveCommandClass cmdClass : commandClasses){
			if (cmdClass.getKey() == key){
				if (cmdClass.getVersion() == version ){
					return cmdClass;
				}				
			}
		}
		return tmp;
	}

	protected void load(String filename) throws IOException, XmlConvertionException{
		File file = new File(filename);
		if (!file.exists()){		
			throw new IOException("File '"+filename+"' does not exist!");
		}
		if (!file.isFile()){
			throw new IOException("'"+filename+"' is not a file!");
		}
	       
		Document doc = XmlUtils.loadXml(file);
		init(doc);
		
	}
	
	protected void init(Document doc) throws XmlConvertionException{
			
		Element root = doc.getDocumentElement();		
		NodeList cmdList = root.getElementsByTagName("cmd_class");
		// FIXME: handle exceptions
		initCommandClasses(cmdList);
		
		NodeList genDevList = root.getElementsByTagName("gen_dev");
		initGenericDevices(genDevList);
		
		NodeList basDevList = root.getElementsByTagName("bas_dev");
		initBasicDevices(basDevList);
	}
	
	protected void initBasicDevices(NodeList nodeList) throws XmlConvertionException{
		
		
		this.basicDevTypes = new ArrayList<JWaveBasicDeviceType>();
		
		for (int i= 0; i< nodeList.getLength(); i++){
			if (nodeList.item(i) instanceof Element){
				basicDevTypes.add(generateBasicDeviceType((Element)nodeList.item(i)));
			}
		}
	}
	
	protected JWaveBasicDeviceType generateBasicDeviceType(Element elem) throws XmlConvertionException{
		
		
		checkNotNull(elem);
		checkAttribute(elem,"key");
		checkAttribute(elem,"name");
		checkAttribute(elem,"read_only");
		
		JWaveBasicDeviceType basDev = new JWaveBasicDeviceType();
		
		try {
			basDev.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""),16);
			basDev.readOnly = Boolean.parseBoolean(elem.getAttribute("read_only"));
			basDev.name = elem.getAttribute("name");
			basDev.comment = elem.getAttribute("comment");
			basDev.help = elem.getAttribute("help");
		} catch (Exception exc){
			throw new XmlConvertionException("Unable to generate basic device type with given XML element.",elem,exc);
		}
		
		
		return basDev;
	}
	
	protected void initGenericDevices(NodeList nodeList) throws XmlConvertionException{
		this.deviceTypes = new HashMap<Integer, JWaveGenericDeviceType>();
		
		JWaveGenericDeviceType devType;
		for (int i=0; i< nodeList.getLength(); i++){
			if (nodeList.item(i) instanceof Element){
				// FIXME: handle exceptions
				devType = generateGenericDeviceType((Element)nodeList.item(i));
				deviceTypes.put(devType.getKey(),devType);
			}
		}
	}
	
	
	protected JWaveGenericDeviceType generateGenericDeviceType(Element elem) throws XmlConvertionException{
		
		checkNotNull(elem);
		checkAttribute(elem,"key");
		checkAttribute(elem,"name");
		
			
		
		JWaveGenericDeviceType devType = new JWaveGenericDeviceType();
		
		try {
			devType.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""),16);		
			devType.name = elem.getAttribute("name");
			if (elem.hasAttribute("read_only")){
			    devType.readOnly = Boolean.parseBoolean(elem.getAttribute("read_only"));
			}
			
			if (elem.hasAttribute("comment")){
				devType.comment = elem.getAttribute("comment");
			}
			
			if (elem.hasAttribute("help")){
				devType.help = elem.getAttribute("help");
			}
		} catch (Exception exc){
			throw new XmlConvertionException("Unable to generate generic device type with given XML element.",elem,exc);
		}
		NodeList specificList = elem.getChildNodes(); 
		
		for (int i= 0; i<specificList.getLength(); i++){
			if (specificList.item(i) instanceof Element){
				devType.specDevTypes.add(generateSpecificDeviceType((Element)specificList.item(i)));
			}
			
		}
		
		return devType;
	}
	
	protected JWaveSpecificDeviceType generateSpecificDeviceType(Element elem) throws XmlConvertionException{
		checkNotNull(elem);
		checkAttribute(elem,"key");
		checkAttribute(elem,"name");
		
					
		
		JWaveSpecificDeviceType st = new JWaveSpecificDeviceType();
		try {
			st.name = elem.getAttribute("name");
			st.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""),16);
		
			if (elem.hasAttribute("comment")){
				st.comment = elem.getAttribute("comment");
			}
			
			if (elem.hasAttribute("help")){
				st.help = elem.getAttribute("help");
			}
			
		} catch (Exception exc){
			throw new XmlConvertionException("Unable to generate specific device type with given XML element.",elem,exc);
		}
		
		return st;
	}
	
	protected void initCommandClasses(NodeList cmdList) throws XmlConvertionException{
		commandClasses = new ArrayList<JWaveCommandClass>();
		
		for (int i=0; i< cmdList.getLength(); i++){
			if (cmdList.item(i) instanceof Element){
				// FIXME: handle exceptions
				JWaveCommandClass cmdClass = generateCommandClass((Element)cmdList.item(i));
				// Some Command Classes do not have any command ( 0xef - COMMAND_CLASS_MARK)
				// Thus, they can't be used and causes errors
				// FIXME: find better solution than ignoring these classes
				if (!cmdClass.getCommandList().isEmpty()){					
					commandClasses.add(cmdClass);
				}
			}
		}
	}
	
	protected JWaveCommandClass generateCommandClass(Element elem) throws XmlConvertionException{
		
		checkNotNull(elem);
		checkAttribute(elem,"key");
		checkAttribute(elem,"name");
		checkAttribute(elem,"version");
	
		JWaveCommandClass cmdClass = new JWaveCommandClass();
		
		try {
			cmdClass.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""),16);
			cmdClass.name = elem.getAttribute("name");		
			cmdClass.version = Integer.parseInt(elem.getAttribute("version"));
		
			if (elem.hasAttribute("help")){
				cmdClass.help = elem.getAttribute("help");
			}
		
			if (elem.hasAttribute("read_only")){
				cmdClass.readOnly = Boolean.parseBoolean(elem.getAttribute("read_only"));
			}
		
			if (elem.hasAttribute("comment")){
				cmdClass.comment = elem.getAttribute("comment");
			}	
		} catch (Exception exc){
			throw new XmlConvertionException("Unable to generate command class with given XML element,",elem,exc);
		}
	
		initCommands(cmdClass, elem.getElementsByTagName("cmd"));		
		
		return cmdClass;
	}
	
	
	protected void initCommands(JWaveCommandClass cmdClass, NodeList cmdList) throws XmlConvertionException{
		
		JWaveCommand cmd = null;
		
		for (int i = 0; i< cmdList.getLength(); i++){
			if (cmdList.item(i) instanceof Element){
				
					cmd = generateCommand((Element)cmdList.item(i));;
					cmd.cmdClass = cmdClass;
				
				cmdClass.cmdList.add(cmd);
			}
		}
		
		
	}
	
	protected JWaveCommand generateCommand(Element elem) throws XmlConvertionException{
		
		checkNotNull(elem);
		checkAttribute(elem,"key");
		checkAttribute(elem,"name");
		
		
		JWaveCommand cmd = new JWaveCommand();
		
		try {
			if (elem.hasAttribute("help")){
				cmd.help = elem.getAttribute("help");
			}
		
			cmd.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""),16);
			cmd.name = elem.getAttribute("name");
		} catch (Exception exc){
			throw new XmlConvertionException("Unalbe to generate command with given XML element.",elem,exc);
		}
	
		NodeList nodeList = elem.getChildNodes();
		
	
		generateParams(cmd, nodeList);
		
		return cmd;
	}
	
	protected void generateParams(JWaveCommand cmd, NodeList paramList) throws XmlConvertionException{
		Element tmp;
		for (int i = 0; i<paramList.getLength(); i++){
			if (paramList.item(i) instanceof Element){
				tmp = (Element)paramList.item(i);
				if ("param".equalsIgnoreCase(tmp.getTagName())){
					cmd.params.add(generateCmdParam(cmd,tmp));	
				}
				if ("variant_group".equalsIgnoreCase(tmp.getTagName())){
					cmd.params.add(generateCmdParamVariantGroup(cmd,tmp));
				}
							
			}
		}
		
		
		
		 Collections.sort(cmd.params, new Comparator<JWaveCommandParameter>() {
		        public int compare(JWaveCommandParameter p1, JWaveCommandParameter p2) {
		        	if (p1.getKey() > p2.getKey()){
		        		return 1;
		        	} else {
		        		return -1;
		        	}
		        }
		   }); 
		
	}
	
	protected JWaveCommandParameter generateCmdParam(JWaveCommand cmd, Element elem) throws XmlConvertionException{
		checkNotNull(elem);
		checkAttribute(elem,"key");
		checkAttribute(elem,"name");
		checkAttribute(elem,"type");
		checkAttribute(elem,"typehashcode");
		
		
		JWaveCommandParameter param = new JWaveCommandParameter();
		
		try {
			param.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""), 16);
			param.name = elem.getAttribute("name");
			param.type = JWaveCommandParameterType.valueOf(elem.getAttribute("type").toUpperCase());
			param.typeHashCode = Integer.parseInt(elem.getAttribute("typehashcode").replace("0x",""),16);
			param.cmd = cmd;
		} catch (Exception exc){
			throw new XmlConvertionException("Unable to generate command parameter with given XML element",elem,exc);
		}
		
		addValues(param, elem);
		
		return param;
	
	}
	
	protected JWaveCommandParameterVariantGroup generateCmdParamVariantGroup(JWaveCommand cmd, Element elem) throws XmlConvertionException{
		checkAttribute(elem,"key");
		checkAttribute(elem,"name");
		checkAttribute(elem,"typehashcode");
		
								
		JWaveCommandParameterVariantGroup group = new JWaveCommandParameterVariantGroup();
		
		try {
			group.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""), 16);
			group.name = elem.getAttribute("name");		
			group.typeHashCode = Integer.parseInt(elem.getAttribute("typehashcode").replace("0x",""),16);
			group.cmd = cmd;
		} catch (Exception exc){
			throw new XmlConvertionException("Unable to generate command param variant group with given XML element",elem,exc);
		}
		
		if (elem.hasChildNodes()){
			Element tmp;
			NodeList childs = elem.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++){
				if (childs.item(i) instanceof Element){
					tmp = (Element)childs.item(i);
					if ("param".equalsIgnoreCase(tmp.getTagName())){
						group.addCmdParam(generateCmdParam(cmd,tmp));
					}
				}
			}
		}
		
		return group;
	}
	
	protected void addValues(JWaveCommandParameter param, Element elem) throws XmlConvertionException{
	
		Element tmp;
		NodeList nodeList = elem.getChildNodes();
		for (int i = 0; i<nodeList.getLength();i++){
			if (nodeList.item(i) instanceof Element){
				tmp = (Element)nodeList.item(i);
				if (tmp.getTagName().equalsIgnoreCase("bitflag")){					
					param.values.add(generateBitflag(tmp));
				}
				if (tmp.getTagName().equalsIgnoreCase("bitmask")){
					param.values.add(generateBitmask(tmp));
				}
				if (tmp.getTagName().equalsIgnoreCase("bitfield")){
					param.values.add(generateBitfield(tmp));
				}
				if (tmp.getTagName().equalsIgnoreCase("valueattrib")){
					param.values.add(generateAttribute(tmp));
				}
				if (tmp.getTagName().equalsIgnoreCase("const")){
					param.values.add(generateConst(tmp));
				}
				
			}
		}
	}
	
	protected JWaveCommandParameterBitflag generateBitflag(final Element elem) throws XmlConvertionException{
		checkNotNull(elem);
		checkAttribute(elem,"key");
		checkAttribute(elem,"flagmask");
		checkAttribute(elem,"flagname");
		
		JWaveCommandParameterBitflag flag = new JWaveCommandParameterBitflag();
		
		try {			
			flag.flagmask = Integer.parseInt(elem.getAttribute("flagmask").replace("0x",""),16);
			flag.flagname = elem.getAttribute("flagname");
			flag.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""),16);
		} catch (Exception exc){
			throw new XmlConvertionException("Unable to generate command param bitflag with given XML element.",elem,exc);
		}
		return flag;
	}
	
	protected JWaveCommandParameterBitmask generateBitmask(final Element elem) throws XmlConvertionException{
		checkNotNull(elem);
		checkAttribute(elem,"key");
		checkAttribute(elem,"lenmask");
		checkAttribute(elem,"paramoffs");
		checkAttribute(elem,"lenoffs");
				
		JWaveCommandParameterBitmask mask = new JWaveCommandParameterBitmask();
		try {
			mask.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""),16);
			mask.lenmask = Integer.parseInt(elem.getAttribute("lenmask").replace("0x",""),16);
			mask.paramoffs = Integer.parseInt(elem.getAttribute("paramoffs"));
			mask.lenoffs = Integer.parseInt(elem.getAttribute("lenoffs"));
		} catch (Exception exc){
			throw new XmlConvertionException("Unable to generate cmd param bitmask with given XML element.",elem,exc);
		}
		return mask;
	}
	
	protected JWaveCommandParameterBitfield generateBitfield(final Element elem) throws XmlConvertionException{
		
		checkNotNull(elem);
		checkAttribute(elem,"key");
		checkAttribute(elem,"fieldmask");
		checkAttribute(elem,"fieldname");
		checkAttribute(elem,"shifter");
		JWaveCommandParameterBitfield field = new JWaveCommandParameterBitfield();
		
		try {
			field.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""),16);
			field.fieldmask = Integer.parseInt(elem.getAttribute("fieldmask").replace("0x",""),16);
			field.fieldname = elem.getAttribute("name");
			field.shifter = Integer.parseInt(elem.getAttribute("shifter"));
		} catch (Exception exc){
			throw new XmlConvertionException("Unable to generate command param bitfield with given XML element.",elem,exc);
		}
		return field;
	}
	
	protected JWaveCommandParameterAttribute generateAttribute(final Element elem) throws XmlConvertionException{
		
		checkNotNull(elem);
		checkAttribute(elem,"key");	
	
		
		JWaveCommandParameterAttribute attr = new JWaveCommandParameterAttribute();
		try {
		  attr.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""),16);	
		  if (elem.hasAttribute("showHex")){
		    attr.showHex = Boolean.parseBoolean("showHex");
		  }
		} catch (Exception exc){
			throw new XmlConvertionException("Unable to generate command param attribute. "+exc.getMessage(),elem,exc);
		}
		return attr;
	}	
	
	protected JWaveCommandParameterConstant generateConst(final Element elem) throws XmlConvertionException{
		
				
		checkNotNull(elem);		
		checkAttribute(elem,"key");
		checkAttribute(elem,"flagname");
		checkAttribute(elem,"flagmask");
			
		JWaveCommandParameterConstant c = new JWaveCommandParameterConstant();
		c.key = Integer.parseInt(elem.getAttribute("key").replace("0x",""),16);
		c.flagName = elem.getAttribute("flagname");
		c.flagMask = Integer.parseInt(elem.getAttribute("flagmask").replace("0x",""),16);
			
		return c;
	}
	
	protected void checkNotNull(Element elem) throws XmlConvertionException{
		if (elem == null){
			throw new XmlConvertionException("Given XML element is null");
		}
	}
	
	protected void checkAttribute(Element elem, String attribute) throws XmlConvertionException{
		if (!elem.hasAttribute(attribute)){
			throw new XmlConvertionException("Given XML element '"+elem.getTagName()+"' does not contain attribute '"+attribute+"'.",elem);
		}
	}
	
	protected Document loadXml(File file) throws IOException{		
	      
    	BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
        
        br.close();
        String everything = sb.toString();
        
        return parseDoc(everything);
	}
	
	protected Document parseDoc(String data) throws IOException{
			Document result = null;		
			DocumentBuilder docBuilder;
			DocumentBuilderFactory docBFac;
			StringReader inStream;
			InputSource inSource;	
			
			try {
				inStream = new StringReader(data);
				inSource = new InputSource(inStream);
				docBFac = DocumentBuilderFactory.newInstance();
				docBuilder = docBFac.newDocumentBuilder();			
				result = docBuilder.parse(inSource);
			} catch (Exception exc){
				JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
				throw new IOException("Unable to parse given XML content." +exc.getMessage(),exc);
			}		
			return result;
	}
	
}
