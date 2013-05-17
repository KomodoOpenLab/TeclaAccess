package ca.idi.tekla.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import ca.idi.tecla.framework.TeclaStatic;
import ca.idi.tekla.TeclaApp;

class EmergencyPoneCall extends AsyncTask<Context, Void, Boolean> {

	@Override
	protected Boolean doInBackground(Context... params) {
		Context context = params[0];
		Boolean succes = false;
		try {
			Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+emergency_phone_number()));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			succes = true;
		} catch (Exception e) {
			TeclaStatic.logD(TeclaApp.CLASS_TAG, " doInBackground error: " + e);
		}
		return succes;
	}

	public String emergency_phone_number() {
		return TeclaApp.persistence.getEmergencyPhoneNumber().toString();
	}

}