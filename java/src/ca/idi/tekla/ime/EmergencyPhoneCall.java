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