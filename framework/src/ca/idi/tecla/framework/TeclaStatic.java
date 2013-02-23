package ca.idi.tecla.framework;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class TeclaStatic
{
	/**
	 * Main debug switch, turns on/off debugging for the whole framework
	 */
	public static final boolean DEBUG = true;

	/**
	 * Tag used for logging in the whole framework
	 */
	public static final String TAG = "TeclaFramework";
	public static final String CLASS_TAG = "TeclaStatic";

	private static final String IME_ID = "ca.idi.tekla/.ime.TeclaIME";
	private static final String IME_SERVICE = "ca.idi.tekla.ime.TeclaIME";

	public static void startTeclaService (Context context) {
		logD(CLASS_TAG, "Starting TeclaService...");
		if (!isTeclaServiceRunning(context)) {
			Intent serviceIntent = new Intent(TeclaService.NAME);
			context.startService(serviceIntent);		
		} else {
			logW(CLASS_TAG, "Tecla Service already running!");
		}
	}
	
	private static boolean isTeclaServiceRunning(Context context)
	{
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service_info : manager.getRunningServices(Integer.MAX_VALUE))
	    {
	        if (TeclaService.class.getName().equals(service_info.service.getClassName()))
	        {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static boolean isIMERunning(Context context)
	{
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service_info : manager.getRunningServices(Integer.MAX_VALUE))
	    {
	        if (IME_SERVICE.equals(service_info.service.getClassName()))
	        {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static Boolean isDefaultIME(Context context) {
		String ime_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
		if (ime_id.equals(IME_ID)) return true;
		return false;
	}

	public static void logV(String class_tag, String msg) {
		if (DEBUG) Log.v(TAG, class_tag + ": " + msg);
	}
	
	public static void logI(String class_tag, String msg) {
		if (DEBUG) Log.i(TAG, class_tag + ": " + msg);
	}
	
	public static void logD(String class_tag, String msg) {
		if (DEBUG) Log.d(TAG, class_tag + ": " + msg);
	}
	
	public static void logW(String class_tag, String msg) {
		if (DEBUG) Log.w(TAG, class_tag + ": " + msg);
	}

	public static void logE(String class_tag, String msg) {
		if (DEBUG) Log.e(TAG, class_tag + ": " + msg);
	}
}
