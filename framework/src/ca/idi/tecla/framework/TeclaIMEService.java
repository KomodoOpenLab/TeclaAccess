package ca.idi.tecla.framework;

import ca.idi.tecla.framework.SwitchEventProvider.LocalBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;

public class TeclaIMEService extends InputMethodService {
	
	private static final String CLASS_TAG = "TeclaIMEService";

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Bind to SwitchEventProvider
		Intent intent = new Intent(this, SwitchEventProvider.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	public void injectSwitchEvent(SwitchEvent event) {
		switch_event_provider.injectSwitchEvent(event);
	}
	
	public void injectSwitchEvent(int switchChanges, int switchStates) {
		switch_event_provider.injectSwitchEvent(switchChanges, switchStates);
	}
	
	SwitchEventProvider switch_event_provider;
	boolean mBound = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            switch_event_provider = binder.getService();
            mBound = true;
            TeclaStatic.logD(CLASS_TAG, "IME bound to SEP");
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
			
		}
    };
    
    
}
