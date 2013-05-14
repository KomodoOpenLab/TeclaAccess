package ca.idi.tekla.util;

import ca.idi.tecla.framework.TeclaStatic;
import ca.idi.tekla.TeclaApp;

public class EmergencyCallout {
	
	public void Callout() {
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency switch event detected: "+ emergency_email_address());
		
	}
	
	
	// called from init of TeclaApp, probably deleted before RC version 
	public void test() {
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency test proc GPS Setting: " + emergency_GPS_setting());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency test proc Phone number: " + emergency_phone_number());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency test proc SMS number: " + emergency_SMS_number());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency test proc Email address: "+ emergency_email_address());
	}
	
	public boolean emergency_GPS_setting() {
		return TeclaApp.persistence.getEmergencyGPSSetting();
	}
	public String emergency_phone_number() {
		return TeclaApp.persistence.getEmergencyPhoneNumber().toString();
	}
	public String emergency_SMS_number() {
		return TeclaApp.persistence.getEmergencySMSNumber().toString();
	}
	public String emergency_email_address() {
		return TeclaApp.persistence.getEmergencyEmailAddress().toString();
	}
}
