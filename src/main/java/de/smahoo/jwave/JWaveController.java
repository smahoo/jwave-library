package de.smahoo.jwave;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.smahoo.jwave.cmd.*;
import de.smahoo.jwave.event.*;
import de.smahoo.jwave.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.smahoo.jwave.cmd.JWaveCommandClass;
import de.smahoo.jwave.event.JWaveControlEvent;
import de.smahoo.jwave.io.JWaveNodeCommandDatagram;
import de.smahoo.jwave.node.JWaveNode;
import de.smahoo.jwave.node.JWaveNodeCommunicator;
import de.smahoo.jwave.node.JWaveNodeFactory;
import de.smahoo.jwave.node.JWaveNodeInterviewOptions;
import de.smahoo.jwave.utils.logger.LogTag;
import de.smahoo.jwave.utils.logger.Logger;
import de.smahoo.jwave.utils.logger.LoggerConsolePrinter;
import de.smahoo.jwave.utils.xml.XmlConvertionException;
import de.smahoo.jwave.utils.xml.XmlUtils;

/**
 * The JWave library basically communicates not directly to the Z-Wave nodes in one single network. To be more precisely,
 * the communication takes places between this library and the Z-Wave controller that hosts the network. The class
 * JWaveController therefore contains all necessary elements to handle the datagrams that were exchanged with the physical
 * Z-Wave controller.
 *
 * @author Mathias Runge (mathias.runge@smahoo.de)
 */
public class JWaveController {

	// consts
	private static final String VERSION="0.9.83";  // Main.Sub.Build
	private static final String FILE_COMMAND_CLASS_SPECIFICATIONS = "cmd_classes.xml";
	private static final long DEFAULT_CONTROLLER_MODE_TIMEOUT = 300000; // 5 minutes in milliseconds
	
	// sub modules
	
	private JWaveCommandClassSpecification cmdClassSpec = null;	// all supported command classes, their commands, params, etc...
	private List<JWaveNode> nodeList = null;					// List with connected nodes
	private JWaveNodeFactory nodeFactory = null;				// Factory to create new nodes
	private JWaveNodeCommandFactory nodeCmdFactory = null;		// Factory to create node commands
	
	private InputStream currentInputStream = null;
	private OutputStream currentOutputStream = null;
	// logging
	/**
	 * It is possible to add any logger by implementing the interface Logger. To change the default
	 * logger, just make use of static method setLogger(Logger logger). 
	 * To use the default logger just enabling logging by doLogging(true)
	 */
	private static Logger logger = null;		
	private static boolean isLogging = false;
	
	// controller info
	private String zwChip = null;				// Information about the used chip of the z-wave controller
	private String controllerVersion = null;	// Information about the controller itself
	private int homeId;							// the current home ID used by the controller, will change with each reset
	
	// module communication
	private JWaveDatagramTransceiver transceiver = null;				// sending and receiving datagrams
	private JWaveDatagramTransceiverListener tranceiverListener = null;	// Listener to receive datagram events (e.g. sent, received, etc.)
//	private JWaveErrorHandler transceiverErrorHandler = null;				// Error handler for transceiver module
	private JWaveNodeEventListener nodeEventListener = null;			// listener to receive events from each node (e.g. goes to sleep, wakeup, etc..)
	private List<JWaveEventListener> eventListeners = null;				// registered listeners which will be informed
	private List<JWaveNodeCommunicator> nodeCommunicatorList = null;	// each node has a single communicator which transfers a command class into a datagram
	
	
	// operational parameters
	private JWaveNodeCommunicator ncToAdd = null;						// temporary communicator that was generated but not added
																		// the node will be added when interview is finished, but the communicator is needed for the interview
	private JWaveControllerMode cntrlMode;								// current controller mode (adding nodes, resetting, removing nodes, normal, etc)
	private String configFilePath = null;
	//private boolean doInterview = true;									// indicates whether the added node should be interviewed (supported cmd classes / version / ids)
	private boolean reqManufacturer = true;								// indicates whether manufacture details should be requested after inclusion
	private boolean reqVersions = true;									// indicates whether the versions of cmd classes and supported sdk version should be requested after inclusionh
	private Timer timerModeTimeOut = null;								// timer to reset the inclusion/exclusion mode back to normal if nothing happens
	private TimerTask modeTimeroutTask = null;							// the task which will be executed when mode timeout is reached
	private long modeTimeout = DEFAULT_CONTROLLER_MODE_TIMEOUT;			// timeout for controller mode in ms - 0 means disabled
	
	private boolean securityEnabled = true;
	// Configuration
	private Element configXmlElement = null;							// the node configuration will be cached, so that nodes can be configured to any time
	
	// init helpers
	private int nodeToInit = -1;										// new added node id needs to be cached temporarily
	
