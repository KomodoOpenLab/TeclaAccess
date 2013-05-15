package ca.idi.tekla.util;

import android.content.Context;
import ca.idi.tecla.framework.TeclaStatic;
import ca.idi.tekla.TeclaApp;

public class EmergencyCallout extends ca.idi.tekla.ime.TeclaIME {
	
	public void Callout(Context context) {
		
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency proc context: " + context.toString());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency proc GPS Setting: " + emergency_GPS_setting());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency proc Phone number: " + emergency_phone_number());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency proc SMS number: " + emergency_SMS_number());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency proc Email address: "+ emergency_email_address());
		if(emergency_phone_number().length() > 0) {
			new EmergencyPoneCall().execute(context);
		}
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
