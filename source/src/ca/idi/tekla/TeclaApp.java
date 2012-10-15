/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla;

import java.util.ArrayList;

import android.app.Application;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import ca.idi.tekla.util.Highlighter;
import ca.idi.tekla.util.Persistence;
import ca.idi.tekla.util.TeclaDesktopClient;

public class TeclaApp extends Application {

	/**
	 * Tag used for logging in the whole app
	 */
	public static final String TAG = "Tecla";
	/**
	 * Main debug switch, turns on/off debugging for the whole app
	 */
	public static final boolean DEBUG = false	;

	public static final String TECLA_IME_ID = "ca.idi.tekla/.ime.TeclaIME";

	//IME CONSTANTS
	public static final String PACKAGE_VOICE_SEARCH = "com.google.android.voicesearch";
//	public static final String PACKAGE_QUICKSEARCHBOX = "com.android.quicksearchbox";
	public static final String PACKAGE_QUICKSEARCHBOX = "com.google.android.googlequicksearchbox";
	public static final String ACTION_ENABLE_MORSE = "ca.idi.tekla.ime.action.ENABLE_MORSE";
	public static final String ACTION_DISABLE_MORSE = "ca.idi.tekla.ime.action.DISABLE_MORSE";
	public static final String ACTION_SHOW_IME = "ca.idi.tekla.ime.action.SHOW_IME";
	public static final String ACTION_HIDE_IME = "ca.idi.tekla.ime.action.HIDE_IME";
	public static final String ACTION_IME_CREATED = "ca.idi.tekla.ime.action.SOFT_IME_CREATED";
	public static final String ACTION_START_FS_SWITCH_MODE = "ca.idi.tekla.ime.action.START_FS_SWITCH_MODE";
	public static final String ACTION_STOP_FS_SWITCH_MODE = "ca.idi.tekla.ime.action.STOP_FS_SWITCH_MODE";
	public static final String ACTION_INPUT_STRING = "ca.idi.tekla.ime.action.INPUT_STRING";
	public static final String EXTRA_INPUT_STRING = "ca.idi.tekla.sep.extra.INPUT_STRING";
	private static final long BOOT_TIMEOUT = 60000;
	private static final int WAKE_LOCK_TIMEOUT = 5000;
	
	private PowerManager mPowerManager;
	private KeyguardManager mKeyguardManager;
	private KeyguardLock mKeyguardLock;
	private WakeLock mWakeLock;
	private AudioManager mAudioManager;
	private PackageManager mPackageManager;
	private Handler mHandler;

	/** Record whether we've broadcast an ACTION_IME_CREATED Intent,
	 * so that any client creating a BroadcastReceiver after this
	 * can detect and respond accordingly. (Note: access synchronized
	 * on this). */
	private boolean mIMECreated;
	
	private static TeclaApp instance;
	public static Persistence persistence;
	public static Highlighter highlighter;
	
	public static String password="Tecla123";
	public static boolean sendflag=false,connect_to_desktop=false;
	
	public static TeclaDesktopClient desktop;
	public static Object dictation_lock=new Object();
	public static boolean dict_lock=false,mSendToPC;
	
	public static int disconnect_event;
	public static int dictation_event;
	
	public TeclaApp() {
        instance = this;
    }
	
