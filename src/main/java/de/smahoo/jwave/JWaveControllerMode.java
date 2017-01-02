package de.smahoo.jwave;

/**
 * The controller that hosts the Z-Wave network can have different modes. Thus, JWaveController has to represent these modes
 * as well as additional operating ones.
 *
 * @author Mathias Runge (mathias.runge@smahoo.de)
 */
public enum JWaveControllerMode {
	CNTRL_MODE_NOT_CONNECTED,	// no connection to the physical Z-Wave controller
	CNTRL_MODE_INITIALIZING,	// initialization of JWaveController ongoing
	CNTRL_MODE_ERROR,			// something went wrong
	CNTRL_MODE_NORMAL,			// no exclusion, no inclusion, simply ready to run
	CNTRL_MODE_INCLUSION,		// physical controller is awaiting a node to be added
	CNTRL_MODE_NWI,				// physical controller is awaiting a node to be added anywhere in the network
	CNTRL_MODE_EXCLUSION		// physical controller is awaiting a node to be removed
}
