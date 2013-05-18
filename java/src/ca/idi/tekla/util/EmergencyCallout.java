package ca.idi.tekla.util;

import java.util.concurrent.ExecutionException;

import android.content.Context;
import ca.idi.tecla.framework.TeclaStatic;
import ca.idi.tekla.TeclaApp;

public class EmergencyCallout extends ca.idi.tekla.ime.TeclaIME {

	public void Callout(Context context) {

		Boolean didAnything = false;

		TeclaStatic.logD(TeclaApp.CLASS_TAG, "Emergency proc context: "
				+ context.toString());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, "Emergency proc GPS Setting: "
				+ emergency_GPS_setting());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, "Emergency proc Phone number: "
				+ emergency_phone_number());
		TeclaStatic.logD(TeclaApp.CLASS_TAG, "Emergency proc SMS number: "
				+ emergency_SMS_number());
		/*
		 * Following asynchroneous tasks are completely self-sufficient so they
		 * can take care (or not) on their own without depending on anything.
		 * Some parts will be executed twice or even more but the advantage is
		 * that they will not interfere or wait on eachother.
		 */

		if (emergency_phone_number().length() > 0)
			if (makePhoneCall(context))
				didAnything = true;
		if (emergency_SMS_number().length() > 0)
			if (makeSMS(context))
				didAnything = true;

		if (didAnything) {
			// TODO make happy sound
			TeclaStatic.logD(TeclaApp.CLASS_TAG,
					"Yes at least we did something");
		} else {
			// TODO make unhappy sound
			TeclaStatic.logD(TeclaApp.CLASS_TAG, "No we did");
		}
	}

	public Boolean makePhoneCall(Context context) {
		try {
			if (new EmergencyPoneCall().execute(context).get())
				TeclaStatic.logD(TeclaApp.CLASS_TAG,
						"Phone call succesfully initiated");
			else
				TeclaStatic.logD(TeclaApp.CLASS_TAG, "Phonecall not initiatd?");
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
			else
				TeclaStatic.logD(TeclaApp.CLASS_TAG, "SMS not send?");
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

	/*
	 * Google maps link explanation
	 * http://maps.google.com/maps?&z=14&ll=39.211374,-82.978277 z for zoom ll
	 * for lat/lon
	 */
}
