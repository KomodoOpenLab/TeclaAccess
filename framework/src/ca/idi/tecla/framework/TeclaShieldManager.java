/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tecla.framework;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TeclaShieldManager {

	/**
	 * Intent string used to start and stop the switch event
	 * provider service. {@link #EXTRA_SHIELD_ADDRESS}
	 * must be provided to start the service.
	 */
	private static final String SHIELD_SERVICE = "ca.idi.tecla.framework.TECLA_SHIELD_SERVICE";
	private static final String SHIELD_SERVICE_CLASS = "ca.idi.tecla.framework.TeclaShieldService";
	/**
	 * Tecla Shield MAC Address to connect to.
	 */
	public static final String EXTRA_SHIELD_ADDRESS = "ca.idi.tecla.sdk.extra.SHIELD_ADDRESS";
	
	/**
	 * Start the Switch Event Provider and attempt a connection with the last known Tecla Shield
	 */
	public static boolean connect(Context context) {
		return connect(context, null);
	}

	/**
	 * Start the Switch Event Provider and attempt a connection with a Tecla Shield with the address provided
	 */
	public static boolean connect(Context context, String shieldAddress) {
		Intent sepIntent = new Intent(SHIELD_SERVICE);
		sepIntent.putExtra(EXTRA_SHIELD_ADDRESS, shieldAddress);
		return context.startService(sepIntent) == null? false:true;
	}

	public static boolean disconnect(Context context) {
		Intent sepIntent = new Intent(SHIELD_SERVICE);
		return context.stopService(sepIntent);
	}

	public static boolean isRunning(Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	Log.d("Tecla SDK", service.service.getClassName().toString());
	    	if (SHIELD_SERVICE_CLASS.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
}