	public static TeclaApp getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}
	
	private void init() {

		//if (TeclaApp.DEBUG) android.os.Debug.waitForDebugger();
		Log.d(TAG, "TECLA APP STARTING ON " + Build.MODEL + " BY " + Build.MANUFACTURER);
		
		persistence = new Persistence(this);
		highlighter = new Highlighter(this);
		
		

		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK |
				PowerManager.ON_AFTER_RELEASE, TeclaApp.TAG);
		mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		mKeyguardLock = mKeyguardManager.newKeyguardLock(TeclaApp.TAG);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mPackageManager = getPackageManager();
		
		mHandler = new Handler();
		mIMECreated = false;
		
		persistence.unsetInverseScanningChanged();
		persistence.setScreenOn();
		
		//Intents & Intent Filters
		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		connect_to_desktop=prefs.getBoolean(Persistence.CONNECT_TO_PC, false);
		sendflag=prefs.getBoolean(Persistence.SEND_SHIELD_EVENTS, false);
		disconnect_event=prefs.getInt("set_disconnect_event", 65);
		dictation_event=prefs.getInt("set_dictation_event", 55);
		password=prefs.getString(Persistence.SET_PASSWORD, "Tecla123");
		//if (persistence.isPersistentKeyboardEnabled()) queueSplash();

	}

	/**
	 * This method is for use in emulated process environments.
	 * It will never be called on a production Android device,
	 * where processes are removed by simply killing them;
	 * no user code (including this callback) is executed when doing so. 
	 */
	@Override
	public void onTerminate() {
		unregisterReceiver(mReceiver);
		releaseWakeLock();
		releaseKeyguardLock();
		Log.d(TAG, "TECLA APP TERMINATED!");
		super.onTerminate();
	}

	// All intents will be processed here
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, "Screen off");
				persistence.setScreenOff();
				releaseKeyguardLock();
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, "Screen on");
				persistence.setScreenOn();
				if (persistence.isPersistentKeyboardEnabled()) requestShowIMEView();
			}
		}

	};

	public void requestShowIMEView() {
		requestShowIMEView(0);
	}
	
	public void requestShowIMEView(long delay) {
		mHandler.removeCallbacks(mRequestShowIMERunnable);
		mHandler.postDelayed(mRequestShowIMERunnable, delay);
	}
	
	private Runnable mRequestShowIMERunnable = new Runnable() {

		public void run() {
			if (DEBUG) Log.d(TAG, "Broadcasting show IME intent...");
			sendBroadcast(new Intent(ACTION_SHOW_IME));
		}
		
	};
	
	/**
	 * A recursive call to force the soft IME open without blocking the UI
	 * @param delay the time after which the call is sent
	 * TODO: This method should be moved to the TeclaApp class
	 */
	public void callShowSoftIMEWatchDog(int delay) {
		mHandler.removeCallbacks(mShowSoftIMEWatchdog);
		mHandler.postDelayed(mShowSoftIMEWatchdog, delay);
	}
	
	private Runnable mShowSoftIMEWatchdog = new Runnable () {

		public void run() {
			if (!TeclaApp.highlighter.isSoftIMEShowing()) {
				// If IME View still not showing...
				// We are force-openning the soft IME through an intent since
				//it seems to be the only way to make it work
				TeclaApp.getInstance().requestShowIMEView();
			}
		}
		
	};

	public void enabledMorseIME() {
		if (DEBUG) Log.d(TAG, "Broadcasting enable morse IME intent...");
		sendBroadcast(new Intent(ACTION_ENABLE_MORSE));
	}
	
	public void disabledMorseIME() {
		if (DEBUG) Log.d(TAG, "Broadcasting disable morse IME intent...");
		sendBroadcast(new Intent(ACTION_DISABLE_MORSE));
	}

	public void requestHideIMEView() {
		if (DEBUG) Log.d(TAG, "Broadcasting hide IME intent...");
		sendBroadcast(new Intent(ACTION_HIDE_IME));
	}
	
	public void startFullScreenSwitchMode() {
		if (DEBUG) Log.d(TAG, "Broadcasting start fullscreen switch mode intent...");
		sendBroadcast(new Intent(ACTION_START_FS_SWITCH_MODE));
	}

	public void stopFullScreenSwitchMode() {
		if (DEBUG) Log.d(TAG, "Broadcasting stop fullscreen switch mode intent...");
		sendBroadcast(new Intent(ACTION_STOP_FS_SWITCH_MODE));
	}

	public void startScanningTeclaIME() {
		if (DEBUG) Log.d(TAG, "Broadcasting start scanning IME intent...");
		sendBroadcast(new Intent(Highlighter.ACTION_START_SCANNING));
	}

	public void stopScanningTeclaIME() {
		if (DEBUG) Log.d(TAG, "Broadcasting stop scanning IME intent...");
		sendBroadcast(new Intent(Highlighter.ACTION_STOP_SCANNING));
	}
	
	public void inputStringAvailable(String input_string) {
		Log.d(TeclaApp.TAG, "Broadcasting input string: " + input_string);
		Intent intent = new Intent(ACTION_INPUT_STRING);
		intent.putExtra(EXTRA_INPUT_STRING, input_string);
		sendBroadcast(intent);
	}
	
	public void inputStringListAvailable(ArrayList<String> input_string) {
		Log.d(TeclaApp.TAG, "Broadcasting input string: " + input_string);
		Intent intent = new Intent(ACTION_INPUT_STRING);
		intent.putExtra(EXTRA_INPUT_STRING, input_string);
		sendBroadcast(intent);
	}
	
	public void postDelayedFullReset(long delay) {
		cancelFullReset();
		mHandler.postDelayed(mFullResetRunnable, delay * 1000);
	}
	
	public void cancelFullReset() {
		mHandler.removeCallbacks(mFullResetRunnable);
	}
	
	private Runnable mFullResetRunnable = new Runnable () {

		public void run() {
			Intent home = new Intent(Intent.ACTION_MAIN);
			home.addCategory(Intent.CATEGORY_HOME);
			home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(home);
		}

	};

	public void candidatesAvailable() {
		//TODO: Sends list of candidates to IME for user selection
	}

	public void queueSplash() {

		if (isDefaultIME()) {
			long now = SystemClock.uptimeMillis();
			if (persistence.isPersistentKeyboardEnabled()) {
				if (now < BOOT_TIMEOUT) {
					// If just booted, wait a bit before calling splash
					Log.w(TeclaApp.TAG, "Delayed call to show splash screen");
					mHandler.removeCallbacks(mShowSplashRunnable);
					mHandler.postAtTime(mShowSplashRunnable, BOOT_TIMEOUT);
				} else {
					mHandler.post(mShowSplashRunnable);
				}
			}
		}
	}
	
	private Runnable mShowSplashRunnable = new Runnable() {

		public void run() {
			// Show configuration splash
			showSplashScreen();
		}
		
	};

	public void showSplashScreen() {
		if (DEBUG) Log.d(TAG, "Showing splash screen...");
		Intent intent = new Intent(this, TeclaSplash.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	public Boolean isVoiceInputSupported() {
		// Check to see if a speech recognition app is present
		if (mPackageManager.queryIntentActivities(
				  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0).size() != 0) {
			return true;
		}
		return false;
	}

	public Boolean isVoiceActionsInstalled() {
		// Check to see if voice actions is installed
		Intent intent = new Intent();
		intent.setComponent(new
		    ComponentName(PACKAGE_VOICE_SEARCH, PACKAGE_VOICE_SEARCH + ".RecognitionActivity"));
		if (mPackageManager.queryIntentActivities(intent, 0).size() != 0) {
			return true;
		}
		return false;
	}
	
	public void startVoiceInput(String language_model) {
		if (DEBUG) Log.d(TAG, "Calling voice input...");
		Intent intent = new Intent(this, TeclaVoiceInput.class);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, language_model);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	public void startVoiceDictation(String language_model) {
		if (DEBUG) Log.d(TAG, "Calling voice input...");
		Intent intent = new Intent(this, TeclaVoiceInput.class);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, language_model);
		intent.putExtra("isDictation", 0x56);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	public void startVoiceActions() {
		Log.d(TAG, "Starting voice actions...");
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(new
		    ComponentName(PACKAGE_VOICE_SEARCH, PACKAGE_VOICE_SEARCH + ".RecognitionActivity"));
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e1) {
			Log.e(TeclaApp.TAG, "Voice Search not installed");
			intent.setComponent(new
				    ComponentName(PACKAGE_QUICKSEARCHBOX, PACKAGE_QUICKSEARCHBOX + ".VoiceSearchActivity"));
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e2) {
				Log.e(TeclaApp.TAG, "Quick Search Box not available");
				TeclaApp.getInstance().showToast(R.string.no_voice_actions_installed);
			}
		}
	}
	