	/**
	 * To use a special specification file make use of constructor JWaveController(JWaveCommandClassSpecification cmdClassSpec)
	 */
	public JWaveController(){
		// if no specification was given, try to load them from default directory
		//this(loadSpecifications());
		
	}	
	
	
	/**
	 * Main constructor, need to called before JWaveController can be used properly.
	 * @param cmdClassSpec the current z-wave specification
	 */
	public JWaveController(JWaveCommandClassSpecification cmdClassSpec){
		this.cntrlMode = JWaveControllerMode.CNTRL_MODE_NOT_CONNECTED;
		this.cmdClassSpec = cmdClassSpec;
		eventListeners = new ArrayList<JWaveEventListener>();
		nodeList = new ArrayList<JWaveNode>();
		nodeCommunicatorList = new ArrayList<JWaveNodeCommunicator>();
		nodeCmdFactory = new JWaveNodeCommandFactory(cmdClassSpec);
		
		// establish eventing to transceiver		
		tranceiverListener = new JWaveDatagramTransceiverListener() {	
		
			@Override
			public void onDatagramResponse(JWaveDatagram request, JWaveDatagram response){
				evalDatagramResponse(request,response);
				
			}
			
			@Override
			public void onDatagramTransmissionFailed(int flag, JWaveDatagram datagram){
				datagramTransmissionFailed(flag, datagram);
			}
			
			@Override
			public void onDatagramTransmitted(JWaveDatagram datagram) {
				datagramTransmitted(datagram);				
			}
			
			@Override
			public void onDatagramReceived(JWaveDatagram datagram) {
				try {
					evaluateDatagram(datagram);
				} catch (Exception exc){
					JWaveController.log(LogTag.ERROR,exc.getMessage(),exc);
				}
				
			}
		};
		nodeEventListener = new JWaveNodeEventListener() {
			
			@Override
			public void onNodeEvent(JWaveNodeEvent event) {
				evaluateNodeEvent(event);
				
			}
		};
		
		// the node factory needs the current specifications for generating the nodes
		if (cmdClassSpec != null){
			nodeFactory = new JWaveNodeFactory(this.cmdClassSpec);
		}
		
		// preparing of timeout timer
		//timerModeTimeOut = new Timer();
	}
	
	
	
	public void dispose() throws JWaveException{
		if (timerModeTimeOut != null){
			timerModeTimeOut.cancel();
			timerModeTimeOut = null;
		}
		if (transceiver != null){			
			try {
				transceiver.terminate();
				transceiver = null;
			} catch (Exception exc){
				throw new JWaveException("unable to terminate transceiver",exc);
			}
		}
		for (JWaveNodeCommunicator com : this.nodeCommunicatorList){
			com.dispose();
		}
	}
	
	/**
	 * Returns a list of all known nodes. Manipulation of the returned list doesn't effect the node list of JWaveController.
	 * @return list with instances of JWaveNodes
	 */
	
	public List<JWaveNode> getNodes(){
		return new ArrayList<>(nodeList);
	}
	
	
	public JWaveDatagramTransceiver getTransceiver(){
		return transceiver;
	}
	
	/**
	 * The JWaveController is able to set the hardware controller to a special mode (eg. adding a device, removing device). If nothing happens, the modeTimeoutTimer will
	 * set the controller back to normal mode. The timeout duration can be set by using setControllerModeTimeout(long timeout)
	 */
	protected void startModeTimeoutTimer(){
		if (modeTimeout <= 0){
			return;
		}
		if (timerModeTimeOut == null){
			timerModeTimeOut = new Timer();
		}
		if (modeTimeroutTask != null){
			modeTimeroutTask.cancel();
		}
		modeTimeroutTask = new TimeOutTask();
		timerModeTimeOut.schedule(modeTimeroutTask,modeTimeout);	
		log(LogTag.INFO,"Setting controller mode timeout to "+modeTimeout +"ms");
	}
	
	/**
	 * Stops the ModeTimeoutTimer(). 
	 */
	protected void stopModeTimoutTimer(){
		log(LogTag.INFO,"Stopping the timeout timer");
		if (timerModeTimeOut == null){
			return;
		}
		if (modeTimeroutTask != null){
			modeTimeroutTask.cancel();
		}
		modeTimeroutTask = null;
	}
	
	
	/**
	 * Sets the timeout for pairing  and removing mode.
	 * @param timeout timeout in milliseconds. 0 means no timeout
	 */
	public void setControllerModeTimeout(long timeout){
		this.modeTimeout = timeout;
	}
	
	
	/**
	 * Returns the current controller mode timeout for pairing and removing nodes. 
	 * @return current timeout in milliseconds. 0 means no timeout
	 */
	public long getContollerModeTimeout(){
		return this.modeTimeout;
	}
	
	
	/**
	 * Sets controller back in normal mode. Depending on current mode, cancelInclusionMode() or cancelRemoveMode() will be called.
	 */
	public void setNormalMode(){	
		
		if ((cntrlMode == JWaveControllerMode.CNTRL_MODE_INCLUSION)||(cntrlMode == JWaveControllerMode.CNTRL_MODE_NWI)){
			cancelInclusionMode();
			return ;
		}
		if (cntrlMode == JWaveControllerMode.CNTRL_MODE_EXCLUSION){
			cancelRemoveMode();
			return;
		}	
		
	}
	
