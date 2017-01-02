package de.smahoo.jwave.node;

public class JWaveNodeInterviewOptions {
	boolean requestManufactureDetails;
	boolean requestCmdClassVersions;
	
	
	public JWaveNodeInterviewOptions(boolean requestManufactureDetails,boolean requestCmdClassVersions){
		this.requestCmdClassVersions = requestCmdClassVersions;
	
		this.requestManufactureDetails = requestManufactureDetails;
	}

	public boolean isRequestManufactureDetails() {
		return requestManufactureDetails;
	}

	public boolean isRequestCmdClassVersions() {
		return requestCmdClassVersions;
	}

	
	public boolean isNoInterview(){
		return (requestCmdClassVersions == false) && (requestManufactureDetails == false);
	}
	
}