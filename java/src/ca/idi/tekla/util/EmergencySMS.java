package ca.idi.tekla.util;

import android.content.Context;
import android.os.AsyncTask;
import ca.idi.tekla.TeclaApp;
import ca.idi.tecla.framework.TeclaStatic;
import android.telephony.SmsManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

class EmergencySMS extends AsyncTask<Context, Void, Boolean> {

	@Override
	protected Boolean doInBackground(Context... params) {
		String message = "";
		// with or without emergency GPS setting?
		if (emergency_GPS_setting()) {
			String location[] = getLocation(params[0]);
			if (location[0].length() == 0 || location[1].length() == 0) {
				message = "Emergency call! Please take immediate action, No locator available.";
				TeclaStatic.logD(TeclaApp.CLASS_TAG, "No location available!");
			} else {
				message = "Emergency call! Please come to me, click for location: http://maps.google.com/maps?&z=14&t=h&q=loc:"
						+ location[2]
						+ "Latitude: "
						+ location[0]
						+ "Longitude: " + location[1];
				TeclaStatic.logD(TeclaApp.CLASS_TAG, "Location: " + location[2]);
			}
		} else {
			message = "Emergency call! Please take immediate action!";
		}
		Boolean succes = true;
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(emergency_SMS_number(), null, message, null,
				null);
		return succes;
	}

	public String emergency_SMS_number() {
		return TeclaApp.persistence.getEmergencySMSNumber().toString();
	}

	public boolean emergency_GPS_setting() {
		return TeclaApp.persistence.getEmergencyGPSSetting();
	}

	public String[] getLocation(Context context) {
		/* array location:
		 * [0] Longitude
		 * [1] Latitude
		 * [2] after each other with + between for google maps
		 */
		String currentLocation[] = new String[3];
		Criteria criteria = new Criteria();
		LocationManager locMan = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		String towers = locMan.getBestProvider(criteria, false);
		Location location = locMan.getLastKnownLocation(towers);
		currentLocation[0] = String.valueOf(location.getLatitude());
		currentLocation[1] = String.valueOf(location.getLongitude());
		currentLocation[2] = String.valueOf(location.getLatitude()) + "+"
				+ String.valueOf(location.getLongitude());
		return currentLocation;
	}
}