package ca.idi.tekla.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import ca.idi.tecla.framework.TeclaStatic;
import ca.idi.tekla.TeclaApp;

class EmergencyEmail extends AsyncTask<Context, Void, Boolean> {

	@Override
	protected Boolean doInBackground(Context... params) {
		Context context = params[0];
		Boolean succes = false;
		try {
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+emergency_SMS_number())); 
 			intent.putExtra("sms_body", "Shoot! Shoot! Shoot motherf*cker!"); 
			//intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			succes = true;
		} catch (Exception e) {
			TeclaStatic.logD(TeclaApp.CLASS_TAG, " doInBackground error: " + e);
		}
		return succes;
	}
	
	public String emergency_SMS_number() {
		return TeclaApp.persistence.getEmergencySMSNumber().toString();
	}

}