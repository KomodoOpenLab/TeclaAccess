package ca.idi.tecla.sdk;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TeclaCommon
{
	/**
	 * Tag used for logging in the whole framework
	 */
	public static final String TAG = "TeclaFramework";
	/**
	 * Main debug switch, turns on/off debugging for the whole framework
	 */
	public static final boolean DEBUG = true;

	public static void startTeclaService (Context context)
	{
		if (DEBUG) Log.d(TeclaCommon.TAG, "Starting TeclaService...");
		if (!isTeclaServiceRunning(context))
		{
			Intent serviceIntent = new Intent(TeclaService.NAME);
			context.startService(serviceIntent);		
		} else
		{
			if (DEBUG) Log.w(TeclaCommon.TAG, "Tecla Service already started!");
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
}
