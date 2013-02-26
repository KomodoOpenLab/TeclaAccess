package ca.idi.tecla.framework;

import ca.idi.tecla.framework.Persistence;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.KeyguardManager.KeyguardLock;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class TeclaApp extends Application {

	/**
	 * Tag used for logging in the whole class
	 */
	public static final String CLASS_TAG = "TeclaApp";
	/**
	 * Main debug switch, turns on/off debugging for the whole app
	 */
	public static final boolean DEBUG = true;

	public static final int WAKE_LOCK_TIMEOUT = 5000;

	private static TeclaApp instance;
	public static Persistence persistence;
	public static BluetoothAdapter bluetooth_adapter;
	public static NotificationManager notification_manager;

	private PowerManager power_manager;
	private KeyguardManager keyguard_manager;
	private WakeLock wake_lock;
	private KeyguardLock keyguard_lock;
	private AudioManager audio_manager;

	private Handler handler;

	private Boolean screen_on;
	
	public static TeclaApp getInstance() {
		return instance;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		init();
	}
	
	private void init() {
		
		TeclaStatic.logD(CLASS_TAG, "TECLA FRAMEWORK STARTING ON " + Build.MODEL + " BY " + Build.MANUFACTURER);

		instance = this;
		persistence = new Persistence(this);

		power_manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wake_lock = power_manager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK |
				PowerManager.ON_AFTER_RELEASE, TeclaStatic.TAG);
		keyguard_manager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		keyguard_lock = keyguard_manager.newKeyguardLock(CLASS_TAG);
		bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();
		notification_manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		audio_manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		handler = new Handler();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
			screen_on = getScreenState();
		} else {
			screen_on = true;
		}
		TeclaStatic.logD(CLASS_TAG, "Screen on? " + screen_on);
		
		//Intents & Intent Filters
		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		
		TeclaStatic.startTeclaService(this);
	}

	public boolean isScreenOn() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
			return getScreenState();
		} else {
			return screen_on;
		}
	}
	
	// All intents will be processed here
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				screen_on = false;
				TeclaStatic.logD(CLASS_TAG, "Screen off");
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				screen_on = true;
				TeclaStatic.logD(CLASS_TAG, "Screen on");
			}
		}

	};
	
	@TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
	private Boolean getScreenState() {
		return power_manager.isScreenOn();		
	}

	/**
	 * Hold wake lock until releaseWakeLock() is called.
	 */
	public void holdWakeLock() {
		holdWakeLock(0);
	}
	
	/**
	 * Hold wake lock for the number of seconds specified by length
	 * @param length the number of seconds to hold the wake lock for
	 */
	public void holdWakeLock(long length) {
		if (length > 0) {
			TeclaStatic.logD(CLASS_TAG, "Aquiring temporal wake lock...");
			wake_lock.acquire(length);
		} else {
			TeclaStatic.logD(CLASS_TAG, "Aquiring wake lock...");
			wake_lock.acquire();
		}
		pokeUserActivityTimer();
	}

	public void releaseWakeLock () {
		TeclaStatic.logD(CLASS_TAG, "Releasing wake lock...");
		wake_lock.release();
	}
	
	public void releaseKeyguardLock() {
		TeclaStatic.logD(CLASS_TAG, "Releasing keyguard lock...");
		keyguard_lock.reenableKeyguard();
	}
	
	public void holdKeyguardLock() {
		TeclaStatic.logD(CLASS_TAG, "Acquiring keyguard lock...");
		keyguard_lock.disableKeyguard();
	}
	
	/**
	 * Wakes and unlocks the screen for a minimum of {@link WAKE_LOCK_TIMEOUT} miliseconds
	 */
	public void wakeUnlockScreen() {
		holdKeyguardLock();
		holdWakeLock(WAKE_LOCK_TIMEOUT);
	}

	public void pokeUserActivityTimer () {
		power_manager.userActivity(SystemClock.uptimeMillis(), true);
	}

	public void answerCall() {
		// Simulate a press of the headset button to pick up the call
		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);             
		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
		
		// froyo and beyond trigger on buttonUp instead of buttonDown
		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);               
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
		
		TeclaApp.persistence.isSpeakerphoneEnabled();
		useSpeakerphone();
	}

	public void postDelayedFullReset(long delay) {
		cancelFullReset();
		handler.postDelayed(mFullResetRunnable, delay * 1000);
	}
	
	public void cancelFullReset() {
		handler.removeCallbacks(mFullResetRunnable);
	}
	
	private Runnable mFullResetRunnable = new Runnable () {

		public void run() {
			Intent home = new Intent(Intent.ACTION_MAIN);
			home.addCategory(Intent.CATEGORY_HOME);
			home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(home);
		}

	};

	public void useSpeakerphone() {
		audio_manager.setMode(AudioManager.MODE_IN_CALL);
		audio_manager.setSpeakerphoneOn(true);
	}
	
	public void stopUsingSpeakerPhone() {
		audio_manager.setMode(AudioManager.MODE_NORMAL);
		audio_manager.setSpeakerphoneOn(false);
	}

	public String byte2Hex(int bite) {
		return String.format("0x%02x", bite);
	}
	
	public void showToast(int resid) {
		Toast.makeText(this, resid, Toast.LENGTH_LONG).show();
	}
	
	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

}
