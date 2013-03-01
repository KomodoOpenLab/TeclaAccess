package ca.idi.tecla.framework;

import java.util.HashMap;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Persistence {

	//public static final String PREF_SHIELD_VERSION = "shield_version";
	public static final String PREF_TEMP_SHIELD_DISCONNECT = "shield_temp_disconnect";
	public static final String PREF_SHIELD_ADDRESS = "shield_address";
	public static final String PREF_CONNECT_TO_SHIELD = "shield_connect";
	public static final String SEND_SHIELD_EVENTS="enable_events_relay";
	public static final String PREF_SPEAKERPHONE_SWITCH = "speakerphone_switch";
	public static final String PREF_MORSE_MODE = "morse_mode";
	public static final String PREF_FULL_RESET_TIMEOUT = "full_reset_timeout";

	public static final int DEFAULT_FULL_RESET_TIMEOUT = 3;

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
	
	private static HashMap<String,String[]> mSwitchMap;
	
	public SharedPreferences shared_prefs;
	public SharedPreferences.Editor prefs_editor;

	public Persistence(Context context) {
		
		shared_prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs_editor = shared_prefs.edit();
		
		mSwitchMap = new HashMap<String,String[]>();
	}
	
	public String getShieldAddress() {
		String mac = shared_prefs.getString(PREF_SHIELD_ADDRESS, "");
		return BluetoothAdapter.checkBluetoothAddress(mac)? mac:null;
	}
	
	public void setShieldAddress(String shieldAddress) {
		prefs_editor.putString(PREF_SHIELD_ADDRESS, shieldAddress);
		prefs_editor.commit();
	}

	public boolean shouldConnectToShield() {
		return shared_prefs.getBoolean(PREF_CONNECT_TO_SHIELD, false);
	}

	public void setConnectToShield(boolean shieldConnect) {
		prefs_editor.putBoolean(PREF_CONNECT_TO_SHIELD, shieldConnect);
		prefs_editor.commit();
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

}
