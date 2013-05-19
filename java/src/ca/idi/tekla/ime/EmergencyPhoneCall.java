package ca.idi.tekla.ime;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import ca.idi.tecla.framework.TeclaStatic;
import ca.idi.tekla.TeclaApp;

public class EmergencyPhoneCall extends AsyncTask<Object, Void, Boolean> {

	@Override
	protected Boolean doInBackground(Object... params) {
		Context context = (Context) params[0];
		String phoneNumber = (String) params[1];
		Boolean succes = false;
		try {
			Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + phoneNumber));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			succes = true;
		} catch (Exception e) {
			TeclaStatic.logD(TeclaApp.CLASS_TAG, " doInBackground error: " + e);
		}
		return succes;
	}

}