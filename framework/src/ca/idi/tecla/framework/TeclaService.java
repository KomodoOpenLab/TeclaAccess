package ca.idi.tecla.framework;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class TeclaService extends Service
{

	public static final String NAME = "ca.idi.tecla.sdk.TECLA_SERVICE";
	
	private static final int REQUEST_IME_DELAY = 1000;
	
	private Handler handler;
	private Context context;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		init();
	}
	
	private void init()
	{
		context = this;
		handler = new Handler();
		handler.postDelayed(requestIME, REQUEST_IME_DELAY);
		TeclaStatic.logD("Tecla Service created");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		handler.removeCallbacks(requestIME);
		TeclaStatic.logW("TeclaService onDestroy called!");
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		TeclaStatic.logD("TeclaService onStart called");
	}

	private Runnable requestIME = new Runnable()
	{
		@Override
		public void run()
		{
			handler.removeCallbacks(requestIME);
			if (TeclaStatic.isDefaultIME(context))
			{
				//TODO: Check if soft IME view is created
				if (TeclaStatic.isIMERunning(context))
				{
					TeclaStatic.logD("IME is running!");
				} else
				{
					TeclaStatic.logD("IME is NOT running!");
				}
				//TODO: If soft IME not running/created, spawn activity to force it open
				//TeclaStatic.logD("TeclaIME is the default!");
			}
			handler.postDelayed(requestIME, REQUEST_IME_DELAY);
		}
		
	};
	
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	TeclaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TeclaService.this;
        }
    }
    

	
}
