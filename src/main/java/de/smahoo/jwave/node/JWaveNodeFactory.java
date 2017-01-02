package de.smahoo.jwave.node;

import java.util.Collection;
import java.util.List;

import de.smahoo.jwave.cmd.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.JWaveException;
import de.smahoo.jwave.cmd.JWaveCommandClassSpecification;
import de.smahoo.jwave.event.JWaveEventType;
import de.smahoo.jwave.event.JWaveNodeEvent;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveNodeFactory {
	
	private JWaveCommandClassSpecification JWaveDefs = null;
	
	public JWaveNodeFactory(JWaveCommandClassSpecification defs){
		this.JWaveDefs = defs;
	
	}
	
	public JWaveNode createNode(int id){
		JWaveNode node = new JWaveNode();
		node.nodeId = id;		
		return node;
	}
	
	public JWaveNode createNode(List<JWaveCommandClass> cmdClasses, int id, int status, int basic, int generic, int specific){
		JWaveNode node = createNode(id);		
		configNode(node,cmdClasses, status, basic,generic,specific);
		return node;
	}
	
	public void configNode(JWaveNode node, List<JWaveCommandClass> cmdClasses, int status, int basic, int generic, int specific){
		setNodeDeviceType(node, basic, generic, specific);
		setNodeStatus(node, status);	
		node.setCmdClasses(cmdClasses);
	}
	
	
	
	public void setNodeDeviceType(JWaveNode node, int basic, int generic, int specific){
		JWaveGenericDeviceType genDevType = JWaveDefs.getGenericDeviceType(generic);
		JWaveSpecificDeviceType specDevType = null;
		if (genDevType != null){
			specDevType = genDevType.getSpecificDeviceType(specific);
		}		
		
		JWaveBasicDeviceType basDevType = JWaveDefs.getBasicDeviceType(basic);
		node.setDeviceType(basDevType, genDevType, specDevType);
		
	}
	
	public void setNodeCapabilities(JWaveNode node, int capabilities){
		node.capability = (byte)(capabilities&0xFF);
	}
	
	public void setNodeStatus(JWaveNode node, int Status){
		// TODO: not implemented yet
	}
	
	public void setNodeSecurity(JWaveNode node, int security){
		
	}

	public static void assignCommandClass(JWaveNode node, JWaveCommandClass cmdClass){
		node.cmdClasses.add(cmdClass);
	}

	public static void assignCommandClasses(JWaveNode node, Collection<JWaveCommandClass> cmdClassList){
		node.cmdClasses.addAll(cmdClassList);
	}
	
	public synchronized void assignNodeCmd(JWaveNode node, JWaveNodeCommand cmd){
		if (node == null){
			return;
		}
		switch (cmd.getCommandClass().getKey()){
		case 0x86:	// COMMAND_CLASS_VERSION
			try {
				evaluateVersionCmd(node, cmd);
			} catch (Exception exc){
				JWaveController.log(LogTag.ERROR,exc.getMessage(),exc);
			}
			break;
		}
		if (cmd instanceof JWaveMultiNodeCommand){
			JWaveMultiNodeCommand multiCmd = (JWaveMultiNodeCommand)cmd;
			for (JWaveNodeCommand c : multiCmd.getNodeCmdList()){
				node.onNodeCmdReceived(c);
			}
		} else {
			node.onNodeCmdReceived(cmd);
		}
	}
	
	public Element generateNodeConfigurationElement(Document doc, JWaveNode node){
		Element elem = doc.createElement("node");
		
		elem.setAttribute("manufacturerid",""+node.getManufactureId());
		elem.setAttribute("producttype", ""+node.getProductTypeId());
		elem.setAttribute("productid",""+node.getProductId());
		
		elem.setAttribute("id",""+node.getNodeId());
		if (node.getGenericDeviceType() != null){
			elem.setAttribute("generic",""+node.getGenericDeviceType().getKey());
		}
		if (node.getSpecificDeviceType() != null){
			elem.setAttribute("specific",""+node.getSpecificDeviceType().getKey());
		}
		
		
		
		for (JWaveCommandClass cmdClass : node.getSecuritySupportedCmdClasses()){
			elem.appendChild(generateNodeCommandClassConfigurationElement(doc, cmdClass, true));
		}
		
		for (JWaveCommandClass cmdClass : node.getNonSecuritySupportedCmdClasses()){
			elem.appendChild(generateNodeCommandClassConfigurationElement(doc, cmdClass, false));
		}
		
		return elem;
	}
	
	// TODO error occurs here. cmdCLass is null
	protected Element generateNodeCommandClassConfigurationElement(Document doc, JWaveCommandClass cmdClass, boolean secure){
		Element elem = doc.createElement("cmdclass");
		
		elem.setAttribute("key",""+cmdClass.getKey());
		elem.setAttribute("name",cmdClass.getName()); // not needed at all. version and key are enough to identify cmdClass
		elem.setAttribute("version",""+cmdClass.getVersion());		
		elem.setAttribute("secure", ""+secure);	
		return elem;
	}
	
	
	public synchronized void init(JWaveNode node, Element elem) throws JWaveException{
		if (node == null){
			throw new JWaveException("there is no node to init");		
		}
		
		JWaveGenericDeviceType genType = null;
		JWaveSpecificDeviceType specType = null;
		if (elem.hasAttribute("generic")){
			genType = JWaveDefs.getGenericDeviceType(Integer.parseInt(elem.getAttribute("generic")));
		}
		if (elem.hasAttribute("specific")){
			if (genType != null){
				specType = genType.getSpecificDeviceType(Integer.parseInt(elem.getAttribute("specific")));
			}
		}
		
		// manufacturerid="-1" productid="-1" producttype="-1" specific="1">
		if (elem.hasAttribute("manufacturerid")){
			node.manufactureId = Integer.parseInt(elem.getAttribute("manufacturerid"));
		}
		if (elem.hasAttribute("producttype")){
			node.productTypeId = Integer.parseInt(elem.getAttribute("producttype"));
		}
		if (elem.hasAttribute("productid")){
			node.productId = Integer.parseInt(elem.getAttribute("productid"));
		}
		
		if (genType != null){			
			if (node.getGenericDeviceType().getKey() != genType.getKey()){
				throw new JWaveNodeConfigurationException("Generic type of configuration ("+genType.getKey()+
						") data is not the same then the one of the node ("+node.getGenericDeviceType().getKey()+"). Aborting node configuration", node); 
				
			}
		}
		if (specType != null){			
			if (node.getSpecificDeviceType().getKey() != specType.getKey()){
				throw new JWaveNodeConfigurationException("Specific type of configuration ("+specType.getKey()+
						") data is not the same then the one of the node ("+node.getSpecificDeviceType().getKey()+"). Aborting node configuration", node); 
				
			}
		}
			
		NodeList nodeList = elem.getChildNodes();
		Element tmp;
		JWaveCommandClass cmdClass = null;
		for (int i=0; i<nodeList.getLength(); i++){
			if (nodeList.item(i) instanceof Element){
				tmp = (Element)nodeList.item(i);
				cmdClass = getCommandClass(tmp);
				if (cmdClass != null){
					if (tmp.hasAttribute("secure")){
						if (tmp.getAttribute("secure").equalsIgnoreCase("true")){
							node.secSuppCmdClasses.add(cmdClass);
						} else {
							node.cmdClasses.add(cmdClass);
						}
					} else {
						node.cmdClasses.add(cmdClass);
					}
				}
			}
		}
		node.nodeCommunicator.mode = 2;
		if (node.supportsClassWakeUp()){
			node.sleepMode = JWaveNodeSleepMode.SLEEP_MODE_SLEEPING;
		} else {
			node.sleepMode = JWaveNodeSleepMode.SLEEP_MODE_NONE;
		}
		node.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_CONFIG_CHANGED, node));
		
	}
	
	protected JWaveCommandClass getCommandClass(Element elem){
		JWaveCommandClass cmdClass = null;
		
		int vers = Integer.parseInt(elem.getAttribute("version"));
		int key  = Integer.parseInt(elem.getAttribute("key"));
		
		cmdClass = this.JWaveDefs.getCommandClass(key, vers);
		
		return cmdClass;
	}
	
	
	protected synchronized void evaluateVersionCmd(JWaveNode node, JWaveNodeCommand cmd) throws JWaveException{
		try {
			switch (cmd.getCommandKey()){
			case 0x14:			
				if (cmd.getParamValue(0x01) != null){
					if (cmd.getParamValue(0x00)!= null){
						byte key = JWaveCommandParameterType.toByte(cmd.getParamValue(0x00));
						byte version = JWaveCommandParameterType.toByte(cmd.getParamValue(0x01));
						setCommandClassValue(node, key&0xFF, version&0xFF);
					} else {
						// FIXME: handle that! wron g param value transmitted or definition has been changed.
						JWaveController.log(LogTag.WARN,"Unable to find param 0x00 for cmd "+cmd.getCommand().getName());
					}
				} else {
					// FIXME: handle that! wrong param value transmitted or definition has been changed.
					JWaveController.log(LogTag.WARN,"Unable to find param 0x01 for cmd "+cmd.getCommand().getName());
				}
			break;
			}
		} catch (Exception exc){
			throw new JWaveException("Unable to evaluate VersionCmd", exc);
		}
	}
	
	public synchronized void setCommandClassValue(JWaveNode node, int key, int version){
		JWaveCommandClass cmdClass = this.JWaveDefs.getCommandClass(key, version);
		if (cmdClass == null){
			// FIXME: why is that null?	
			JWaveController.log(LogTag.DEBUG,"JWaveNodeFactory.setCommandClassValue: Command Classes at node is null! (key = "+key+", version = "+version+")");
		}
		JWaveCommandClass oldClass = null;
		List<JWaveCommandClass> cmdClasses = node.getCommandClasses();
			for (JWaveCommandClass cc : cmdClasses){
				if (cc != null){
					if (cc.getKey() == key){
						if (cc.getVersion() != version){
							oldClass = cc;
						}
					}
				} else {
					//FIXME: that never should happen
					JWaveController.log(LogTag.DEBUG,"JWaveNodeFactory.setCommandClassValue: cc is null!");
				}
			}
		synchronized (node.cmdClasses) {
			if ((oldClass != null) && (cmdClass != null)){
				JWaveController.log(LogTag.DEBUG,"Removing oldClass ("+oldClass.getName()+" v"+oldClass.getVersion()+")");
				node.cmdClasses.remove(oldClass);
				JWaveController.log(LogTag.DEBUG,"Adding cmdClass ("+cmdClass.getName()+" v"+cmdClass.getVersion()+")");
				node.cmdClasses.add(cmdClass);
				node.dispatchNodeEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_CONFIG_CHANGED, node));			
			}
		}
		
	}
}