	/**
	 * Sends EXPIRED events before setting the controller mode back to normal
	 */
	protected void setExpiredNormalMode(){
		if ((cntrlMode == JWaveControllerMode.CNTRL_MODE_INCLUSION)||(cntrlMode == JWaveControllerMode.CNTRL_MODE_NWI)){
			this.dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_ADD_NODE_TO_NETWORK_EXPIRED, this));
		}
		if (cntrlMode == JWaveControllerMode.CNTRL_MODE_EXCLUSION){
			this.dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_REMOVE_NODE_FROM_NETWORK_EXPIRED, this));
		}	
		
		setNormalMode();
	}
	
	public void setSecurityEnabled(boolean enabled){
		this.securityEnabled = enabled;
	}
	
	// ++++++++++++++++++++   Setting the Controller to inclusion mode ++++++++++++++++++++++++
	
	// The inclusion mode is the z-wave specific term for add mode or learn mode. Devices/Nodes can only be added to the network, when the hardware controller was set
	// to inclusion mode before. 
	// 
	// When the controller was set to inclusion mode, the learn (LRN) or inclusion button at the devices is needed to be pressed (or device uses auto inclusion - see manual). 
	// HINT: Devices / nodes can only be added to the network when they where not added to another network before. Devices which are added to another network needed to be excluded first.
	//
	// JWaveController supports the automatic interview of each added node by default. To avoid this, use setInclusionMode(false) (not recommended)
	
	
	/**
	 * Get the controller mode. See com.domoone.JWave.JWaveControllerMode.java for further details
	 * @return current controller mode
	 */
	public JWaveControllerMode getControllerMode(){
		return this.cntrlMode;
	}
	
	public void setNetworkWideInclusionMode(){
		setNetworkWideInclusionMode(true,true);
	}
	
	public void setNetworkWideInclusionMode(boolean doInterview){
		setNetworkWideInclusionMode(doInterview,doInterview);
	}
	
	public void setNetworkWideInclusionMode(boolean reqManufacturer, boolean reqVersions){
		
		//this.doInterview = reqManufacturer || reqVersions;
		this.reqManufacturer = reqManufacturer;
		this.reqVersions = reqVersions;
			
		transceiver.send(JWaveDatagramFactory.generateAddNodeToNetworkRequest((byte)0xC1, transceiver.generateFuncId()));		
	}
	
	
	/** 
	 * Sets the hardware controller to inclusion mode. Interview will be done after by default.
	 */
	public void setInclusionMode(){
		setInclusionMode(true);
	}
	
	
	/**
	 * Sets the hardware controller to inclusion mode. 
	 * @param doInterview indicates if node will be interviewed automatically when added
	 */
	public void setInclusionMode(boolean doInterview){
		if (doInterview){
			setInclusionMode(true,true);
		} else {
			setInclusionMode(false,false);
		}
	}
	
	
	/**
	 * Sets the hardware controller to inclusion mode
	 * @param reqManufacturer indicates whether the manufacture, product type and product id should be requested automatically after inclusion
	 * @param reqVersions indicates whether versions of supported command classes should be requested after inclusion
	 */
	public void setInclusionMode(boolean reqManufacturer, boolean reqVersions){
		if (cntrlMode == JWaveControllerMode.CNTRL_MODE_EXCLUSION){
			cancelRemoveMode();
		}
		
		//this.doInterview = reqManufacturer || reqVersions;
		this.reqManufacturer = reqManufacturer;
		this.reqVersions = reqVersions;	
		transceiver.send(JWaveDatagramFactory.generateAddNodeToNetworkRequest((byte)0x81, transceiver.generateFuncId()));
		startModeTimeoutTimer();
	}
	
	
	/**
	 * Sets the hardware controller back to normal mode. 
	 */
	protected void cancelInclusionMode(){		
		stopModeTimoutTimer();
		transceiver.send(JWaveDatagramFactory.generateAddNodeToNetworkRequest((byte)0x05, transceiver.generateFuncId()));
		
	}
	
	// ++++++++++++++++++++ Setting the Controller to Remove Node Mode +++++++++++++++++++++++++++++++++
	
	
	/**
	 * Sets the hardware controller to exclusion mode. 
	 */
	public void setExlusionMode(){
		if (cntrlMode == JWaveControllerMode.CNTRL_MODE_INCLUSION){
			cancelInclusionMode();
		}		
		transceiver.send(JWaveDatagramFactory.generateRemoveNodeFromNetworkRequest((byte)0x01, transceiver.generateFuncId()));
		startModeTimeoutTimer();
	}
	
	
	/**
	 * Sets the controller from exclusion back to normal mode. 
	 */
	public void cancelRemoveMode(){		
		transceiver.send(JWaveDatagramFactory.generateRemoveNodeFromNetworkRequest((byte)0x05, transceiver.generateFuncId()));
		stopModeTimoutTimer();
	}
	
	
	
	
	/**
	 * Controller hardware reset. All node IDs will be deleted and new home ID will be generated. 
	 */
	public void resetController(){	
		transceiver.send(JWaveDatagramFactory.generateResetControllerCmd());
		
		//dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_CONTROLLER_RESET,this));
	}
	
	

	/**
	 * Removes the node from internal cache and dispatches related node event. 
	 * 
	 * HINT: this method does not remove physical node from z-wave network. To do so, set controller to exclusion mode and press LRN-Button on 
	 * the device you want to remove. 
	 * @param node
	 */
	protected void removeNode(JWaveNode node){		
		nodeList.remove(node);
		dispatchEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_NODE_REMOVED,node));
		
	
	}
	
	
	
	
	public synchronized void addCntrlListener(JWaveEventListener listener){
		if (eventListeners.contains(listener)) return;		
		eventListeners.add(listener);
	}
	

	
	public synchronized void removeCntrlListener(JWaveEventListener listener){
		if (eventListeners.isEmpty()) return;
		eventListeners.remove(listener);
	}
	
	
	protected void addNodeCommunicator(JWaveNodeCommunicator nodeCommunicator){
		addNCToList(nodeCommunicator);		
		dispatchEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_NODE_ADDED,nodeCommunicator.getNode()));
		
		//dispatchEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_INTERVIEW_STARTED,nodeCommunicator.getNode()));
		
		try {
			nodeCommunicator.startInterview(new JWaveNodeInterviewOptions(reqManufacturer, reqVersions));
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
	}

	
	protected void addNCToList(JWaveNodeCommunicator nodeCommunicator){
		this.nodeList.add(nodeCommunicator.getNode());		
		nodeCommunicatorList.add(nodeCommunicator);
		nodeCommunicator.getNode().addEventListener(nodeEventListener);
	}
	
	public JWaveCommandClassSpecification getDefinitions(){
		return this.cmdClassSpec;
	}

	
	public void setConfigurationFilePath(String configurationFilePath){
		this.configFilePath = configurationFilePath;		
	}
	
	public void loadConfiguration() throws IOException{
		if (this.configFilePath == null){
			throw new IOException("No configuration file given!");
		}
		loadConfiguration(this.configFilePath);
	}
	
	public void saveConfiguration() throws IOException{
		if (this.configFilePath == null){
			throw new IOException("No configuration file given!");
		}
		saveConfiguration(this.configFilePath);
	}
	

	public Element getConfiguration(Document doc){
		Element root = doc.createElement("JWave-configuration");
		root.setAttribute("homeId",this.getHomeId());
		for (JWaveNode node : this.nodeList){
			root.appendChild(this.nodeFactory.generateNodeConfigurationElement(doc,node));
		}		
		return root;
	}
	
	public void saveConfiguration(String filePath) throws IOException{
		Document doc = XmlUtils.createDocument();		
		doc.appendChild(getConfiguration(doc));
		
		File file = new File(filePath);		
		try {
			XmlUtils.saveXml(file, doc);
		} catch (Exception exc){
			throw new IOException("Unable to save Configuration. "+exc.getMessage(),exc);
		}
	}
	
	public void setConfiguration(Element elem) throws JWaveException{
		this.configXmlElement = elem;
        if ((this.cntrlMode != JWaveControllerMode.CNTRL_MODE_INITIALIZING) && (this.cntrlMode != JWaveControllerMode.CNTRL_MODE_NOT_CONNECTED)){
			setConfiguration();
		}		
	}
	
	protected void setConfiguration() throws JWaveException{
		if (configXmlElement == null){
			return;
		}
		log(LogTag.INFO,"Configuring Z-Wave Nodes");
		NodeList nodeList = configXmlElement.getChildNodes();
		Element tmp;
		for (int i = 0; i< nodeList.getLength(); i++){
			if (nodeList.item(i) instanceof Element){
				tmp = (Element)nodeList.item(i);
				if (!tmp.hasAttribute("id")){
					throw new JWaveException("Corrupted configuration data. Excpected attribute 'id' for element node!");
				}
				nodeFactory.init(this.getNode(Integer.parseInt(tmp.getAttribute("id"))), tmp);
			}
		}
		
	}
	
	
	public void loadConfiguration(String filePath) throws IOException{		
		Document doc = XmlUtils.loadXml(new File(filePath));
		
		Element root = doc.getDocumentElement();
		if (!(root.getTagName().equalsIgnoreCase("JWave-configuration"))){
			throw new IOException("File does not contain JWave configuration data!");			
		}
		
		try {
			setConfiguration(root);
		} catch (Exception exc){
			throw new IOException("Unable to load configuration File",exc);
		}
		
	}
	
	
	private byte[] getNetworkKey(){
		byte[] bytes = {0x00,0x11,0x22,0x33,0x44,0x55,0x66,0x77,(byte)0x88,(byte)0x99,(byte)0xaa,(byte)0xbb,(byte)0xcc,(byte)0xdd,(byte)0xee,(byte)0xff};
		return bytes;
	}
	
	public void deleteConfiguration() throws IOException {
		if (this.configFilePath == null){
			throw new IOException("No configuration file given!");
		}
		deleteConfiguration(this.configFilePath);
	}
	
	public void deleteConfiguration(String filePath) throws IOException {
		new File(filePath).delete();
	}
	
	public void init(JWaveConnection connection){
	
		init(connection.getInputStream(), connection.getOutputStream());
	}
	
	public void init(InputStream inputStream, OutputStream outputStream){
		this.currentInputStream = inputStream;
		this.currentOutputStream = outputStream;
		this.cntrlMode = JWaveControllerMode.CNTRL_MODE_INITIALIZING;
		dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_INIT_STARTED, this));
		createTransceiver(inputStream, outputStream);		
		sendInitCmdsToController();		
	}
	
	
	
	public void reInit(){
		
		try {
			this.dispose();
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR,exc.getMessage(),exc);
			// FIXME: handle exception
		}
		cntrlMode = JWaveControllerMode.CNTRL_MODE_INITIALIZING;
		nodeList = new ArrayList<JWaveNode>();
		nodeCommunicatorList = new ArrayList<JWaveNodeCommunicator>();
		
		
		this.ncToAdd = null;
		this.nodeToInit=-1;
		this.zwChip="";
		if (transceiver == null){			
			createTransceiver(currentInputStream,currentOutputStream);			
		}	
		sendInitCmdsToController();
	}              
	
	public boolean isInitialized(){
		switch (cntrlMode){
			case CNTRL_MODE_ERROR:
			case CNTRL_MODE_NOT_CONNECTED:
			case CNTRL_MODE_INITIALIZING:
				return false;
			
			default:
				return true;
		}
		
	}
	
	protected void createTransceiver(InputStream inputStream, OutputStream outputStream){
		transceiver = new JWaveDatagramTransceiver(inputStream, outputStream, tranceiverListener);		
		transceiver.setErrorHandler(new JWaveErrorHandler() {
			
			@Override
			public void onError(String message, Throwable throwable) {
				handleTransceiverError(message, throwable);
				
			}
		});
	}
	
	protected void sendInitCmdsToController(){
		transceiver.send(JWaveDatagramFactory.generateGetVersionRequest());
		transceiver.send(JWaveDatagramFactory.generateGetSerialApiCapabilitiesRequest());
		transceiver.send(JWaveDatagramFactory.generateGetHomeIdRequest());
		transceiver.send(JWaveDatagramFactory.generateSerialInitDataRequest());
	}
	
	
	
	public void setNodeCommandClass(JWaveNode node, int commandClassKey, int commandClassVersion){
		if (nodeFactory != null){
			nodeFactory.setCommandClassValue(node, commandClassKey,commandClassVersion);
		}
	}
	
	
	
	public JWaveNode getNode(int nodeId){
		for (JWaveNode node : nodeList){
			if (node.getNodeId() == nodeId){
				return node;
			}
		}
		return null;
	}
	
	public String getZWaveChipVersion(){
		return zwChip;
	}
	
	public String getHomeId() {
		String res = "";
		byte[] bytes = ByteBuffer.allocate(4).putInt(homeId).array();
		
		for (int i= 0; i<bytes.length; i++){
			res = res + Integer.toHexString(bytes[i]&0xFF);
		}
	
		return res;
	}
	
	public boolean isHomeIdValid() {
		return ((homeId != 0x00000000) && (homeId != 0xFFFFFFFF) && (homeId != -1));
	}
	
	public String getControllerVersion(){
		return this.controllerVersion;
	}
	
	
	public void broadcastControllerNodeInfo(){
		JWaveDatagram datagram = JWaveDatagramFactory.generateSendNodeInfo(0xff);
		transceiver.send(datagram);
	}
	
	protected void sendSecure(JWaveNode node, JWaveNodeCommand cmd){
		//
	}
	
	protected void send(JWaveNode node, JWaveNodeCommand cmd){
		JWaveDatagram datagram = JWaveDatagramFactory.generateSendDataDatagram(node, cmd, (byte)(node.getNodeId()&0xFF));		
		transceiver.send(datagram);	
	}
	
	protected void datagramTransmitted(JWaveDatagram datagram){
		dispatchEvent(new JWaveDatagramEvent(JWaveEventType.DATA_EVENT_DATAGRAM_TRANSMITTED, datagram));
	}
	

	protected void datagramTransmissionFailed(int flag, JWaveDatagram datagram){
		if (datagram.getCommandType() == JWaveCommandType.CMD_JWave_SEND_DATA){
			byte[] payload = datagram.getPayload();
			int nodeId = payload[0];
			JWaveNodeCommunicator comm = getNodeCommunicator(nodeId);
			if (comm != null){
				comm.onTransmissionFailed();
			}
		}
		
		//
	}
	
	protected void handleTransceiverError(String message, Throwable throwable){
		JWaveErrorEvent errorEvent = new JWaveErrorEvent(JWaveEventType.ERROR_IO_CONNECTION,message,throwable);
		this.dispatchEvent(errorEvent);
	}
	
	protected void evaluateNodeEvent(JWaveNodeEvent event){
		
		dispatchEvent(event);
	}
	
	protected void evalDatagramResponse(JWaveDatagram request, JWaveDatagram response){
		if (request instanceof JWaveNodeCommandDatagram){
			//JWaveNodeCommand nodeCmd = ((JWaveNodeCommandDatagram)request).getNodeCmd();
			try {
				//nodeCmd.setResponse(generateNodeCmd(response));
				dispatchEvent(new JWaveDatagramEvent(JWaveEventType.DATA_EVENT_NODE_CMD_DELIVERED, request));
			} catch (Exception exc){
				JWaveController.log(LogTag.ERROR,exc.getMessage(),exc);
			}
			
		}
		
	}
	
	
	protected synchronized void evaluateDatagram(JWaveDatagram datagram) throws JWaveException{
		dispatchEvent(new JWaveDatagramEvent(JWaveEventType.DATA_EVENT_DATAGRAM_RECEIVED, datagram));
		
		switch (datagram.getCommandType()){
			case CMD_JWave_GET_VERSION:
					setVersion(datagram);				
				break;
			case CMD_SERIAL_GET_INIT_DATA:
					evaluateInitData(datagram);
				break;
			case CMD_MEMORY_GET_ID:
					setHomeId(datagram);									
				break;
			case CMD_JWave_GET_NODE_PROTOCOL_INFO:
					evalGetNodeProtocolInfo(datagram);
				break;
			case CMD_APPL_CONTROLLER_UPDATE:
					updateController(datagram);
				break;
			case CMD_SERIAL_GET_CAPABILITIES:
					setSerialApiCapabilities(datagram);
				break;
			case CMD_JWave_ADD_NODE_TO_NETWORK:
					evalAddNodeToNetworkDatagram(datagram);
					break;
			case CMD_JWave_REMOVE_NODE_FROM_NETWORK:
					evalRemoveNodeFromNetworkDatagram(datagram);
					break;
			case CMD_APPL_COMMAND_HANDLER:
					evalCmdHandlerDatagram(datagram);
				break;
			case CMD_JWave_SEND_DATA:
					evalCmdSendDataDatagram(datagram);
				break;
			case CMD_JWave_SET_DEFAULT:
					handleControllerReset(datagram);
				break;
			default:
			
				break;
		}
		
	}
	
	protected synchronized void evalCmdHandlerDatagram(JWaveDatagram datagram){
		
		
		
		if (cntrlMode == JWaveControllerMode.CNTRL_MODE_INITIALIZING){
			// not all nodes are initialized at this point. ignoring command handlers.
			return;
		}
		byte[] payload = datagram.getPayload();
		int sourceNode = payload[1];
		if (payload[2] > 0){
			JWaveNode node = this.getNode(sourceNode);
			
			try {
				JWaveNodeCommand cmd = nodeCmdFactory.generateNodeCmd(datagram);
				this.nodeFactory.assignNodeCmd(node, cmd);
			} catch (Exception exc){
				JWaveController.log(LogTag.ERROR,exc.getMessage(),exc);
			}
			
		}
	}
	
	/*protected JWaveNodeCommand getNodeCmd(JWaveDatagram datagram) throws JWaveException{
		byte[] payload = datagram.getPayload();		
		if (payload.length > 1){
			int sourceNode = payload[0];
			if (payload[1] > 0){
				//JWaveNode node = this.getNode(sourceNode);
				byte[] cmdData = new byte[payload[1]];
				for (int i = 0; i< payload[1]; i++){
					cmdData[i] = payload[i+2];
				}
			
				return generateNodeCmd(cmdData);
					
			
			}
		}
		return null;		
	}*/
	
	protected void evalCmdSendDataDatagram(JWaveDatagram datagram){		
		
		
		byte[] payload = datagram.getPayload();		
		if (payload.length > 1){		
							
				try {
					int id = payload[0];
					JWaveNodeCommunicator comm = getNodeCommunicator(id);
					if (comm != null){
						comm.onTransmissionSuccess();
					}
				} catch (Exception exc){
					JWaveController.log(LogTag.ERROR,exc.getMessage(),exc);
				}
			
		}

		
	}
	
	protected synchronized JWaveNodeCommunicator getNodeCommunicator(int id){
		for (JWaveNodeCommunicator comm : this.nodeCommunicatorList){
			if (comm.getNode().getNodeId() == id){
				return comm;
			}
		}
		return null;
		
	}
	
		
	protected void evalGetNodeProtocolInfo(JWaveDatagram datagram){
		/*if (isLearnMode){
			if (ncToAdd != null){
			   
				
				nodeFactory.setNodeCapabilities(ncToAdd.getNode(), datagram.getPayload()[0]&0xFF);	
				transceiver.send(JWaveDatagramFactory.generateAddNodeToNetworkRequest((byte)0x05,transceiver.generateFuncId()));
			}
			return;
		}*/
		if (cntrlMode == JWaveControllerMode.CNTRL_MODE_INITIALIZING){
			if (nodeList != null){
				JWaveNode node = nodeList.get(nodeToInit);
				byte[] payload = datagram.getPayload();
				
				nodeFactory.setNodeDeviceType(node,payload[3]&0xFF, payload[4]&0xFF, payload[5]&0xFF);
				nodeFactory.setNodeCapabilities(node, payload[0]&0xFF);			
				nodeFactory.setNodeSecurity(node, payload[1]&0xFF);		
				
				if (nodeToInit < nodeList.size()-1){
					nodeToInit++;					
					node = nodeList.get(nodeToInit);					
					transceiver.send(JWaveDatagramFactory.generateGetNodeProtocolInfoRequest(node.getNodeId()));
					
				} else {
					cntrlMode = JWaveControllerMode.CNTRL_MODE_NORMAL;
					
					try {
						setConfiguration();
					} catch (Exception exc){
						JWaveController.log(LogTag.ERROR,exc.getMessage(),exc);
					}
					cntrlMode = JWaveControllerMode.CNTRL_MODE_NORMAL;
					dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_INIT_COMPLETED, this));
					
				}
			}
		}
		
	}
	
	protected void setSerialApiCapabilities(JWaveDatagram datagram){
		 /*ret.Append(string.Format("AppVersion={0}; ", Tools.ToHexString(payload[0])));
         ret.Append(string.Format("AppRevision={0}; ", Tools.ToHexString(payload[1])));
         ret.Append(string.Format("ManufacturerId={0} {1}; ", Tools.ToHexString(payload[2]), Tools.ToHexString(payload[3])));
         ret.Append(string.Format("ManufacturerProductType={0} {1}; ", Tools.ToHexString(payload[4]), Tools.ToHexString(payload[5])));
         ret.Append(string.Format("ManufacturerProductId={0} {1}; ", Tools.ToHexString(payload[6]), Tools.ToHexString(payload[7])));
         byte funcIdx = 0;
         ret.Append(string.Format("SupportedFuncIds="));
         for (int n = 0; n < payload.Length - 8; n++)
         {
             byte availabilityMask = payload[n + 8];
             for (byte bit = 0; bit < 8; bit++)
             {
                 funcIdx++;
                 if ((availabilityMask & (1 << bit)) > 0)
                 {
                     ret.Append(string.Format("{0} ", Tools.ToHexString(funcIdx)));
                 }
             }
         }
         ret.Append(string.Format("; "));
         */
    
	}
	
	protected void evalRemoveNodeFromNetworkDatagram(JWaveDatagram datagram){
		byte[] payload = datagram.getPayload();
		//CmdJWaveRemoveNodeFromNetwork	funcID=02; bStatus=06; bSource=21; basic=04; generic=21; specific=01; commandclasses=31 60 86 72 85 84 80 70 20 ; 	02 06 21 0C 04 21 01 31 60 86 72 85 84 80 70 20
		
		
		if (payload.length == 4){
			switch (payload[1]){
			case 01: //remove mode
			case 07: //remove mode if controller was already in remove mode)			
				cntrlMode = JWaveControllerMode.CNTRL_MODE_EXCLUSION;
				dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_REMOVE_NODE_FROM_NETWORK_START, this));
				break;
			case 06:  				
				cntrlMode = JWaveControllerMode.CNTRL_MODE_NORMAL;
				dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_REMOVE_NODE_FROM_NETWORK_CANCELED, this));
				break;
			}
			
		}
		
		if (payload.length > 4){
			int id = payload[2]&0xFF;
			byte status = payload[1];
			if (status == 0x03){
				// prepare node for remove
			}
			
			if (status == 0x06){ // remove done
				JWaveNode node = getNode(id);
				if (node != null){
					removeNode(node);					
				}
				
				stopModeTimoutTimer();
				//transceiver.send(JWaveDatagramFactory.generateRemoveNodeFromNetworkRequest((byte)0x05, transceiver.generateFuncId()));
				//transceiver.send(JWaveDatagramFactory.generateRemoveNodeFromNetworkCompletedMessage());
				cntrlMode = JWaveControllerMode.CNTRL_MODE_NORMAL;		
				dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_REMOVE_NODE_FROM_NETWORK_SUCCESS, this));
				
			}
		}
	}
	
	protected void evalAddNodeToNetworkDatagram(JWaveDatagram datagram) throws JWaveException{
		
		byte[] payload = datagram.getPayload();		
		// setting the controller mode if needed
		if (payload.length >= 4){
			switch (payload[1]){
				case 0x01:	// Add mode
				case 0x07:  // Add mode (if controller was already in add mode
					if (cntrlMode != JWaveControllerMode.CNTRL_MODE_INCLUSION){
						cntrlMode = JWaveControllerMode.CNTRL_MODE_INCLUSION;					
						dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_ADD_NODE_TO_NETWORK_START, this));
					}
					break;
				case 0x06:  // add mode canceled 
					if (cntrlMode != JWaveControllerMode.CNTRL_MODE_NORMAL){
						cntrlMode = JWaveControllerMode.CNTRL_MODE_NORMAL;					
						dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_ADD_NODE_TO_NETWORK_CANCELED, this));
					}				
					break;	
				case 0x05: // finished to add device
					if (cntrlMode != JWaveControllerMode.CNTRL_MODE_NORMAL){
						stopModeTimoutTimer();
						transceiver.send(JWaveDatagramFactory.generateAddNodeToNetworkRequest((byte)0x05, transceiver.generateFuncId()));
						cntrlMode = JWaveControllerMode.CNTRL_MODE_NORMAL;
						if (this.ncToAdd != null){	
							dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_ADD_NODE_TO_NETWORK_SUCCESS, this));
							addNodeCommunicator(ncToAdd);	
							ncToAdd = null;
						} else {
							dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_ADD_NODE_TO_NETWORK_FAILED, this));
						}
					}
				break;			
			}
		}
		
		
		if (payload.length > 4){
			// if payload is that long, it includes information about the new added node
				 List<JWaveCommandClass> cmdClassList = new ArrayList<JWaveCommandClass>();
	             JWaveCommandClass tmp;
	             for (int i = 0; i < payload[3] - 3; i++) {            	 
	                 tmp = this.cmdClassSpec.getCommandClass((payload[7 + i]&0xFF));
	                 if (tmp != null){
	                	// Some nodes are buggy (e.g. fibaro wall plug) and implementing cmd classes twice
	                	 // avoid double cmd classes to pretend unnecessary traffic due to version request during interview
	                	 if (!cmdClassList.contains(tmp)){ 
	                		 cmdClassList.add(tmp);
	                	 }
	                 }
	             }	           
	             int id =  payload[2]&0xFF;
	             if (isNodeAlreadyExisting(id)){
	            	 JWaveController.log(LogTag.WARN, "There exists a node with id 2 already. Node will not be added again");
	            	 JWaveNode node = getNode(id);
	            	 dispatchEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_NODE_ALREADY_ADDED, node));
	             } else {
	            	 JWaveNode nodeToAdd = nodeFactory.createNode(cmdClassList,id, payload[1]&0xFF,payload[4]&0xFF,payload[5]&0xFF, payload[6]&0xFF);
	            	 ncToAdd = new JWaveNodeCommunicator(nodeToAdd, transceiver,this.cmdClassSpec,getNetworkKey());
	            	 ncToAdd.setSecurityEnabled(securityEnabled);
	             }
		}		
	}
	
	protected boolean isNodeAlreadyExisting(int id){
		synchronized(nodeList) {
			for (JWaveNode node : this.nodeList){
				if (node.getNodeId() == id){
					return true;
				}
			}
		}
		return false;
	}
	
	protected void updateController(JWaveDatagram datagram){
		byte[] payload = datagram.getPayload();
		if (payload.length > 4) {            
             
			 JWaveNode node = getNode(payload[1]&0xFF);
			 if (node == null){
				 // FIXME: handle that!
				 return;
			 }
			 
             List<JWaveCommandClass> cmdClassList = new ArrayList<JWaveCommandClass>();
             JWaveCommandClass tmp;
             for (int i = 0; i < payload[2] - 3; i++) {            	 
                 tmp = this.cmdClassSpec.getCommandClass((payload[6 + i]&0xFF));
                 if (tmp != null){
                	 cmdClassList.add(tmp);
                 }
             }
             
             nodeFactory.configNode(node, cmdClassList, payload[0]&0xFF,payload[3]&0xFF,payload[4]&0xFF, payload[5]&0xFF);
             dispatchEvent(new JWaveNodeEvent(JWaveEventType.NODE_EVENT_NIF_RECEIVED, node));
         } else {
             //ret.Append(string.Format("funcID={0}; ", Tools.ToHexString(payload[0])));
         }
	}
	
	
	protected void evaluateInitData(JWaveDatagram datagram){
		byte[] payload = datagram.getPayload();		
		
		//System.out.println("version = "+payload[0]);
		//System.out.println("Capabilities = "+payload[1]);
		
		
		
		// Setting Z-Wave Chip version
		
		String tmp = Integer.toString(payload[3 + payload[2]]&0xFF, 16);
		if (tmp.length() < 2){
			tmp = "0"+tmp;
		}	
		
		zwChip = "zw"+tmp;
		tmp = Integer.toString(payload[4 + payload[2]]&0xFF, 16);
		if (tmp.length() < 2){
			tmp = "0"+tmp;
		}
		zwChip = zwChip+tmp;	
		
		if (getControllerMode() != JWaveControllerMode.CNTRL_MODE_INITIALIZING){
			log(LogTag.WARN, "Controller mode is "+ getControllerMode().name() +". Skipping node initialisation.");
			return;
		}
		
		JWaveNode tmpNode;
		
		// setting up the node list		
		if (payload[2] > 0){		   
		   int nodeIdx = 0;
           for (int i = 0; i < payload[2]; i++){
              byte availabilityMask = payload[3 + i];
              for (byte bit = 0; bit < 8; bit++){
                 nodeIdx++;
                 if ((availabilityMask & (1 << bit)) > 0) {
                	 tmpNode = nodeFactory.createNode(nodeIdx);                	 
                	 addNCToList(new JWaveNodeCommunicator(tmpNode, transceiver,this.cmdClassSpec,getNetworkKey()));   	                 	 
                 }
              }
           }
             
         }
		
		if (nodeList != null){
			if (!nodeList.isEmpty()){
				JWaveNode node = nodeList.get(0);			
				this.nodeToInit = 0;
				transceiver.send(JWaveDatagramFactory.generateGetNodeProtocolInfoRequest(node.getNodeId()));
			} else {
				// FIXME: empty nodeList sucks -> something went wrong due to controller initializing 
			}
		}	
		
	}
	
	protected void setVersion(JWaveDatagram datagram){		
		char[] data = new char[11];
		for (int i = 0; i< 11; i++){
			data[i] = (char)datagram.toByteArray()[i+4];
		}
		this.controllerVersion = String.valueOf(data);		
	}
	
	protected void setHomeId(JWaveDatagram datagram){
		if (datagram.getCommandType() == JWaveCommandType.CMD_MEMORY_GET_ID){
			homeId = ByteBuffer.wrap(datagram.toByteArray(), 4, 4).getInt();			
		}
		// if controller was flashed and EEPROM is empty, the controller won't be ready to work until it will be reset again. 
		// the only indicator of this state is a non valid homeId (0x00000000 or 0xFFFFFFFF)
		if (!isHomeIdValid()){
			String errorMsg = "Invalid home id (0x"+getHomeId()+"). Reset controller to generate a new one.";
			this.cntrlMode = JWaveControllerMode.CNTRL_MODE_ERROR;
			log(LogTag.WARN, errorMsg);		
			errorMsg += "This could happen after reflashing the Z-Wave chip on the controller.";
			this.dispatchEvent(new JWaveErrorEvent(JWaveEventType.ERROR_CONTROLLER, errorMsg,new JWaveException(errorMsg)));
		}
	}
	
	protected boolean hasConfigurationFile(){
		return configFilePath != null;
	}
	
	
	protected void handleControllerReset(JWaveDatagram datagram){		
		this.nodeList = new ArrayList<JWaveNode>();			
		configXmlElement = null;
		nodeToInit = -1;
		try {
			if (hasConfigurationFile()) {
				deleteConfiguration();
			}
		} catch (Exception exc){
			log(LogTag.ERROR,"Unable to delete configuration file.",exc);
		}		
		reInit();
		dispatchEvent(new JWaveControlEvent(JWaveEventType.CNTRL_EVENT_CONTROLLER_RESET, this));
	}
	
	private void dispatchEvent(final JWaveEvent event){		
		
		if (eventListeners.isEmpty()) return;
		
		for (JWaveEventListener l : eventListeners){
			final JWaveEventListener tmpListener = l;
			// HINT: to many listeners could result in to many threads
			Thread t = new Thread() {
				
				@Override
				public void run() {					
					tmpListener.onJWaveEvent(event);					
				}
			};
			t.start();			
		}		
	}
		
	
	
	
	//  Static Methods
	public static String getVersion(){
		return VERSION;
	}
	
	
	public JWaveCommandClassSpecification getCommandClassSpecifications(){
		return this.cmdClassSpec;
	}
	
	public static JWaveCommandClassSpecification loadCmdClassSpecifications(String path) throws IOException,XmlConvertionException{
		return new JWaveCommandClassSpecification(path);
	}
	
	protected static JWaveCommandClassSpecification loadSpecifications(){
		InputStream is = ClassLoader.getSystemResourceAsStream(FILE_COMMAND_CLASS_SPECIFICATIONS);
		if (is == null){			
			is = ClassLoader.getSystemResourceAsStream(FILE_COMMAND_CLASS_SPECIFICATIONS);
			if (is == null){
				return null;
			}
		}
		int read;
		byte[] buffer = new byte[1014];
		StringBuffer strBuf = new StringBuffer();
		try {
			while ((read = is.read(buffer)) != -1){
				strBuf.append(new String(buffer,0,read));
			}
			Document doc = XmlUtils.parseDoc(strBuf.toString());
			if (doc == null){
				
				return null;
			}
			return new JWaveCommandClassSpecification(doc);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR,exc.getMessage(),exc);
		}
		return null;
	}	
	
	
	
	// ++++++++++++++++++++++++++++  Logging +++++++++++++++++++++++++++++++++++++++++++
	
	public static void doLogging(boolean doLog){
		isLogging = doLog;
	}
	
	public static void setLogger(Logger newLogger){
		logger = newLogger;
	}
	
	public static void log(LogTag tag, String message){
		log(tag,message,null);
	}
	
	public static void log(LogTag tag, String message,Throwable throwable){
		if (logger == null){
			if (!isLogging){
				return;
			}
			logger = new LoggerConsolePrinter("JWave "+ JWaveController.getVersion());
		}
		
		logger.log(tag, message, throwable);
	}
	
	
   // ++++++++++++++++++++++++++++++  TimerTask ++++++++++++++++++++++++++++++++++++++++ 
	protected class TimeOutTask extends TimerTask{
		public void run(){
			log(LogTag.INFO,"TIMEOUT | Controller will be set to normal mode.");
			setExpiredNormalMode();			
		}
	}
	
	
	
	
}
