package ca.idi.tekla.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import ca.idi.tecla.framework.TeclaStatic;
import ca.idi.tekla.TeclaApp;

class EmergencyPoneCall extends AsyncTask<Context, Void, Void> {

	@Override
	protected Void doInBackground(Context... params) {
		Context context = params[0];

		TeclaStatic.logD(TeclaApp.CLASS_TAG, " doInBackground context: " + context.toString());
		try {
			Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+emergency_phone_number()));
			//intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Exception e) {
			TeclaStatic.logD(TeclaApp.CLASS_TAG, " doInBackground error: " + e);
		}
		return null;
	}

	public String emergency_phone_number() {
		return TeclaApp.persistence.getEmergencyPhoneNumber().toString();
	}

}