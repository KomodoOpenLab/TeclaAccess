package ca.idi.tecla.framework;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Persistence {

	public static final String PREF_SHIELD_ADDRESS = "shield_address";
	public static final String PREF_CONNECT_TO_SHIELD = "shield_connect";
	public static final String PREF_SPEAKERPHONE_SWITCH = "speakerphone_switch";
	public static final String PREF_MORSE_MODE = "morse_mode";
	public static final String PREF_FULL_RESET_TIMEOUT = "full_reset_timeout";

	public static final int DEFAULT_FULL_RESET_TIMEOUT = 3;

	private boolean screen_on;
	
	private SharedPreferences shared_prefs;
	private SharedPreferences.Editor prefs_editor;

	public Persistence(Context context) {
		
		shared_prefs = PreferenceManager.getDefaultSharedPreferences(context);
		screen_on = false;
		
	}
	
	public String getShieldAddress() {
		String mac = shared_prefs.getString(PREF_SHIELD_ADDRESS, "");
		return BluetoothAdapter.checkBluetoothAddress(mac)? mac:null;
	}
	
	public void setShieldAddress(String shieldAddress) {
		prefs_editor.putString(PREF_SHIELD_ADDRESS, shieldAddress);
		prefs_editor.commit();
	}

	public void setConnectToShield(boolean shieldConnect) {
		prefs_editor.putBoolean(PREF_CONNECT_TO_SHIELD, shieldConnect);
		prefs_editor.commit();
	}

	public boolean isScreenOn() {
		return screen_on;
	}
	
	public boolean isSpeakerphoneEnabled() {
		return shared_prefs.getBoolean(PREF_SPEAKERPHONE_SWITCH, false);
	}

	public boolean isMorseModeEnabled() {
		return shared_prefs.getBoolean(PREF_MORSE_MODE, false);
	}
	
	public int getFullResetTimeout() {
		return shared_prefs.getInt(PREF_FULL_RESET_TIMEOUT,DEFAULT_FULL_RESET_TIMEOUT);
	}

}
