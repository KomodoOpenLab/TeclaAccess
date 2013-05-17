package ca.idi.tekla.util;

import java.util.concurrent.ExecutionException;

import android.content.Context;
import ca.idi.tecla.framework.TeclaStatic;
import ca.idi.tekla.TeclaApp;

public class EmergencyCallout extends ca.idi.tekla.ime.TeclaIME {

	public void Callout(Context context) {

		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency proc context: "
				+ context.toString());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency proc GPS Setting: "
				+ emergency_GPS_setting());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency proc Phone number: "
				+ emergency_phone_number());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency proc SMS number: "
				+ emergency_SMS_number());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, " Emergency proc Email address: "
				+ emergency_email_address());
		if (emergency_phone_number().length() > 0)
			makePhoneCall(context);
		if (emergency_SMS_number().length() > 0)
			makeSMS(context);
	}

	public Boolean makePhoneCall(Context context) {
		try {
			if (new EmergencyPoneCall().execute(context).get())
				TeclaStatic.logD(TeclaApp.CLASS_TAG,
						"Phone call succesfully initiated");
		} catch (InterruptedException e) {
			TeclaStatic.logD(TeclaApp.CLASS_TAG, "Phone call error: " + e);
		} catch (ExecutionException e) {
			TeclaStatic.logD(TeclaApp.CLASS_TAG, "Phone call error: " + e);
		}
		return true;
	}

	public Boolean makeSMS(Context context) {
		try {
			if (new EmergencySMS().execute(context).get())
				TeclaStatic.logD(TeclaApp.CLASS_TAG,
						"SMS msg succesfully initiated");
		} catch (InterruptedException e) {
			TeclaStatic.logD(TeclaApp.CLASS_TAG, "SMS error: " + e);
		} catch (ExecutionException e) {
			TeclaStatic.logD(TeclaApp.CLASS_TAG, "SMS error: " + e);
		}
		return true;
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
	
	
/*	Google maps link explanation	
  	http://maps.google.com/maps?&z=14&ll=39.211374,-82.978277 
	z for zoom 
	ll for lat/lon 
*/
}
