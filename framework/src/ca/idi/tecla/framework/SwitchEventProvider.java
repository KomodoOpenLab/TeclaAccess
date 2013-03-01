package ca.idi.tecla.framework;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import ca.idi.tecla.sdk.SwitchEvent;

public class SwitchEventProvider extends ca.idi.tecla.sdk.SwitchEventProvider {

	private static final String CLASS_TAG = "SwitchEventProvider";
	private static final int REQUEST_IME_DELAY = 60000;
	
	private Intent mSwitchEventIntent;
	private boolean mPhoneRinging;

	private Handler handler;
	private Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		
		init();
	}
	
	private void init() {
		context = this;
		handler = new Handler();
		mPhoneRinging = false;
		
		//Intents & Intent Filters
		registerReceiver(mReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
		mSwitchEventIntent = new Intent(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED);
		
		handler.postDelayed(requestIME, REQUEST_IME_DELAY);
		
		if (TeclaApp.persistence.shouldConnectToShield()) {
			TeclaStatic.logD(CLASS_TAG, "Starting Shield Service...");
			TeclaShieldManager.connect(this);
		}
		TeclaStatic.logD(CLASS_TAG, "Tecla Service created");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		TeclaStatic.logD(CLASS_TAG, "TeclaService onStart called");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		handler.removeCallbacks(requestIME);
		TeclaStatic.logW(CLASS_TAG, "TeclaService onDestroy called!");
	}

	private Runnable requestIME = new Runnable()
	{
		@Override
		public void run()
		{
			handler.removeCallbacks(requestIME);
			if (TeclaStatic.isDefaultIME(context)) {
				//TODO: Check if soft IME view is created
				if (TeclaStatic.isIMERunning(context)) {
					TeclaStatic.logD(CLASS_TAG, "IME is running!");
				} else {
					TeclaStatic.logD(CLASS_TAG, "IME is NOT running!");
				}
				//TODO: If soft IME not running/created, spawn activity to force it open
				//TeclaStatic.logD("TeclaIME is the default!");
			}
			handler.postDelayed(requestIME, REQUEST_IME_DELAY);
		}
		
	};
	
	/** INPUT HANDLING METHODS AND VARIABLES **/
	public void injectSwitchEvent(SwitchEvent event) {
		
		int switchChanges = event.getSwitchChanges();
		int switchStates = event.getSwitchStates();
		
		TeclaStatic.logD(CLASS_TAG, "Handling switch event.");
		if (mPhoneRinging) {
			//Screen should be on
			//Answering should also unlock
			TeclaApp.getInstance().answerCall();
			// Assume phone is not ringing any more
			mPhoneRinging = false;
		} else if (!TeclaApp.getInstance().isScreenOn()) {
			// Screen is off, so just wake it
			TeclaApp.getInstance().wakeUnlockScreen();
			TeclaStatic.logD(CLASS_TAG, "Waking and unlocking screen.");
		} else {
			// In all other instances acquire wake lock,
			// WARNING: just poking user activity timer DOES NOT WORK on gingerbread
			TeclaApp.getInstance().wakeUnlockScreen();
			TeclaStatic.logD(CLASS_TAG, "Broadcasting switch event: " +
					TeclaApp.getInstance().byte2Hex(switchChanges) + ":" +
					TeclaApp.getInstance().byte2Hex(switchStates));

			// Reset intent
			mSwitchEventIntent.removeExtra(SwitchEvent.EXTRA_SWITCH_ACTIONS);
			mSwitchEventIntent.removeExtra(SwitchEvent.EXTRA_SWITCH_CHANGES);
			mSwitchEventIntent.removeExtra(SwitchEvent.EXTRA_SWITCH_STATES);
			//Collect the mapped actions of the current switch
			String[] switchActions = TeclaApp.persistence.getSwitchMap().get(event.toString());
			mSwitchEventIntent.putExtra(SwitchEvent.EXTRA_SWITCH_ACTIONS, switchActions);
			mSwitchEventIntent.putExtra(SwitchEvent.EXTRA_SWITCH_CHANGES, switchChanges);
			mSwitchEventIntent.putExtra(SwitchEvent.EXTRA_SWITCH_STATES, switchStates);

			// Broadcast event
			sendBroadcast(mSwitchEventIntent);
		}
	}
	
	public void injectSwitchEvent(int switchChanges, int switchStates) {
		injectSwitchEvent(new SwitchEvent(switchChanges, switchStates));
	}

	// All intents will be processed here
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
				TeclaStatic.logD(CLASS_TAG, "Phone state changed");
				mPhoneRinging = false;
				TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
					TeclaStatic.logD(CLASS_TAG, "Phone ringing");
					mPhoneRinging = true;
				}
			}
		}

	};

	/** BINDING METHODS AND VARIABLES **/
	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	SwitchEventProvider getService() {
            // Return this instance of LocalService so clients can call public methods
            return SwitchEventProvider.this;
        }
    }
    	
}
