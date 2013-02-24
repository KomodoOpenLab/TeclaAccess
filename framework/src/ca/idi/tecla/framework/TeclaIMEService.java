package ca.idi.tecla.framework;

import ca.idi.tecla.framework.SwitchEventProvider.LocalBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;

public class TeclaIMEService extends InputMethodService {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Bind to TeclaService
		Intent intent = new Intent(this, SwitchEventProvider.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	SwitchEventProvider tecla_service;
	boolean mBound = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            tecla_service = binder.getService();
            mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
			
		}
    };
    
    
}
