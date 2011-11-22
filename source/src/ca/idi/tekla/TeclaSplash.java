/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla;

import ca.idi.tecla.sdk.SepManager;
import ca.idi.tekla.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class TeclaSplash extends Activity
		implements OnFocusChangeListener {

	/**
	 * Tag used for logging in this class
	 */
	private static final String CLASS_TAG = "Splash: ";
	
	private TextView mSplashText;
	private boolean mIMECreated, mConnectToShieldCalled, mStartFullscreenSwitchCalled;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (TeclaApp.DEBUG) android.os.Debug.waitForDebugger();

		init();
	}
	
	private void init() {
		setContentView(R.layout.activity_splash);
		mSplashText = (TextView) findViewById(R.id.splash_text);

		TeclaApp.getInstance().wakeUnlockScreen();
		TeclaApp.persistence.setScreenOn();

		synchronized(TeclaApp.getInstance()) {
			mIMECreated = TeclaApp.getInstance().isIMECreated();
			if (!mIMECreated) {
				// Register only if ACTION_IME_CREATED has not already been broadcast
				registerReceiver(mReceiver, new IntentFilter(TeclaApp.ACTION_IME_CREATED));
			}
		}
		mSplashText.setOnFocusChangeListener(this);
		mSplashText.clearFocus();
		mSplashText.requestFocus();
		
		mConnectToShieldCalled = false;
		mStartFullscreenSwitchCalled = false;
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (mIMECreated) {
			//ACTION_IME_CREATED has already been broadcast. So
			// effect the broadcast ourselves:
			mReceiver.onReceive(this, new Intent(TeclaApp.ACTION_IME_CREATED));
			//and we won't want to unregister, either:
			mReceiver=null;
		}
		TeclaApp.getInstance().requestShowIMEView(4000);
		Log.d(TeclaApp.TAG, CLASS_TAG + "Splash started.");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.w(TeclaApp.TAG, CLASS_TAG + "Ignoring duplicate call to show Splash");
	}

	// All intents will be processed here
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(TeclaApp.ACTION_IME_CREATED)) {
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Closing splash screen...");
				// Soft IME showing, process the rest of the preferences
				connectToShield();
				startFullscreenSwitch();
				finish();
			}
		}
		
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mReceiver != null)
			unregisterReceiver(mReceiver);
		// Sometimes IME hides so show it again!
		TeclaApp.getInstance().requestShowIMEView();
		if (!mConnectToShieldCalled) {
			connectToShield();
		}
		if (!mStartFullscreenSwitchCalled) {
			startFullscreenSwitch();
		}
	}

	private void connectToShield() {
		mConnectToShieldCalled = true;
		if (TeclaApp.persistence.shouldConnectToShield()) {
			if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Starting SEP...");
			SepManager.start(this);
		}
	}

	private void startFullscreenSwitch() {
		mStartFullscreenSwitchCalled = true;
		if (TeclaApp.persistence.isFullscreenSwitchEnabled()) {
			if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Starting fullscreen switch mode");
			TeclaApp.getInstance().startFullScreenSwitchMode();
		}
	}

	public void onFocusChange(View v, boolean hasFocus) {

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		InputMethodManager mImeManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		mImeManager.toggleSoftInputFromWindow(mSplashText.getWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
		mImeManager.toggleSoftInputFromWindow(mSplashText.getWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

	}

}
