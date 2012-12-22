package ca.idi.tecla.sdk;

import android.app.Application;
import android.os.Build;
import android.util.Log;

public class TeclaApp extends Application
{

	@Override
	public void onCreate()
	{
		super.onCreate();
		init();
	}
	
	private void init()
	{
		Log.d(TeclaCommon.TAG, "TECLA FRAMEWORK STARTING ON " + Build.MODEL + " BY " + Build.MANUFACTURER);
		
		TeclaCommon.startTeclaService(this);
	}

}
