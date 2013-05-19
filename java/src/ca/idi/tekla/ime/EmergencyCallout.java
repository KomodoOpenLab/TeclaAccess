package ca.idi.tekla.ime;

import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.media.MediaPlayer;
import ca.idi.tecla.framework.TeclaStatic;
import ca.idi.tekla.TeclaApp;

public class EmergencyCallout extends TeclaIME {

	public void Callout(Context context) {

		Boolean didPhoneCall = false;
		String phoneNumber = emergency_phone_number();
		String smsNumber = emergency_SMS_number();

		if (phoneNumber.length() > 0) {
			try {
				if (new EmergencyPhoneCall().execute(context,
						phoneNumber).get()) {
					TeclaStatic.logD(TeclaApp.CLASS_TAG,
							"Phone call succesfully initiated");
					didPhoneCall = true;
				} else {
					TeclaStatic.logD(TeclaApp.CLASS_TAG,
							"Phonecall not initiatd?");
				}
			} catch (InterruptedException e) {
				TeclaStatic.logD(TeclaApp.CLASS_TAG, "Phone call error: " + e);
			} catch (ExecutionException e) {
				TeclaStatic.logD(TeclaApp.CLASS_TAG, "Phone call error: " + e);
			}
		}

		if (smsNumber.length() > 0) {
			try {
				if (new EmergencySMS().execute(context, smsNumber).get()) {
					TeclaStatic.logD(TeclaApp.CLASS_TAG,
							"SMS msg succesfully initiated");
					/* sound to ack something happened when no phonecall is made, otherwise 
					 * user does not get any signal anything happened.
					 */
					if(!didPhoneCall) {
						MediaPlayer mPlay = MediaPlayer.create(context,
								ca.idi.tekla.R.raw.emergency_succes);
						mPlay.start();
						Thread.sleep(500);
						mPlay.stop();
						mPlay.reset();
						mPlay.release();
						mPlay = null ;					}
				} else {
					TeclaStatic.logD(TeclaApp.CLASS_TAG, "SMS not send?");
				}
			} catch (InterruptedException e) {
				TeclaStatic.logD(TeclaApp.CLASS_TAG, "SMS error: " + e);
			} catch (ExecutionException e) {
				TeclaStatic.logD(TeclaApp.CLASS_TAG, "SMS error: " + e);
			}
		}
	}

	private String emergency_phone_number() {
		return TeclaApp.persistence.getEmergencyPhoneNumber().toString();
	}

	private String emergency_SMS_number() {
		return TeclaApp.persistence.getEmergencySMSNumber().toString();
	}

	/*
	 * Google maps link explanation
	 * http://maps.google.com/maps?&z=14&ll=39.211374,-82.978277 z for zoom ll
	 * for lat/lon
	 */
}
