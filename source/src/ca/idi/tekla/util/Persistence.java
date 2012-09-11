/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla.util;

import java.util.HashMap;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Persistence {
	
	// Deprecated preferences
	//public static final String PREF_SCAN_DELAY_STRING = "scan_delay";
	//public static final String PREF_SHIELD_VERSION = "shield_version";

	public static final String PREF_VOICE_INPUT = "voice_input";
	public static final String PREF_VARIANTS = "variants";
	public static final String PREF_VARIANTS_KEY = "variants_key";
	public static final String PREF_MORSE_MODE = "morse_mode";
	public static final String PREF_MORSE_SHOW_HUD = "morse_show_hud";
	public static final String PREF_MORSE_KEY_MODE = "morse_key_mode";
	public static final String PREF_MORSE_TIME_UNIT = "morse_time_unit";
	public static final String PREF_PERSISTENT_KEYBOARD = "persistent_keyboard";
	public static final String PREF_AUTOHIDE_TIMEOUT = "autohide_timeout";
	
	public static final String PREF_CONFIGURE_INPUT = "configure_input";
	public static final String PREF_SWITCH_J1 = "switch_j1";
	public static final String PREF_SWITCH_J2 = "switch_j2";
	public static final String PREF_SWITCH_J3 = "switch_j3";
	public static final String PREF_SWITCH_J4 = "switch_j4";
	public static final String PREF_SWITCH_E1 = "switch_e1";
	public static final String PREF_SWITCH_E2 = "switch_e2";
	
	public static final String PREF_SWITCH_J1_TECLA = "switch_j1_tecla";
	public static final String PREF_SWITCH_J2_TECLA = "switch_j2_tecla";
	public static final String PREF_SWITCH_J3_TECLA = "switch_j3_tecla";
	public static final String PREF_SWITCH_J4_TECLA = "switch_j4_tecla";
	public static final String PREF_SWITCH_E1_TECLA = "switch_e1_tecla";
	public static final String PREF_SWITCH_E2_TECLA = "switch_e2_tecla";
	
	public static final String PREF_SWITCH_J1_MORSE = "switch_j1_morse";
	public static final String PREF_SWITCH_J2_MORSE = "switch_j2_morse";
	public static final String PREF_SWITCH_J3_MORSE = "switch_j3_morse";
	public static final String PREF_SWITCH_J4_MORSE = "switch_j4_morse";
	public static final String PREF_SWITCH_E1_MORSE = "switch_e1_morse";
	public static final String PREF_SWITCH_E2_MORSE = "switch_e2_morse";
	
	public static final String PREF_SWITCH_DEFAULT = "switch_default";
	
	public static final String PREF_FULL_RESET_TIMEOUT = "full_reset_timeout";
	public static final String PREF_CONNECT_TO_SHIELD = "shield_connect";
	public static final String PREF_TEMP_SHIELD_DISCONNECT = "shield_temp_disconnect";
	public static final String PREF_SHIELD_ADDRESS = "shield_address";
	public static final String PREF_FULLSCREEN_SWITCH = "fullscreen_switch";
	public static final String PREF_SPEAKERPHONE_SWITCH = "speakerphone_switch";
	public static final String PREF_SELF_SCANNING = "self_scanning";
	public static final String PREF_INVERSE_SCANNING = "inverse_scanning";
	public static final String PREF_SCAN_DELAY_INT = "scan_delay_int";
	public static final String PREF_MORSE_REPEAT_INT = "morse_repeat_int";
	public static final String DEFAULT_MORSE_KEY_MODE = "0";
	public static final int DEFAULT_MORSE_TIME_UNIT = 150;
	public static final int DEFAULT_FULL_RESET_TIMEOUT = 3;
	public static final int DEFAULT_SCAN_DELAY = 1000;
	public static final int DEFAULT_REPEAT_FREQ = 750;
	public static final int MAX_SCAN_DELAY = 3000;
	public static final int MIN_SCAN_DELAY = 250;
	public static final int MAX_REPEAT_FREQ = 1000;
	public static final int MIN_REPEAT_FREQ = 500;
	public static final int AUTOHIDE_NULL = -999;
	public static final int FULLRESET_NULL = -999;
	public static final int NEVER_AUTOHIDE = -1;
	public static final int NEVER_REPEAT = -1;
	
	
	private boolean mScreenOn, mInverseScanningChanged, mVariantsShowing;
	private static HashMap<String,String[]> mSwitchMap;
	
	private SharedPreferences shared_prefs;
	private SharedPreferences.Editor prefs_editor;
	
	public Persistence(Context context) {
		
		shared_prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs_editor = shared_prefs.edit();
		mVariantsShowing = false;
		mSwitchMap = new HashMap<String,String[]>();
		
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
	
	public boolean isVoiceInputEnabled() {
		return shared_prefs.getBoolean(PREF_VOICE_INPUT, false);
	}

	public boolean isVariantsKeyEnabled() {
		return shared_prefs.getBoolean(PREF_VARIANTS_KEY, false);
	}
	
	public boolean isVariantsOn() {
		return shared_prefs.getBoolean(PREF_VARIANTS, false);
	}
	
	public void setVariantsOn() {
		prefs_editor.putBoolean(PREF_VARIANTS, true);
		prefs_editor.commit();
	}

	public void setVariantsOff() {
		prefs_editor.putBoolean(PREF_VARIANTS, false);
		prefs_editor.commit();
	}
	
	public void setVariantsShowing (boolean showing) {
		mVariantsShowing = showing;
	}

	public boolean isVariantsShowing () {
		return mVariantsShowing;
	}

	public boolean isPersistentKeyboardEnabled() {
		return shared_prefs.getBoolean(PREF_PERSISTENT_KEYBOARD, false);
	}

	public void setNavigationKeyboardTimeout(int timeout) {
		prefs_editor.putInt(PREF_AUTOHIDE_TIMEOUT, timeout);
		prefs_editor.commit();
	}
	
	public void setNeverHideNavigationKeyboard () {
		prefs_editor.putInt(PREF_AUTOHIDE_TIMEOUT, NEVER_AUTOHIDE);
		prefs_editor.commit();
	}

	public int getNavigationKeyboardTimeout() {
		return shared_prefs.getInt(PREF_AUTOHIDE_TIMEOUT, NEVER_AUTOHIDE);
	}
	
	public void setFullResetTimeout(int timeout) {
		prefs_editor.putInt(PREF_FULL_RESET_TIMEOUT, timeout);
		prefs_editor.commit();
	}
	
	public int getFullResetTimeout() {
		return shared_prefs.getInt(PREF_FULL_RESET_TIMEOUT,DEFAULT_FULL_RESET_TIMEOUT);
	}

	public void setConnectToShield(boolean shieldConnect) {
		prefs_editor.putBoolean(PREF_CONNECT_TO_SHIELD, shieldConnect);
		prefs_editor.commit();
	}

	public boolean shouldConnectToShield() {
		return shared_prefs.getBoolean(PREF_CONNECT_TO_SHIELD, false);
	}

	public void setShieldAddress(String shieldAddress) {
		prefs_editor.putString(PREF_SHIELD_ADDRESS, shieldAddress);
		prefs_editor.commit();
	}

	public String getShieldAddress() {
		String mac = shared_prefs.getString(PREF_SHIELD_ADDRESS, "");
		return BluetoothAdapter.checkBluetoothAddress(mac)? mac:null;
	}
	
	public boolean isMorseModeEnabled() {
		return shared_prefs.getBoolean(PREF_MORSE_MODE, false);
	}
	
	public boolean isMorseHudEnabled() {
		return shared_prefs.getBoolean(PREF_MORSE_SHOW_HUD, true);
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

	public void setInverseScanningChanged() {
		mInverseScanningChanged = true;
	}
	
	public void unsetInverseScanningChanged() {
		mInverseScanningChanged = false;
	}
	
	public boolean isInverseScanningChanged() {
		return mInverseScanningChanged;
	}
	
	public boolean isScanningEnabled() {
		return  isSelfScanningEnabled() || isInverseScanningEnabled();
	}

	public boolean isSpeakerphoneEnabled() {
		return shared_prefs.getBoolean(PREF_SPEAKERPHONE_SWITCH, false);
	}

	public void setScanDelay(int delay) {
		prefs_editor.putInt(PREF_SCAN_DELAY_INT, delay);
		prefs_editor.commit();
	}

	public int getScanDelay() {
		return shared_prefs.getInt(PREF_SCAN_DELAY_INT, DEFAULT_SCAN_DELAY);
	}
	
	public HashMap<String,String[]> getSwitchMap() {
		mSwitchMap.clear();
		mSwitchMap.put(PREF_SWITCH_J1,
				new String[]{shared_prefs.getString(PREF_SWITCH_J1_TECLA, "1"),
				shared_prefs.getString(PREF_SWITCH_J1_MORSE, "1")});
		mSwitchMap.put(PREF_SWITCH_J2,
				new String[]{shared_prefs.getString(PREF_SWITCH_J2_TECLA, "2"),
				shared_prefs.getString(PREF_SWITCH_J2_MORSE, "2")});
		mSwitchMap.put(PREF_SWITCH_J3,
				new String[]{shared_prefs.getString(PREF_SWITCH_J3_TECLA, "3"),
				shared_prefs.getString(PREF_SWITCH_J3_MORSE, "3")});
		mSwitchMap.put(PREF_SWITCH_J4,
				new String[]{shared_prefs.getString(PREF_SWITCH_J4_TECLA, "4"),
				shared_prefs.getString(PREF_SWITCH_J4_MORSE, "4")});
		mSwitchMap.put(PREF_SWITCH_E1,
				new String[]{shared_prefs.getString(PREF_SWITCH_E1_TECLA, "4"),
				shared_prefs.getString(PREF_SWITCH_E1_MORSE, "0")});
		mSwitchMap.put(PREF_SWITCH_E2,
				new String[]{shared_prefs.getString(PREF_SWITCH_E2_TECLA, "3"),
				shared_prefs.getString(PREF_SWITCH_E2_MORSE, "0")});
		return mSwitchMap;
	}

	public void setRepeatFrequency(int delay) {
		prefs_editor.putInt(PREF_MORSE_REPEAT_INT, delay);
		prefs_editor.commit();
	}

	public int getRepeatFrequency() {
		return shared_prefs.getInt(PREF_MORSE_REPEAT_INT, DEFAULT_REPEAT_FREQ);
	}
	
	public int getMorseKeyMode() {
		//getInt does not work with ListPreference and string type arrays,
		//so use getString instead
		return Integer.parseInt(shared_prefs.getString(PREF_MORSE_KEY_MODE, DEFAULT_MORSE_KEY_MODE));
	}

	public void setMorseTimeUnit(int speed) {
		prefs_editor.putInt(PREF_MORSE_TIME_UNIT, speed);
		prefs_editor.commit();
	}
	
	public int getMorseTimeUnit() {
		return shared_prefs.getInt(PREF_MORSE_TIME_UNIT, DEFAULT_MORSE_TIME_UNIT);
	}

}
