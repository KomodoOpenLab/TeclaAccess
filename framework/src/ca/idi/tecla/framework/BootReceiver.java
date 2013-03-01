package ca.idi.tecla.framework;

import ca.idi.tecla.sdk.SEPManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			SEPManager.start(context);
		}
		
	}
	
}
