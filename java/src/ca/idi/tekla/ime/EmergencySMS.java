package ca.idi.tekla.ime;

import android.content.Context;
import android.os.AsyncTask;
import ca.idi.tekla.TeclaApp;
import ca.idi.tecla.framework.TeclaStatic;
import android.telephony.SmsManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

public class EmergencySMS extends AsyncTask<Object, Void, Boolean> {

	@Override
	protected Boolean doInBackground(Object... params) {
		String message = "";
		Context context = (Context) params[0];
		String smsNumber = (String) params[1];
		// with or without emergency GPS setting?
		if (emergency_GPS_setting()) {
			String location[] = getLocation(context);
			if (location[0].length() == 0 || location[1].length() == 0) {
				message = context.getString(ca.idi.tekla.R.string.emergency_SMS_text_withoutLoc);
				TeclaStatic.logD(TeclaApp.CLASS_TAG, "No location available!");
			} else {
				message = context.getString(ca.idi.tekla.R.string.emergency_SMS_text_withLoc) + " http://maps.google.com/maps?&z=17&t=h&q=loc:"
						+ location[2]
						+ " Latitude: "
						+ location[0]
						+ " Longitude: " + location[1];
				TeclaStatic.logD(TeclaApp.CLASS_TAG, "Location: " + location[2]);
			}
		} else {
			message = context.getString(ca.idi.tekla.R.string.emergency_SMS_text_withoutLoc);
		}
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(smsNumber, null, message, null,
				null);
		// not crashed? we did our job.
		return true;
	}

	private boolean emergency_GPS_setting() {
		return TeclaApp.persistence.getEmergencyGPSSetting();
	}

	private String[] getLocation(Context context) {
		/* array location:
		 * [0] Longitude
		 * [1] Latitude
		 * [2] after each other with + between for google maps
		 */
		String currentLocation[] = new String[3];
		Criteria criteria = new Criteria();
		Location location;
		LocationManager locMan = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		String currentPosition = locMan.getBestProvider(criteria, false);
		location = locMan.getLastKnownLocation(currentPosition);
		currentLocation[0] = String.valueOf(location.getLatitude());
		currentLocation[1] = String.valueOf(location.getLongitude());
		currentLocation[2] = String.valueOf(location.getLatitude()) + "+"
				+ String.valueOf(location.getLongitude());
		return currentLocation;
	}
}