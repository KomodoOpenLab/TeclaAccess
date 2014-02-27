package ca.idi.tecla.framework;

import ca.idi.tecla.sdk.SwitchEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import ca.idi.tecla.framework.TeclaApp;
import ca.idi.tecla.framework.SwitchEventProvider.LocalBinder;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

public class TeclaShieldService extends Service implements Runnable {


	//Constants
	/**
	 * Tags used for logging in this class
	 */
	private static final String CLASS_TAG = "TeclaShieldManager";

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
	private static final long DEBOUNCE_TIMEOUT = 20; // milliseconds

	private Boolean mIsBroadcasting, mServiceStarted;
	private Thread mMainThread;

	private boolean mKeepReconnecting;
	private int mPingCounter;
	private Handler mHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		initSEP();
	}

	public void initSEP() {
		TeclaStatic.logD(CLASS_TAG, "Creating SEP...");

		// Bind to TeclaService
		Intent intent = new Intent(this, SwitchEventProvider.class);
		bindService(intent, mSEPConnection, Context.BIND_AUTO_CREATE);

		mHandler = new Handler();
		mIsBroadcasting = false;
		mPrevSwitchStates = SwitchEvent.SWITCH_STATES_DEFAULT;
		mServiceStarted = false;

		mShyCounter = 0;
		mBoldCounter = 0;
		mIsBold = false;
	}

	public void stopShieldService() {
		if (mBound) {
			unbindService(mSEPConnection);
		}
		stopMainThread();
		TeclaStatic.logI(CLASS_TAG, "Stopping Shield Service");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopShieldService();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		String shieldAddress = null;
		boolean success = false;

		if (!mServiceStarted) {
			if (intent.hasExtra(TeclaShieldManager.EXTRA_SHIELD_ADDRESS)) {
				shieldAddress = intent.getExtras().getString(TeclaShieldManager.EXTRA_SHIELD_ADDRESS);
			}

			if (!BluetoothAdapter.checkBluetoothAddress(shieldAddress)) {
				// MAC is invalid, try saved address
				shieldAddress = TeclaApp.persistence.getShieldAddress();
			}
			if (shieldAddress != null) {
				// MAC is valid
				success = true;
				// Save shield info
				TeclaApp.persistence.setShieldAddress(shieldAddress);
				startMainThread();
			} else {
				// MAC is invalid, unset connect to shield preference
				TeclaApp.persistence.setConnectToShield(false);
				TeclaStatic.logE(CLASS_TAG, "Could not connect to shield");
			}

			if (success) {
				TeclaStatic.logD(CLASS_TAG, "Successfully started service");
				mServiceStarted = true;
			} else {
				TeclaStatic.logE(CLASS_TAG, "Failed to start service");
			}
		} else {
			TeclaStatic.logW(CLASS_TAG, "SEP already started, ignored start command.");
			success = true;
		}

		return success? Service.START_STICKY:Service.START_NOT_STICKY;
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
			TeclaStatic.logI(CLASS_TAG, "Attempting connection to TeclaShield: " + shieldAddress);
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
					TeclaStatic.logE(CLASS_TAG, "Error getting streams: " + e.getMessage());
				}

				if (gotStreams) {
					TeclaApp.getInstance().wakeUnlockScreen();

					mPingCounter = 0;
					pingShield(500);

					broadcastShieldConnected();
					mIsBroadcasting = true;
					while(mIsBroadcasting) {
						try {
							inByte = mInStream.read();
							TeclaStatic.logV(CLASS_TAG, "Byte received: " +
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
							TeclaStatic.logE(CLASS_TAG, "BroadcastingLoop: " + e.getMessage());
							mIsBroadcasting = false;
							e.printStackTrace();
						}
					}
					broadcastShieldDisconnected();
					TeclaStatic.logW(CLASS_TAG, "Disconnected from Tecla Shield");
					TeclaApp.getInstance().wakeUnlockScreen();
					//Need to toast on a separate thread!
					mHandler.post(new Runnable () {
						public void run() {
							TeclaApp.getInstance().showToast(R.string.shield_disconnected);
						}
					});
				}
			}
			if (mKeepReconnecting) {
				long delay = SHIELD_RECONNECT_DELAY;
				Log.i(CLASS_TAG, "Connection will be attempted in " + delay + " miliseconds.");
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					TeclaStatic.logE(CLASS_TAG, e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	private Runnable mDebounceRunnable = new Runnable () {

		public void run() {
			TeclaStatic.logD(CLASS_TAG, "Filtered switch event received");
			TeclaApp.getInstance().cancelFullReset();

			int switchChanges = mPrevSwitchStates ^ mSwitchStates; // Sets bits of switch states that changed

			// Save switch states for next time
			mPrevSwitchStates = mSwitchStates;

			//FIXME: Temporal work-around for compatibility with mono plugs
			if ((switchChanges & SwitchEvent.MASK_SWITCH_E2) != SwitchEvent.MASK_SWITCH_E2) {
				mSwitchStates |= SwitchEvent.MASK_SWITCH_E2;
			}

			switch_event_provider.injectSwitchEvent(switchChanges, mSwitchStates);

			if (mSwitchStates != SwitchEvent.SWITCH_STATES_DEFAULT) {
				if(!TeclaApp.persistence.isMorseModeEnabled()) {
					//Disables sending a category.HOME intent when
					//using Morse repeat-on-switch-down
					long fullResetDelay=TeclaApp.persistence.getFullResetTimeout();
					TeclaApp.getInstance().postDelayedFullReset(fullResetDelay);
				}
			}

		}

	};

	private void killSocket() {
		mHandler.removeCallbacks(mPingingRunnable);
		if (mBluetoothSocket != null) {
			// Close socket if it still exists
			try {
				mBluetoothSocket.close();
				TeclaStatic.logD(CLASS_TAG, "Socket closed");
			} catch (IOException e) {
				TeclaStatic.logE(CLASS_TAG, "killSocket: " + e.getMessage());
				e.printStackTrace();
			}
			mBluetoothSocket = null;
		}
		TeclaStatic.logD(CLASS_TAG, "Socket killed");
	}

	/**
	 * Connects to bluetooth server.
	 */
	private boolean openSocket(String shieldAddress) {

		Boolean success = false;

		if (TeclaApp.bluetooth_adapter != null && TeclaApp.bluetooth_adapter.isEnabled()) {
			TeclaStatic.logD(CLASS_TAG, "Attempting to open socket to " + shieldAddress + "...");

			BluetoothDevice teclaShield;
			teclaShield = TeclaApp.bluetooth_adapter.getRemoteDevice(shieldAddress);

			if (!success) {
				killSocket();
				// Try usual method
				TeclaStatic.logD(CLASS_TAG, "Creating bluetooth serial socket...");
				try {
					mBluetoothSocket = teclaShield.createRfcommSocketToServiceRecord(SPP_UUID);
				} catch (IOException e) {
					TeclaStatic.logE(CLASS_TAG, "openSocket: " + e.getMessage());
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
					TeclaStatic.logV(CLASS_TAG, "Will not attempt to open bluetooth serial socket with reflection");
				}
			}
			if (!success) {
				killSocket(); //Still no success, kill socket
				Log.i(CLASS_TAG, "Could not open socket");
			}
		} else {
			TeclaStatic.logW(CLASS_TAG, "Can't open socket. Bluetooth is disabled.");
		}
		return success;
	}

	private boolean createSocketWithReflection(BluetoothDevice teclaShield) {
		// Try using reflection
		TeclaStatic.logW(CLASS_TAG, "Creating bluetooth serial socket using reflection...");
		killSocket();
		Method m = null;
		try {
			m = teclaShield.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
			mBluetoothSocket = (BluetoothSocket) m.invoke(teclaShield, 1);
		} catch (SecurityException e) {
			TeclaStatic.logE(CLASS_TAG, "openSocket with reflection: " + e.getMessage());
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			TeclaStatic.logE(CLASS_TAG, "openSocket with reflection: " + e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			TeclaStatic.logE(CLASS_TAG, "openSocket with reflection: " + e.getMessage());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			TeclaStatic.logE(CLASS_TAG, "openSocket with reflection: " + e.getMessage());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			TeclaStatic.logE(CLASS_TAG, "openSocket with reflection: " + e.getMessage());
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
			TeclaStatic.logD(CLASS_TAG, "Connected to " + mBluetoothSocket.getRemoteDevice().getAddress());
			return true;
		} catch (IOException e) {
			TeclaStatic.logE(CLASS_TAG, "connectSocket: " + e.getMessage());
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
				TeclaStatic.logE(CLASS_TAG, "Shield connection timed out!");
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
			TeclaStatic.logE(CLASS_TAG, "writeToShield: " + e.getMessage());
			killSocket();
			e.printStackTrace();
		}
	}

	private void broadcastShieldConnected() {
		TeclaStatic.logD(CLASS_TAG, "Broadcasting Shield connected intent...");
		sendBroadcast(new Intent(ACTION_SHIELD_CONNECTED));
	}

	private void broadcastShieldDisconnected() {
		TeclaStatic.logD(CLASS_TAG, "Broadcasting Shield disconnected intent...");
		sendBroadcast(new Intent(ACTION_SHIELD_DISCONNECTED));
	}

	SwitchEventProvider switch_event_provider;
	boolean mBound = false;

	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mSEPConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            switch_event_provider = binder.getService();
            mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
			
		}
    };

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
