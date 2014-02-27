package ca.idi.tecla.sdk;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;

public class SEPManager {

	public static final String SEP_SERVICE = "ca.idi.tecla.framework.SWITCH_EVENT_PROVIDER";

	/**
	 * Start the Switch Event Provider and attempt a connection with a Tecla Shield with the address provided
	 */
	public static void start(Context context) {
		//		logD(CLASS_TAG, "Starting TeclaService...");
		if (!isRunning(context)) {
			Intent sepIntent = new Intent(SEP_SERVICE);
			context.startService(sepIntent);
		} else {
			//			logW(CLASS_TAG, "Tecla Service already running!");
		}
	}

	public static boolean stop(Context context) {
		if (isRunning(context)) {
			Intent sepIntent = new Intent(SEP_SERVICE);
			return context.stopService(sepIntent);
		}
		return false;
	}

	private static boolean isRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service_info : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (SEP_SERVICE.equals(service_info.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
