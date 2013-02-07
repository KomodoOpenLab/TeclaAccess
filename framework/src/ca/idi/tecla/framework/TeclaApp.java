package ca.idi.tecla.framework;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import ca.idi.tecla.framework.Persistence;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.KeyguardManager.KeyguardLock;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class TeclaApp extends Application implements Runnable {

	/**
	 * Tags used for logging in the whole app
	 */
	public static final String TAG = "TeclaFramework";
	private static final String SEP_TAG = "SEP: ";
	/**
	 * Main debug switch, turns on/off debugging for the whole app
	 */
	public static final boolean DEBUG = true;

	private static final int WAKE_LOCK_TIMEOUT = 5000;

	private static TeclaApp instance;
	public static Persistence persistence;
	public static BluetoothAdapter bluetooth_adapter;
	public static NotificationManager notification_manager;

	private PowerManager power_manager;
	private WakeLock wake_lock;
	private KeyguardLock keyguard_lock;
	private AudioManager audio_manager;

	private Handler handler;

	private Boolean screen_on;
	
	/**
	 * "Well-known" Serial Port Profile UUID as specified at:
	 * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord%28java.util.UUID%29
	 */
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final String ACTION_SHIELD_CONNECTED = "ca.idi.tekla.sep.action.SHIELD_CONNECTED";
	public static final String ACTION_SHIELD_DISCONNECTED = "ca.idi.tekla.sep.action.SHIELD_DISCONNECTED";

	public static final String EXTRA_SWITCH_EVENT = "ca.idi.tekla.sep.extra.SWITCH_EVENT";
	
	public static final String SHIELD_PREFIX_2 = "TeklaShield";
	public static final String SHIELD_PREFIX_3 = "TeclaShield";
	
	public static final int NULL_SHIELD_VERSION = -1;

	//TODO: Attach switch events to preferences
	public static final int SWITCH_EVENT_ACTION = 0x10;
	public static final int SWITCH_EVENT_CANCEL = 0x20;
	public static final int SWITCH_EVENT_SCAN_NEXT = 0x40;
	public static final int SWITCH_EVENT_SCAN_PREV = 0x80;
	//TODO: Turn SWITCH_EVENT_RELEASE into EXTRA_SWITCH_STATE
	public static final int SWITCH_EVENT_RELEASE = 160;

	// FLAGS FOR READING SWITCH STATES
	private static final int STATE_DEFAULT = 0x3F;
	private static final int STATE_PING = 0x70;

	private static final int PING_DELAY = 2000; //milliseconds
	private static final int PING_TIMEOUT_COUNTER = 4;

	private static final int SHIELD_RECONNECT_DELAY = 5000; //milliseconds
	private static final int SHY_RECONNECT_ATTEMPTS = 20; // * SHIELD_RECONNECT_DELAY = SHY_RECONNECT_DELAY
	private static final int BOLD_RECONNECT_ATTEMPTS = 2; //
	
	private int mShyCounter, mBoldCounter;
	private boolean mIsBold;

	private BluetoothSocket mBluetoothSocket;
	private OutputStream mOutStream;
	private InputStream mInStream;

	// VARIABLES FOR SWITCH PROCESSING
	// TODO: This variable should be used when new Shield versions are available
	private int mPrevSwitchStates, mSwitchStates;
	private boolean mPhoneRinging;
	private static final long DEBOUNCE_TIMEOUT = 20; // milliseconds

	private Boolean mIsBroadcasting, mServiceStarted;
	private Thread mMainThread;

	private Intent mSwitchEventIntent;

	private boolean mKeepReconnecting;
	private int mPingCounter;
	private Handler mHandler;

	public void initSEP() {
		//if (TeclaApp.DEBUG) android.os.Debug.waitForDebugger();
		if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Creating SEP...");

		//Intents & Intent Filters
		registerReceiver(mReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
		mSwitchEventIntent = new Intent(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED);

		mHandler = new Handler();
		mIsBroadcasting = false;
		mPhoneRinging = false;
		mPrevSwitchStates = STATE_DEFAULT;
		mServiceStarted = false;
		
		mShyCounter = 0;
		mBoldCounter = 0;
		mIsBold = false;
	}
	
	public void stopSEP() {
		stopMainThread();
		unregisterReceiver(mReceiver);
		Log.i(TeclaApp.TAG, SEP_TAG + "Service Stopped");
	}
	
	public void startSEP(String shield_address) {

		boolean success = false;

		if (!mServiceStarted) {
			if (!BluetoothAdapter.checkBluetoothAddress(shield_address)) {
				// MAC is invalid, try saved address
				shield_address = TeclaApp.persistence.getShieldAddress();
			}
			if (shield_address != null) {
				// MAC is valid
				success = true;
				// Save shield info
				TeclaApp.persistence.setShieldAddress(shield_address);
				startMainThread();
			} else {
				// MAC is invalid, unset connect to shield preference
				TeclaApp.persistence.setConnectToShield(false);
				Log.e(TeclaApp.TAG, SEP_TAG + "Could not connect to shield");
			}

			if (success) {
				Log.d(TeclaApp.TAG, SEP_TAG + "Successfully started service");
				mServiceStarted = true;
			} else {
				Log.d(TeclaApp.TAG, SEP_TAG + "Failed to start service");
			}
		} else {
			Log.w(TeclaApp.TAG, SEP_TAG + "SEP already started, ignored start command.");
			success = true;
		}

	}

	private void startMainThread() {
		stopMainThread();
		mKeepReconnecting = true;
		mMainThread = new Thread(this);
		mMainThread.start();
	}
	
	private void stopMainThread() {
		mKeepReconnecting = false;
		if (mMainThread != null) {
			killSocket();
			while (mMainThread.isAlive()) {
				SystemClock.sleep(1); //Wait for the thread to die
			}
			mMainThread = null; //Reset thread
		}
	}
	
	public void run() {
		
		String shieldAddress;
		int inByte;
		boolean gotStreams;

		shieldAddress = TeclaApp.persistence.getShieldAddress();
		while(mKeepReconnecting) {
			Log.i(TeclaApp.TAG, SEP_TAG + "Attempting connection to TeclaShield: " + shieldAddress);
			// The code below is an attempt to poke the bluetooth chip on devices that put it on stand-by when the
			// screen is off (e.g., Samsung Galaxy series). For additional details see
			// https://github.com/jorgesilva/TeclaAccess/issues/11
			if (!mIsBold) {
				mShyCounter++;
				if (mShyCounter >= SHY_RECONNECT_ATTEMPTS) {
					mShyCounter = 0;
					TeclaApp.getInstance().holdWakeLock();
					mIsBold = true;
				}
			} else {
				mBoldCounter++;
				if (mBoldCounter >= BOLD_RECONNECT_ATTEMPTS) {
					mBoldCounter = 0;
					TeclaApp.getInstance().releaseWakeLock();
					mIsBold = false;
				}
			}
			gotStreams = false;
			if (openSocket(shieldAddress)) {
				try {
					mInStream = mBluetoothSocket.getInputStream();
					mOutStream = mBluetoothSocket.getOutputStream();
					gotStreams = true;
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TeclaApp.TAG, SEP_TAG + "Error getting streams: " + e.getMessage());
				}

				if (gotStreams) {
					TeclaApp.getInstance().wakeUnlockScreen();
					showNotification();

					mPingCounter = 0;
					pingShield(500);

					broadcastShieldConnected();
					mIsBroadcasting = true;
					while(mIsBroadcasting) {
						try {
							inByte = mInStream.read();
							if (TeclaApp.DEBUG) Log.v(TeclaApp.TAG, SEP_TAG + "Byte received: " +
									TeclaApp.getInstance().byte2Hex(inByte) + " at " + SystemClock.uptimeMillis());
							if (inByte != 0xffffffff) { // Work-around for Samsung Galaxy 
								if (inByte == STATE_PING) {
									mPingCounter--;
								} else {
									mSwitchStates = inByte;
									// Ignore any changes ocurring before DEBOUNCE_TIMEOUT runs out
									mHandler.removeCallbacks(mDebounceRunnable);
									mHandler.postDelayed(mDebounceRunnable,DEBOUNCE_TIMEOUT);
								}
							}
						} catch (IOException e) {
							Log.e(TeclaApp.TAG, SEP_TAG + "BroadcastingLoop: " + e.getMessage());
							mIsBroadcasting = false;
							e.printStackTrace();
						}
					}
					broadcastShieldDisconnected();
					cancelNotification();
					Log.w(TeclaApp.TAG, SEP_TAG + "Disconnected from Tecla Shield");
					TeclaApp.getInstance().wakeUnlockScreen();
					//Need to toast on a separate thread!
					mHandler.post(new Runnable () {
						public void run() {
							TeclaApp.getInstance().showToast(R.string.shield_connected);
						}
					});
				}
			}
			if (mKeepReconnecting) {
				long delay = SHIELD_RECONNECT_DELAY;
				Log.i(TeclaApp.TAG, SEP_TAG + "Connection will be attempted in " + delay + " miliseconds.");
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					Log.e(TeclaApp.TAG, SEP_TAG + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	private Runnable mDebounceRunnable = new Runnable () {

		public void run() {
			if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Filtered switch event received");
			TeclaApp.getInstance().cancelFullReset();
			
			int switchChanges = mPrevSwitchStates ^ mSwitchStates; // Sets bits of switch states that changed

			// Save switch states for next time
			mPrevSwitchStates = mSwitchStates;

			//FIXME: Temporal work-around for compatibility with mono plugs
			if ((switchChanges & SwitchEvent.SWITCH_E2) != SwitchEvent.SWITCH_E2) {
				mSwitchStates |= SwitchEvent.SWITCH_E2;
			}
			
			handleSwitchEvent(switchChanges, mSwitchStates);

			if (mSwitchStates != STATE_DEFAULT) {
				if(!TeclaApp.persistence.isMorseModeEnabled()) {
					//Disables sending a category.HOME intent when
					//using Morse repeat-on-switch-down
					long fullResetDelay=TeclaApp.persistence.getFullResetTimeout();
					TeclaApp.getInstance().postDelayedFullReset(fullResetDelay);
				}
			}
			
		}

	};

	private void handleSwitchEvent(int switchChanges, int switchStates) {
		if (mPhoneRinging) {
			//Screen should be on
			//Answering should also unlock
			TeclaApp.getInstance().answerCall();
			// Assume phone is not ringing any more
			mPhoneRinging = false;
		} else if (!TeclaApp.persistence.isScreenOn()) {
			// Screen is off, so just wake it
			TeclaApp.getInstance().wakeUnlockScreen();
		} else {
			// In all other instances acquire wake lock,
			// WARNING: just poking user activity timer DOES NOT WORK on gingerbread
			TeclaApp.getInstance().wakeUnlockScreen();
			if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Broadcasting switch event: " +
					TeclaApp.getInstance().byte2Hex(switchChanges) + ":" +
					TeclaApp.getInstance().byte2Hex(switchStates));
			// Reset intent
			mSwitchEventIntent.removeExtra(SwitchEvent.EXTRA_SWITCH_CHANGES);
			mSwitchEventIntent.removeExtra(SwitchEvent.EXTRA_SWITCH_STATES);
			mSwitchEventIntent.putExtra(SwitchEvent.EXTRA_SWITCH_CHANGES, switchChanges);
			mSwitchEventIntent.putExtra(SwitchEvent.EXTRA_SWITCH_STATES, switchStates);
			// Broadcast event
			sendBroadcast(mSwitchEventIntent);
		}
	}

	private void killSocket() {
		mHandler.removeCallbacks(mPingingRunnable);
		if (mBluetoothSocket != null) {
			// Close socket if it still exists
			try {
				mBluetoothSocket.close();
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Socket closed");
			} catch (IOException e) {
				Log.e(TeclaApp.TAG, SEP_TAG + "killSocket: " + e.getMessage());
				e.printStackTrace();
			}
			mBluetoothSocket = null;
		}
		if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Socket killed");
	}
	
	/**
	 * Connects to bluetooth server.
	 */
	private boolean openSocket(String shieldAddress) {
		
		Boolean success = false;

		if (TeclaApp.bluetooth_adapter != null && TeclaApp.bluetooth_adapter.isEnabled()) {
			if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Attempting to open socket to " + shieldAddress + "...");

			BluetoothDevice teclaShield;
			teclaShield = TeclaApp.bluetooth_adapter.getRemoteDevice(shieldAddress);

			if (!success) {
				killSocket();
				// Try usual method
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Creating bluetooth serial socket...");
				try {
					mBluetoothSocket = teclaShield.createRfcommSocketToServiceRecord(SPP_UUID);
				} catch (IOException e) {
					Log.e(TeclaApp.TAG, SEP_TAG + "openSocket: " + e.getMessage());
					e.printStackTrace();
				}
				success = connectSocket();
			}
			
			if (!success) {
				if (    Build.MODEL.equals("ReflectionCompatibleModel") &&
						Build.MANUFACTURER.equals("ReflectionCompatibleManufacturer")) {
					/*
					 * WARNING! Although fast, the reflection method for reconnecting a lost Bluetooth connection
					 * can fail silently and sometimes lock the App and the Bluetooth chip. This method should be
					 * deprecated. Devices know NOT to work with reflection are:
					 *   LG Phoenix (LG-P505R)
					 *   Samsung Galaxy (SGH-T989D)
					 */
					success = createSocketWithReflection(teclaShield);
				} else {
					if (TeclaApp.DEBUG) Log.v(TeclaApp.TAG, SEP_TAG + "Will not attempt to open bluetooth serial socket with reflection");
				}
			}
			if (!success) {
				killSocket(); //Still no success, kill socket
				Log.i(TeclaApp.TAG, SEP_TAG + "Could not open socket");
			}
		} else {
			Log.w(TeclaApp.TAG, SEP_TAG + "Can't open socket. Bluetooth is disabled.");
		}
		return success;
	}

	private boolean createSocketWithReflection(BluetoothDevice teclaShield) {
		// Try using reflection
		Log.w(TeclaApp.TAG, SEP_TAG + "Creating bluetooth serial socket using reflection...");
		killSocket();
		Method m = null;
		try {
			m = teclaShield.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
			mBluetoothSocket = (BluetoothSocket) m.invoke(teclaShield, 1);
		} catch (SecurityException e) {
			Log.e(TeclaApp.TAG, SEP_TAG + "openSocket with reflection: " + e.getMessage());
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			Log.e(TeclaApp.TAG, SEP_TAG + "openSocket with reflection: " + e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			Log.e(TeclaApp.TAG, SEP_TAG + "openSocket with reflection: " + e.getMessage());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			Log.e(TeclaApp.TAG, SEP_TAG + "openSocket with reflection: " + e.getMessage());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			Log.e(TeclaApp.TAG, SEP_TAG + "openSocket with reflection: " + e.getMessage());
			e.printStackTrace();
		}
		return connectSocket();
	}
	
	private boolean connectSocket() {
		try {
			// See http://developer.android.com/reference/android/bluetooth/BluetoothSocket.html#connect%28%29
			// for why the cancelDiscovery() call is necessary
			TeclaApp.bluetooth_adapter.cancelDiscovery();
			mBluetoothSocket.connect();
			Log.d(TeclaApp.TAG, SEP_TAG + "Connected to " + mBluetoothSocket.getRemoteDevice().getAddress());
			return true;
		} catch (IOException e) {
			Log.e(TeclaApp.TAG, SEP_TAG + "connectSocket: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	private void pingShield(long delay) {
		mHandler.removeCallbacks(mPingingRunnable);
		mHandler.postDelayed(mPingingRunnable, delay);
	}
	
	private Runnable mPingingRunnable = new Runnable () {

		public void run() {
			mPingCounter++;
			if (mPingCounter > PING_TIMEOUT_COUNTER) {
				Log.e(TeclaApp.TAG, SEP_TAG + "Shield connection timed out!");
				killSocket();
			} else {
				writeToShield(STATE_PING);
				pingShield(PING_DELAY);
			}
		}

	};

	private void writeToShield(int mByte) {
		try {
			mOutStream.write(mByte);
		} catch (IOException e) {
			Log.e(TeclaApp.TAG, SEP_TAG + "writeToShield: " + e.getMessage());
			killSocket();
			e.printStackTrace();
		}
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the expanded notification
		CharSequence text = getText(R.string.shield_connected);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.tecla_status, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this notification
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//				new Intent(this, TeclaPrefs.class), 0);

		// Set the info for the views that show in the notification panel.
//		notification.setLatestEventInfo(this, getText(R.string.sep_label),
//				text, contentIntent);
		//TODO: Find a way to define the target class on notification click
		notification.setLatestEventInfo(this, getText(R.string.sep_label),
				text,null);

		// Add sound and type.
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;

		// Send the notification.
		// We use a layout id because it is a unique number.  We use it later to cancel.
		TeclaApp.notification_manager.notify(R.string.shield_connected, notification);
	}

	private void broadcastShieldConnected() {
		if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Broadcasting Shield connected intent...");
		sendBroadcast(new Intent(ACTION_SHIELD_CONNECTED));
	}

	private void broadcastShieldDisconnected() {
		if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Broadcasting Shield disconnected intent...");
		sendBroadcast(new Intent(ACTION_SHIELD_DISCONNECTED));
	}

	private void cancelNotification() {
		// Cancel the persistent notification.
		TeclaApp.notification_manager.cancel(R.string.shield_connected);
	}

	public static TeclaApp getInstance() {
		return instance;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		init();
	}
	
	private void init()
	{
		TeclaStatic.logD("TECLA FRAMEWORK STARTING ON " + Build.MODEL + " BY " + Build.MANUFACTURER);

		instance = this;
		persistence = new Persistence(this);

		power_manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wake_lock = power_manager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK |
				PowerManager.ON_AFTER_RELEASE, TeclaApp.TAG);
		bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();
		notification_manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		audio_manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		handler = new Handler();

		screen_on = true;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1)
		{
			screen_on = getScreenState();
		}
		TeclaStatic.logD("Screen on? " + screen_on);
		
		//Intents & Intent Filters
		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		
		TeclaStatic.startTeclaService(this);
	}

	// All intents will be processed here
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Phone state changed");
				mPhoneRinging = false;
				TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
					if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, SEP_TAG + "Phone ringing");
					mPhoneRinging = true;
				}
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				screen_on = false;
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1)
				{
					screen_on = getScreenState();
				}
				TeclaStatic.logD("Screen on? " + screen_on);
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				screen_on = true;
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1)
				{
					screen_on = getScreenState();
				}
				TeclaStatic.logD("Screen on? " + screen_on);
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
			if (DEBUG) Log.d(TeclaApp.TAG, "Aquiring temporal wake lock...");
			wake_lock.acquire(length);
		} else {
			if (DEBUG) Log.d(TeclaApp.TAG, "Aquiring wake lock...");
			wake_lock.acquire();
		}
		pokeUserActivityTimer();
	}

	public void releaseWakeLock () {
		if (DEBUG) Log.d(TeclaApp.TAG, "Releasing wake lock...");
		wake_lock.release();
	}
	
	public void holdKeyguardLock() {
		if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, "Acquiring keyguard lock...");
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
	
	public String byte2Hex(int bite) {
		return String.format("0x%02x", bite);
	}
	
	public void showToast(int resid) {
		Toast.makeText(this, resid, Toast.LENGTH_LONG).show();
	}	
	
}
