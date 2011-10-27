/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Persistence {
	
	// Deprecated preferences
	//public static final String PREF_SCAN_DELAY_STRING = "scan_delay";
	//public static final String PREF_SHIELD_VERSION = "shield_version";

	public static final String PREF_VOICE_INPUT = "voice_input";
	public static final String PREF_PERSISTENT_KEYBOARD = "persistent_keyboard";
	public static final String PREF_CONNECT_TO_SHIELD = "shield_connect";
	public static final String PREF_SHIELD_ADDRESS = "shield_address";
	public static final String PREF_FULLSCREEN_SWITCH = "fullscreen_switch";
	public static final String PREF_SELF_SCANNING = "self_scanning";
	public static final String PREF_INVERSE_SCANNING = "inverse_scanning";
	public static final String PREF_SCAN_DELAY_INT = "scan_delay_int";
	public static final int DEFAULT_SCAN_DELAY = 1000;
	public static final int MAX_SCAN_DELAY = 3000;
	public static final int MIN_SCAN_DELAY = 250;
	
	private boolean mScreenOn, mInverseScanningChanged;
	
	private SharedPreferences shared_prefs;
	private SharedPreferences.Editor prefs_editor;
	
	public Persistence(Context context) {
		
		shared_prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs_editor = shared_prefs.edit();
		
	}
	
	public boolean isScreenOn() {
		return mScreenOn;
	}
	
	public void setScreenOn() {
		mScreenOn = true;
	}
	
	public void setScreenOff() {
		mScreenOn = false;
	}
	
	public boolean isInverseScanningChanged() {
		return mInverseScanningChanged;
	}
	
	public void setInverseScanningChanged() {
		mInverseScanningChanged = true;
	}
	
	public void unsetInverseScanningChanged() {
		mInverseScanningChanged = false;
	}
	
	public boolean isVoiceInputEnabled() {
		return shared_prefs.getBoolean(PREF_VOICE_INPUT, false);
	}

	public boolean isPersistentKeyboardEnabled() {
		return shared_prefs.getBoolean(PREF_PERSISTENT_KEYBOARD, false);
	}

	public boolean shouldConnectToShield() {
		return shared_prefs.getBoolean(PREF_CONNECT_TO_SHIELD, false);
	}

	public void setConnectToShield(boolean shieldConnect) {
		prefs_editor.putBoolean(PREF_CONNECT_TO_SHIELD, shieldConnect);
		prefs_editor.commit();
	}

	public String getShieldAddress() {
		String mac = shared_prefs.getString(PREF_SHIELD_ADDRESS, "");
		return BluetoothAdapter.checkBluetoothAddress(mac)? mac:null;
	}

	public void setShieldAddress(String shieldAddress) {
		prefs_editor.putString(PREF_SHIELD_ADDRESS, shieldAddress);
		prefs_editor.commit();
	}

	public boolean isFullscreenSwitchEnabled() {
		return shared_prefs.getBoolean(PREF_FULLSCREEN_SWITCH, false);
	}

	public boolean isSelfScanningEnabled() {
		return shared_prefs.getBoolean(PREF_SELF_SCANNING, false);
	}

	public boolean isInverseScanningEnabled() {
		return shared_prefs.getBoolean(PREF_INVERSE_SCANNING, false);
	}

	public boolean isScanningEnabled() {
		return  isSelfScanningEnabled() || isInverseScanningEnabled();
	}

	public int getScanDelay() {
		return shared_prefs.getInt(PREF_SCAN_DELAY_INT, DEFAULT_SCAN_DELAY);
	}

	public void setScanDelay(int delay) {
		prefs_editor.putInt(PREF_SCAN_DELAY_INT, delay);
		prefs_editor.commit();
	}

}
