/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ca.idi.tekla.ime;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import ca.idi.tekla.TeclaApp;
import ca.idi.tekla.ime.EmergencyLocation.LocationResult;
import android.telephony.SmsManager;
import android.util.Log;
import android.location.Location;
import android.location.LocationManager;

public class EmergencySMS extends AsyncTask<Object, Void, Boolean> {

	@Override
	protected Boolean doInBackground(Object... params) {
		final Context context = (Context) params[0];
		final String smsNumber = (String) params[1];
		final String currentLocation[] = new String[4];

		Looper.prepare();
		Looper.getMainLooper();	

		LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		        //Got the location!
				Log.d("GPS", "JA! location:" + location);
				currentLocation[0] = String.valueOf(location.getLatitude());
				currentLocation[1] = String.valueOf(location.getLongitude());
				currentLocation[2] = String.valueOf(location.getLatitude()) + "+"
						+ String.valueOf(location.getLongitude());
				currentLocation[3] = location.getProvider();

				// start SMS messaging!
				String message = null;
				if (emergency_GPS_setting(context)) {
						message = context.getString(ca.idi.tekla.R.string.emergency_SMS_text_withLoc) + " http://maps.google.com/maps?&z=17&t=h&q=loc:"
								+ currentLocation[2]
								+ " Provider: "
								+ currentLocation[3];
				} else {
					message = context.getString(ca.idi.tekla.R.string.emergency_SMS_text_withoutLoc);
				}
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(smsNumber, null, message, null, null);
		    }
		};
		
		EmergencyLocation myLocation = new EmergencyLocation();
		myLocation.getLocation(context, locationResult); //this
		
		// not crashed? we did our job.
		return true;
	}

	private boolean emergency_GPS_setting(Context context) {
		Boolean gpsActive = false;
		if (TeclaApp.persistence.getEmergencyGPSSetting()) {
			LocationManager locMan = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			// check wether any useable location service is turned on
			if (locMan.isProviderEnabled(LocationManager.GPS_PROVIDER))
				gpsActive = true;
			if (locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
				gpsActive = true;
		}
		return gpsActive;
	}
}