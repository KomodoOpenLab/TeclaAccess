/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla.util;

import java.util.LinkedList;
import java.util.List;
import ca.idi.tekla.R;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Persistence {
	
	// Deprecated preferences
	//public static final String PREF_SCAN_DELAY_STRING = "scan_delay";
	//public static final String PREF_SHIELD_VERSION = "shield_version";

	public static final String PREF_VOICE_INPUT = "voice_input";
	public static final String PREF_VARIANTS = "variants";
	public static final String PREF_VARIANTS_KEY = "variants_key";
	public static final String PREF_PERSISTENT_KEYBOARD = "persistent_keyboard";
	public static final String PREF_AUTOHIDE_TIMEOUT = "autohide_timeout";
	public static final String PREF_CONNECT_TO_SHIELD = "shield_connect";
	public static final String PREF_SHIELD_ADDRESS = "shield_address";
	public static final String PREF_FULLSCREEN_SWITCH = "fullscreen_switch";
	public static final String PREF_SELF_SCANNING = "self_scanning";
	public static final String PREF_INVERSE_SCANNING = "inverse_scanning";
	public static final String PREF_SCAN_DELAY_INT = "scan_delay_int";
	public static final String PREF_SWITCH_ACTIONS = "map_switch_actions";
	public static final int DEFAULT_SCAN_DELAY = 1000;
	public static final int MAX_SCAN_DELAY = 3000;
	public static final int MIN_SCAN_DELAY = 250;
	public static final int AUTOHIDE_NULL = -999;
	public static final int NEVER_AUTOHIDE = -1;
	
	private static final String delimiter = "@##@";
	private String[] default_switch_action_map;
	private boolean mScreenOn, mInverseScanningChanged, mVariantsShowing;
	
	private SharedPreferences shared_prefs;
	private SharedPreferences.Editor prefs_editor;
	
	public Persistence(Context context) {
		
		shared_prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs_editor = shared_prefs.edit();
		mVariantsShowing = false;
		default_switch_action_map = context.getResources().getStringArray(R.array.switch_actions);
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

	public void setScanDelay(int delay) {
		prefs_editor.putInt(PREF_SCAN_DELAY_INT, delay);
		prefs_editor.commit();
	}

	public int getScanDelay() {
		return shared_prefs.getInt(PREF_SCAN_DELAY_INT, DEFAULT_SCAN_DELAY);
	}

	public void setSwitchActionMap(String[] switch_actions){
		String str_to_save = "";
		if(switch_actions != null)
			for(int i=0;i<switch_actions.length;i++){
				str_to_save += switch_actions[i] + delimiter;
			}
		else{
			str_to_save = "";
		}
		//Log.d("Persistence : saving map", str_to_save);
		prefs_editor.putString(PREF_SWITCH_ACTIONS, str_to_save);
		prefs_editor.commit();
	}

	public String[] getSwitchActionMap(){
		String str = shared_prefs.getString(PREF_SWITCH_ACTIONS, "");
		//Log.d("Persistence : map recall", str);
		if(str == null || str.equals(""))
			return default_switch_action_map;
		else{
			List<String>list = new LinkedList<String>();
			int i=0,new_index=0;
			while(i<str.length()){
				if((new_index = str.indexOf(delimiter,i))==-1){
					break;
				}
				else{
					list.add(str.substring(i, new_index));
				}
				i = new_index+delimiter.length();
			}
			String[] switch_action_arr = new String[list.size()];
			for(i=0;i<switch_action_arr.length;i++){
				switch_action_arr[i] = list.get(i);
			}
			return switch_action_arr;
		}
	}

}
