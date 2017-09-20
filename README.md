                                           ____.__      __                                .__  ._____.                                                          
            ______   ______   ______      |    /  \    /  \_____ ___  __ ____             |  | |__\_ |______________ _______ ___.__.   ______   ______   ______ 
           /_____/  /_____/  /_____/      |    \   \/\/   /\__  \\  \/ // __ \    ______  |  | |  || __ \_  __ \__  \\_  __ <   |  |  /_____/  /_____/  /_____/ 
           /_____/  /_____/  /_____/  /\__|    |\        /  / __ \\   /\  ___/   /_____/  |  |_|  || \_\ \  | \// __ \|  | \/\___  |  /_____/  /_____/  /_____/ 
                                      \________| \__/\  /  (____  /\_/  \___  >           |____/__||___  /__|  (____  /__|   / ____|                            
                                                      \/        \/          \/                         \/           \/       \/         

    ===================================================================================================================================================================                                                
          
# Content

* How to build
* How to use
  * Initialization
  * Adding a node/device
  * Sending commands
  * Receiving commands


# How to build

## Prequisites

* Java 1.8 or higher
* Maven
* Z-Wave controller

## Build with Maven

    mvn clean install

# How to use

## Initialization

The Z-Wave communications between Z-Wave nodes is based on Command Classes which define the way of data exchange. All Command Classes are stored in 
a huge specification file based on XML. Within the library, no command class is hardly coded. Thus, during the creation of the Z-Wave Controller, the Z-Wave Specification need to be passed.

	
		JWaveCommandClassSpecification spec = spec = new JWaveCommandClassSpecification(path);

		JWaveController controller = new JWaveController(spec);


The dataflow whithin the library is event driven. To react on events you need to add a Listener to the Z-Wave controller.


	controller.addCntrlListener(new JWaveEventListener() {			
		@Override
		public void onJWaveEvent(JWaveEvent event) {
			handleJWaveEvent(event);				
		}
	});



	protected void handleJWaveEvent(JWaveEvent event){
		switch (event.getEventType()){
			case ERROR_IO_CONNECTION:
				if (event instanceof JWaveErrorEvent){
					JWaveErrorEvent errEvnt = (JWaveErrorEvent)event;
					handleIOError(errEvnt.getMessage(),errEvnt.getThrowable());
				}
				break;
			case CNTRL_EVENT_INIT_COMPLETED:
				// ready to use
				break;
			case NODE_EVENT_INTERVIEW_FINISHED:
				JWaveNode node = null;
				if (event instanceof JWaveNodeEvent){
					JWaveNodeEvent nev = (JWaveNodeEvent)event;
					node = nev.getNode();
					...
				}
				break;
			default:
				break;
		}
	}


The JWave controller is ready for initialization now. Since the physical Z-Wave controller could be USB, UART, IP or even emulated, the initialization
procedure is reduced to passing the in- and output stream for communication. Thus, opening a connection to the physical Z-Wave controller is not part of
this library and need to be developed addioionally. Afterwards, the initialization looks like that:

	
	controller.init(<your connection>.getInputStream(),<your connection>.getOutputStream());



## Adding a node/device


	controller.setInclusionMode() 

As written bevore, the communication with the Z-Wave controller is event driven. Every action results in an event, received by the ZWaveEventListener added before. 
Calling cntrl.setInclusionMode() makes not sure that the physical controller is in inclusion mode. 
The controller is ready to add a new device when the event of eventtype CNTRL_EVENT_ADD_NODE_TO_NETWORK_START was received.

When a new device was added, information about manufacture and versions of the supported command classes is missing. The z-wave controller starts the interview after the successfull adding procedure per default. To prevent this, make use of on of these parameters whereas both params are of type boolean

	controller.setInclusionMode(askForManufacture, interviewCommandClassVersion) 

The node (new device) is ready for use, when the event with the eventType NODE_EVENT_INTERVIEW_FINISHED was received. 


## Sending commands


Assumed a device supporting the command class switch binary was added, turning the device on will look like this.

	JWaveNodeCmd nCmd = JWaveCmdClassFactory.generateCmd_SwitchBinary_Set(0xFF);

	node.sendCmd(nCmd);


Depending on the Command Class specification that was loaded at the beginning, the JWaveCmdClassFactory may not be able to generate a new or unknown command. In this case you need to build the JwaveNodeCmd by yourself:

    JWaveCommand cmd = spec.getCommand("COMMAND_CLASS_SWITCH_BINARY",1,"SWITCH_BINARY_SET");
	JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
	try {
		nodeCmd.setParamValue(0, 0xFF);
	} catch (Exception exc) {
		JWaveCntrl.log(LogTag.ERROR, exc.getMessage(),exc); 
	}


## Receiving Commands


Add a JWaveNodeEventListener to the node for handling received commands.



	node.addEventListener(new JWaveNodeEventListener() {	
	    @Override
	    public void onNodeEvent(JWaveNodeEvent event) {
	    	switch (event.getEventType()){				
	    		case NODE_EVENT_DATA_RECEIVED:
		    		if (event instanceof JWaveNodeDataEvent){
			    		JWaveNodeCommand cmd = ((JWaveNodeDataEvent)event).getNodeCmd();						
				    	evaluateReceivedNodeCmd(cmd);
    				}
	    			break;
		    	default:
			    	break;
		    }			
	    }
    });			
		
	
