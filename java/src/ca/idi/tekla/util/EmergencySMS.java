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
		if (emergency_GPS_setting()) {
			String location = getLocation(params[0]);
			message = "Emergency call! Please come to me, click for location: http://maps.google.com/maps?&z=14&t=h&q=loc:" + location;
		} else {
			message = "Emergency call! Please take immediate action!";
		}
		Boolean succes = true;
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(emergency_SMS_number(), null, message, null, null);
		return succes;
	}

	public String emergency_SMS_number() {
		return TeclaApp.persistence.getEmergencySMSNumber().toString();
	}

	public boolean emergency_GPS_setting() {
		return TeclaApp.persistence.getEmergencyGPSSetting();
	}

	public String getLocation(Context context) {
		String point = "";
	    Criteria criteria = new Criteria();
	    LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	    String towers = locMan.getBestProvider(criteria, false);
	    Location location = locMan.getLastKnownLocation(towers);
		TeclaStatic.logD(TeclaApp.CLASS_TAG, "Location: " + location.getLatitude());
		point = location.getLatitude() + "+" + location.getLongitude();
		return point;
	}
}