//	public void startVoiceCommand() {
//		Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(intent);
//	}
//
	public void broadcastInputViewCreated() {
		synchronized(this) {
			sendBroadcast(new Intent(ACTION_IME_CREATED));
			mIMECreated=true;
		}
	}
	
	public boolean isIMECreated() {
		return mIMECreated;
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

	public void useSpeakerphone() {
		mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		mAudioManager.setSpeakerphoneOn(true);
	}
	
	public void stopUsingSpeakerPhone() {
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		mAudioManager.setSpeakerphoneOn(false);
	}

	public void holdKeyguardLock() {
		if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, "Acquiring keyguard lock...");
		mKeyguardLock.disableKeyguard();
	}
	
	public void releaseKeyguardLock() {
		if (DEBUG) Log.d(TeclaApp.TAG, "Releasing keyguard lock...");
		mKeyguardLock.reenableKeyguard();
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
			if (DEBUG) Log.d(TeclaApp.TAG, "Aquiring temporal wake lock...");
			mWakeLock.acquire(length);
		} else {
			if (DEBUG) Log.d(TeclaApp.TAG, "Aquiring wake lock...");
			mWakeLock.acquire();
		}
		pokeUserActivityTimer();
	}

	public void releaseWakeLock () {
		if (DEBUG) Log.d(TeclaApp.TAG, "Releasing wake lock...");
		mWakeLock.release();
	}
	
	/**
	 * Wakes and unlocks the screen for a minimum of {@link WAKE_LOCK_TIMEOUT} miliseconds
	 */
	public void wakeUnlockScreen() {
		holdKeyguardLock();
		holdWakeLock(WAKE_LOCK_TIMEOUT);
	}

	public void pokeUserActivityTimer () {
		mPowerManager.userActivity(SystemClock.uptimeMillis(), true);
	}

	public Boolean isDefaultIME() {
		String ime_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
		if (ime_id.equals(TECLA_IME_ID)) return true;
		return false;
	}

	public String byte2Hex(int bite) {
		return String.format("0x%02x", bite);
	}
	
	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	public void showToast(int resid) {
		Toast.makeText(this, resid, Toast.LENGTH_LONG).show();
	}
	
	
}
