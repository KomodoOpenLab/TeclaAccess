/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla.util;

import android.content.Context;

public class Persistence extends ca.idi.tecla.framework.Persistence {

	// Deprecated preferences
	// public static final String PREF_SCAN_DELAY_STRING = "scan_delay";
	// public static final String PREF_VARIANTS = "variants";

	public static final String PREF_VOICE_INPUT = "voice_input";
	public static final String PREF_VARIANTS_KEY = "variants_key";
	public static final String PREF_MORSE_MODE = "morse_mode";
	public static final String PREF_MORSE_SHOW_HUD = "morse_show_hud";
	public static final String PREF_MORSE_KEY_MODE = "morse_key_mode";
	public static final String PREF_MORSE_TIME_UNIT = "morse_time_unit";
	public static final String PREF_PERSISTENT_KEYBOARD = "persistent_keyboard";
	public static final String PREF_AUTOHIDE_TIMEOUT = "autohide_timeout";

	public static final String PREF_CONFIGURE_INPUT = "configure_input";

	public static final String PREF_SWITCH_DEFAULT = "switch_default";

	public static final String PREF_FULL_RESET_TIMEOUT = "full_reset_timeout";
	public static final String PREF_FULLSCREEN_SWITCH = "fullscreen_switch";
	public static final String PREF_SPEAKERPHONE_SWITCH = "speakerphone_switch";
	public static final String PREF_SELF_SCANNING = "self_scanning";
	public static final String PREF_INVERSE_SCANNING = "inverse_scanning";
	public static final String PREF_SCAN_DELAY_INT = "scan_delay_int";

	public static final String PREF_EMERGENCY_GPS_SETTING = "emergency_GPS_setting";
	public static final String PREF_EMERGENCY_PHONE_NUMBER = "emergency_phone_number";
	public static final String PREF_EMERGENCY_SMS_NUMBER = "emergency_SMS_number";
	public static final String PREF_EMERGENCY_EMAIL_ADDRESS = "emergency_email_address";

	public static final String PREF_MORSE_REPEAT_INT = "morse_repeat_int";
	public static final String DEFAULT_MORSE_KEY_MODE = "0";
	public static final int DEFAULT_MORSE_TIME_UNIT = 150;
	public static final int DEFAULT_FULL_RESET_TIMEOUT = 3;
	public static final int MIN_FULL_RESET_TIMEOUT = 3;
	public static final int MAX_FULL_RESET_TIMEOUT = 40;
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

	public static final String CONNECT_TO_PC = "enable_desktop_connectivity";
	public static final String SET_PASSWORD = "set_password";

	private boolean mScreenOn, mInverseScanningChanged, mVariantsKeyOn,
			mVariantsShowing;
	private boolean mAltNavKeyboardOn, mRepeatLockOn, isRepeatingKey;

	public Persistence(Context context) {
		super(context);

		mVariantsShowing = false;
		mAltNavKeyboardOn = false;
		mVariantsKeyOn = false;
		mRepeatLockOn = false;
		isRepeatingKey = false;
		mScreenOn = false;
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

	public boolean isRepeatingKey() {
		return isRepeatingKey;
	}

	public void setRepeatingKey(boolean repeat) {
		isRepeatingKey = repeat;
	}

	public boolean isRepeatLockOn() {
		return mRepeatLockOn;
	}

	public void setRepeatLockOn() {
		mRepeatLockOn = true;
	}

	public void setRepeatLockOff() {
		mRepeatLockOn = false;
	}

	public boolean isVariantsKeyOn() {
		return mVariantsKeyOn;
	}

	public void setVariantsKeyOn() {
		mVariantsKeyOn = true;
	}

	public void setVariantsKeyOff() {
		mVariantsKeyOn = false;
	}

	public void setVariantsShowing(boolean showing) {
		mVariantsShowing = showing;
	}

	public boolean isVariantsShowing() {
		return mVariantsShowing;
	}

	public boolean isAltNavKeyboardOn() {
		return mAltNavKeyboardOn;
	}

	public void setAltNavKeyboardOn(boolean state) {
		mAltNavKeyboardOn = state;
	}

	public boolean isPersistentKeyboardEnabled() {
		return shared_prefs.getBoolean(PREF_PERSISTENT_KEYBOARD, false);
	}

	public void setNavigationKeyboardTimeout(int timeout) {
		prefs_editor.putInt(PREF_AUTOHIDE_TIMEOUT, timeout);
		prefs_editor.commit();
	}

	public void setNeverHideNavigationKeyboard() {
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
		return shared_prefs.getInt(PREF_FULL_RESET_TIMEOUT,
				DEFAULT_FULL_RESET_TIMEOUT);
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
		return isSelfScanningEnabled() || isInverseScanningEnabled();
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

	public void setRepeatFrequency(int delay) {
		prefs_editor.putInt(PREF_MORSE_REPEAT_INT, delay);
		prefs_editor.commit();
	}

	public int getRepeatFrequency() {
		return shared_prefs.getInt(PREF_MORSE_REPEAT_INT, DEFAULT_REPEAT_FREQ);
	}

	public int getMorseKeyMode() {
		// getInt does not work with ListPreference and string type arrays,
		// so use getString instead
		return Integer.parseInt(shared_prefs.getString(PREF_MORSE_KEY_MODE,
				DEFAULT_MORSE_KEY_MODE));
	}

	public void setMorseTimeUnit(int speed) {
		prefs_editor.putInt(PREF_MORSE_TIME_UNIT, speed);
		prefs_editor.commit();
	}

	public int getMorseTimeUnit() {
		return shared_prefs.getInt(PREF_MORSE_TIME_UNIT,
				DEFAULT_MORSE_TIME_UNIT);
	}

	public Boolean getEmergencyGPSSetting() {
		return shared_prefs.getBoolean(PREF_EMERGENCY_GPS_SETTING, false);
	}

	public String getEmergencyPhoneNumber() {
		return shared_prefs.getString(PREF_EMERGENCY_PHONE_NUMBER, "");
	}

	public String getEmergencySMSNumber() {
		return shared_prefs.getString(PREF_EMERGENCY_SMS_NUMBER, "");
	}

	public String getEmergencyEmailAddress() {
		return shared_prefs.getString(PREF_EMERGENCY_EMAIL_ADDRESS, "");
	}
}
