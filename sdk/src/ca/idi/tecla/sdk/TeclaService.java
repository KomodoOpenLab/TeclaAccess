package ca.idi.tecla.sdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TeclaService extends Service
{

	public static final String NAME = "ca.idi.tecla.sdk.TECLA_SERVICE";

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		if (TeclaCommon.DEBUG) Log.d(TeclaCommon.TAG, "Tecla Service created");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (TeclaCommon.DEBUG) Log.d(TeclaCommon.TAG, "Tecla Service destroyed");
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		if (TeclaCommon.DEBUG) Log.d(TeclaCommon.TAG, "Tecla Service started");
	}

}
