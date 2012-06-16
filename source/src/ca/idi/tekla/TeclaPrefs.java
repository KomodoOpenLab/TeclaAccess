/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ca.idi.tekla;

//FIXME: Tecla Access - Solve backup elsewhere
//import android.backup.BackupManager;
import java.util.HashMap;

import ca.idi.tecla.sdk.SepManager;
import ca.idi.tekla.R;
import ca.idi.tekla.ime.TeclaIME;
import ca.idi.tekla.sep.SwitchEventProvider;
import ca.idi.tekla.util.NavKbdTimeoutDialog;
import ca.idi.tekla.util.Persistence;
import ca.idi.tekla.util.ScanSpeedDialog;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.AutoText;
import android.util.Log;

public class TeclaPrefs extends PreferenceActivity
implements SharedPreferences.OnSharedPreferenceChangeListener {

	/**
	 * Tag used for logging in this class
	 */
	private static final String CLASS_TAG = "Prefs: ";
	private static final String QUICK_FIXES_KEY = "quick_fixes";
	private static final String SHOW_SUGGESTIONS_KEY = "show_suggestions";
	private static final String PREDICTION_SETTINGS_KEY = "prediction_settings";

	private CheckBoxPreference mQuickFixes;
	private CheckBoxPreference mShowSuggestions;
	private CheckBoxPreference mPrefVoiceInput;
	private CheckBoxPreference mPrefVariantsKey;

	private CheckBoxPreference mPrefMorse;
	private CheckBoxPreference mPrefPersistentKeyboard;
	private Preference mPrefAutohideTimeout;
	private CheckBoxPreference mPrefConnectToShield;
	private CheckBoxPreference mPrefFullScreenSwitch;
	private CheckBoxPreference mPrefSelfScanning;
	private CheckBoxPreference mPrefInverseScanning;
	private ProgressDialog mProgressDialog;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mShieldFound;
	private String mShieldAddress, mShieldName;
	
	private ScanSpeedDialog mScanSpeedDialog;
	private NavKbdTimeoutDialog mAutohideTimeoutDialog;
	private PreferenceScreen mConfigureInputScreen;
	private ListPreference mSwitchJ1;
	private ListPreference mSwitchJ2;
	private ListPreference mSwitchJ3;
	private ListPreference mSwitchJ4;
	private ListPreference mSwitchE1;
	private ListPreference mSwitchE2;
	private HashMap<String, String> mSwitchMap;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (TeclaApp.DEBUG) android.os.Debug.waitForDebugger();
		
		init();
		
	}
	
	private void init() {

		addPreferencesFromResource(R.layout.activity_prefs);
		mQuickFixes = (CheckBoxPreference) findPreference(QUICK_FIXES_KEY);
		mShowSuggestions = (CheckBoxPreference) findPreference(SHOW_SUGGESTIONS_KEY);
		mPrefVoiceInput = (CheckBoxPreference) findPreference(Persistence.PREF_VOICE_INPUT);
		mPrefVariantsKey = (CheckBoxPreference) findPreference(Persistence.PREF_VARIANTS_KEY);
		mPrefMorse = (CheckBoxPreference) findPreference(Persistence.PREF_MORSE_MODE);
		mPrefPersistentKeyboard = (CheckBoxPreference) findPreference(Persistence.PREF_PERSISTENT_KEYBOARD);
		mPrefAutohideTimeout = (Preference) findPreference(Persistence.PREF_AUTOHIDE_TIMEOUT);
		mAutohideTimeoutDialog = new NavKbdTimeoutDialog(this);
		mAutohideTimeoutDialog.setContentView(R.layout.dialog_autohide_timeout);
		mPrefConnectToShield = (CheckBoxPreference) findPreference(Persistence.PREF_CONNECT_TO_SHIELD);
		mPrefFullScreenSwitch = (CheckBoxPreference) findPreference(Persistence.PREF_FULLSCREEN_SWITCH);
		mPrefSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_SELF_SCANNING);
		mPrefInverseScanning = (CheckBoxPreference) findPreference(Persistence.PREF_INVERSE_SCANNING);
		mScanSpeedDialog = new ScanSpeedDialog(this);
		mScanSpeedDialog.setContentView(R.layout.dialog_scan_speed);
		mProgressDialog = new ProgressDialog(this);
		mConfigureInputScreen = (PreferenceScreen) findPreference(Persistence.PREF_CONFIGURE_INPUT);
		mSwitchJ1 = (ListPreference) findPreference(Persistence.PREF_SWITCH_J1);
		mSwitchJ2 = (ListPreference) findPreference(Persistence.PREF_SWITCH_J2);
		mSwitchJ3 = (ListPreference) findPreference(Persistence.PREF_SWITCH_J3);
		mSwitchJ4 = (ListPreference) findPreference(Persistence.PREF_SWITCH_J4);
		mSwitchE1 = (ListPreference) findPreference(Persistence.PREF_SWITCH_E1);
		mSwitchE2 = (ListPreference) findPreference(Persistence.PREF_SWITCH_E2);

		// DETERMINE WHICH PREFERENCES SHOULD BE ENABLED
		// If Tecla Access IME is not selected disable all alternative input preferences
		if (!TeclaApp.getInstance().isDefaultIME()) {
			//Tecla Access is not selected
			mPrefPersistentKeyboard.setEnabled(false);
			mPrefAutohideTimeout.setEnabled(false);
			mPrefFullScreenSwitch.setEnabled(false);
			mPrefConnectToShield.setEnabled(false);
			mPrefSelfScanning.setEnabled(false);
			mPrefInverseScanning.setEnabled(false);
			mPrefMorse.setEnabled(false);
			TeclaApp.getInstance().showToast(R.string.tecla_notselected);
		}

		// If no voice apps available, disable voice input
		if (!(TeclaApp.getInstance().isVoiceInputSupported() && 
				TeclaApp.getInstance().isVoiceActionsInstalled())) {
			if (mPrefVoiceInput.isChecked()) mPrefVoiceInput.setChecked(false);
			mPrefVoiceInput.setEnabled(false);
			mPrefVoiceInput.setSummary(R.string.no_voice_input_available);
		}
		
		// If Bluetooth disabled or unsupported disable Shield connection
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			mPrefConnectToShield.setSummary(R.string.shield_connect_summary_BT_nosupport);
			mPrefConnectToShield.setEnabled(false);
		} else if (!mBluetoothAdapter.isEnabled()) {
			mPrefConnectToShield.setSummary(R.string.shield_connect_summary_BT_disabled);
			mPrefConnectToShield.setEnabled(false);
		} else {
			mPrefConnectToShield.setSummary(R.string.shield_connect_summary);
		}

		// If no alternative input selected, disable scanning
		if (!mPrefConnectToShield.isChecked() && !mPrefFullScreenSwitch.isChecked()) {
			mPrefSelfScanning.setEnabled(false);
			mPrefInverseScanning.setEnabled(false);
		}
		
		//TODO Elyas: Set default switch actions + reset to default button
		mSwitchJ1.setSummary(mSwitchJ1.getEntry());
		mSwitchJ2.setSummary(mSwitchJ2.getEntry());
		mSwitchJ3.setSummary(mSwitchJ3.getEntry());
		mSwitchJ4.setSummary(mSwitchJ4.getEntry());
		mSwitchE1.setSummary(mSwitchE1.getEntry());
		mSwitchE2.setSummary(mSwitchE2.getEntry());
		
		//Initialize switch map according to prefs
		mSwitchMap = TeclaApp.persistence.getSwitchMap();
		
		if (mSwitchMap.isEmpty()) {
			mSwitchMap.put(mSwitchJ1.getKey(), mSwitchJ1.getValue());
			mSwitchMap.put(mSwitchJ2.getKey(), mSwitchJ2.getValue());
			mSwitchMap.put(mSwitchJ3.getKey(), mSwitchJ3.getValue());
			mSwitchMap.put(mSwitchJ4.getKey(), mSwitchJ4.getValue());
			mSwitchMap.put(mSwitchE1.getKey(), mSwitchE1.getValue());
			mSwitchMap.put(mSwitchE2.getKey(), mSwitchE2.getValue());
		}

		//Tecla Access Intents & Intent Filters
		registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		registerReceiver(mReceiver, new IntentFilter(SwitchEventProvider.ACTION_SHIELD_CONNECTED));
		registerReceiver(mReceiver, new IntentFilter(SwitchEventProvider.ACTION_SHIELD_DISCONNECTED));

		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(TeclaPrefs.this);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		int autoTextSize = AutoText.getSize(getListView());
		if (autoTextSize < 1) {
			((PreferenceGroup) findPreference(PREDICTION_SETTINGS_KEY))
			.removePreference(mQuickFixes);
		} else {
			mShowSuggestions.setDependency(QUICK_FIXES_KEY);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
				TeclaPrefs.this);
	}

	private void discoverShield() {
		mShieldFound = false;
		if (mBluetoothAdapter.isDiscovering())
			mBluetoothAdapter.cancelDiscovery();
		mBluetoothAdapter.startDiscovery();
		showDiscoveryDialog();
	}

	// All intents will be processed here
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND) && !mShieldFound) {
				BluetoothDevice dev = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
				if ((dev.getName() != null) && (
						dev.getName().startsWith(SwitchEventProvider.SHIELD_PREFIX_2) ||
						dev.getName().startsWith(SwitchEventProvider.SHIELD_PREFIX_3) )) {
					if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Found a Tecla Access Shield candidate");
					mShieldFound = true;
					mShieldAddress = dev.getAddress(); 
					mShieldName = dev.getName();					if (mBluetoothAdapter.isDiscovering())
						mBluetoothAdapter.cancelDiscovery();
				}
			}

			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				if (mShieldFound) {
					// Shield found, try to connect
					if (!mProgressDialog.isShowing())
						mProgressDialog.show();
					mProgressDialog.setMessage(getString(R.string.connecting_tecla_shield) +
							" " + mShieldName);
					if(!SepManager.start(TeclaPrefs.this, mShieldAddress)) {
						// Could not connect to switch
						closeDialog();
						TeclaApp.getInstance().showToast(R.string.couldnt_connect_shield);
					}
				} else {
					// Shield not found
					closeDialog();
					mPrefConnectToShield.setChecked(false);
					TeclaApp.getInstance().showToast(R.string.no_shields_inrange);
				}
			}

			if (intent.getAction().equals(SwitchEventProvider.ACTION_SHIELD_CONNECTED)) {
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Successfully started SEP");
				mPrefPersistentKeyboard.setChecked(true);
				closeDialog();
				TeclaApp.getInstance().showToast(R.string.shield_connected);
				// Enable scanning checkboxes so they can be turned on/off
				mPrefSelfScanning.setEnabled(true);
				mPrefInverseScanning.setEnabled(true);
				mPrefMorse.setEnabled(true);
			}

			if (intent.getAction().equals(SwitchEventProvider.ACTION_SHIELD_DISCONNECTED)) {
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "SEP broadcast stopped");
				closeDialog();
			}
		}
	};

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference.getKey().equals(Persistence.PREF_SCAN_DELAY_INT)) {
			mScanSpeedDialog.show();
		}
		if (preference.getKey().equals(Persistence.PREF_AUTOHIDE_TIMEOUT)) {
			mAutohideTimeoutDialog.show();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Persistence.PREF_VOICE_INPUT) || key.equals(Persistence.PREF_VARIANTS_KEY)) {
			if (mPrefPersistentKeyboard.isChecked() || mPrefVariantsKey.isChecked()) {
				if (mPrefPersistentKeyboard.isChecked()) {
					//Reset IME
					TeclaApp.getInstance().requestHideIMEView();
					TeclaApp.getInstance().requestShowIMEView();
				}
			}
		}
		
		if (key.equals(Persistence.PREF_MORSE_MODE)) {
			if (mPrefMorse.isChecked()) {
				TeclaApp.getInstance().showToast(R.string.morse_enabled);
			}
			
		}
		
		if (key.equals(Persistence.PREF_PERSISTENT_KEYBOARD)) {
			if (mPrefPersistentKeyboard.isChecked()) {
				mPrefAutohideTimeout.setEnabled(true);
				// Show keyboard immediately
				TeclaApp.getInstance().requestShowIMEView();
			} else {
				mPrefAutohideTimeout.setEnabled(false);
				mPrefSelfScanning.setChecked(false);
				mPrefSelfScanning.setEnabled(false);
				mPrefInverseScanning.setChecked(false);
				mPrefInverseScanning.setEnabled(false);
				mPrefFullScreenSwitch.setChecked(false);
				mPrefConnectToShield.setChecked(false);
				TeclaApp.getInstance().requestHideIMEView();
			}
		}
		if (key.equals(Persistence.PREF_AUTOHIDE_TIMEOUT)) {
			if (mPrefPersistentKeyboard.isChecked()) {
				// Show keyboard immediately if Tecla Access IME is selected
				TeclaApp.getInstance().requestShowIMEView();
			} else {
				mPrefSelfScanning.setChecked(false);
				mPrefSelfScanning.setEnabled(false);
				mPrefInverseScanning.setChecked(false);
				mPrefInverseScanning.setEnabled(false);
				mPrefFullScreenSwitch.setChecked(false);
				mPrefConnectToShield.setChecked(false);
				TeclaApp.getInstance().requestHideIMEView();
			}
		}
		if (key.equals(Persistence.PREF_CONFIGURE_INPUT)) {
			TeclaApp.getInstance().showToast("Configure input");
		}
		if (key.equals(Persistence.PREF_SWITCH_J1)) {
			mSwitchJ1.setSummary(mSwitchJ1.getEntry());
			mSwitchMap.remove(key);
			mSwitchMap.put(key, mSwitchJ1.getValue());
		}
		if (key.equals(Persistence.PREF_SWITCH_J2)) {
			mSwitchJ2.setSummary(mSwitchJ2.getEntry());
			mSwitchMap.remove(key);
			mSwitchMap.put(key, mSwitchJ2.getValue());
		}
		if (key.equals(Persistence.PREF_SWITCH_J3)) {
			mSwitchJ3.setSummary(mSwitchJ3.getEntry());
			mSwitchMap.remove(key);
			mSwitchMap.put(key, mSwitchJ3.getValue());
		}
		if (key.equals(Persistence.PREF_SWITCH_J4)) {
			mSwitchJ4.setSummary(mSwitchJ4.getEntry());
			mSwitchMap.remove(key);
			mSwitchMap.put(key, mSwitchJ4.getValue());
		}
		if (key.equals(Persistence.PREF_SWITCH_E1)) {
			mSwitchE1.setSummary(mSwitchE1.getEntry());
			mSwitchMap.remove(key);
			mSwitchMap.put(key, mSwitchE1.getValue());
		}
		if (key.equals(Persistence.PREF_SWITCH_E2)) {
			mSwitchE2.setSummary(mSwitchE2.getEntry());
			mSwitchMap.remove(key);
			mSwitchMap.put(key, mSwitchE2.getValue());
		}
		if (key.equals(Persistence.PREF_CONNECT_TO_SHIELD)) {
			if (mPrefConnectToShield.isChecked()) {
				// Connect to shield but also keep connection alive
				discoverShield();
			} else {
				// FIXME: Tecla Access - Find out how to disconnect
				// switch event provider without breaking
				// connection with other potential clients.
				// Should perhaps use Binding?
				closeDialog();
				if (!mPrefFullScreenSwitch.isChecked()) {
					mPrefSelfScanning.setChecked(false);
					mPrefSelfScanning.setEnabled(false);
					mPrefInverseScanning.setChecked(false);
					mPrefInverseScanning.setEnabled(false);
				}
				SepManager.stop(getApplicationContext());
			}
		}
		if (key.equals(Persistence.PREF_FULLSCREEN_SWITCH)) {
			if (mPrefFullScreenSwitch.isChecked()) {
				mPrefPersistentKeyboard.setChecked(true);
				TeclaApp.getInstance().startFullScreenSwitchMode();
				mPrefSelfScanning.setEnabled(true);
				mPrefInverseScanning.setEnabled(true);
				if (!(mPrefSelfScanning.isChecked() || mPrefInverseScanning.isChecked())) {
					mPrefSelfScanning.setChecked(true);
				}
				mPrefAutohideTimeout.setEnabled(false);
				TeclaApp.persistence.setNeverHideNavigationKeyboard();
			} else {
				if (!mPrefConnectToShield.isChecked()) {
					mPrefSelfScanning.setChecked(false);
					mPrefSelfScanning.setEnabled(false);
					mPrefInverseScanning.setChecked(false);
					mPrefInverseScanning.setEnabled(false);
				}
				if (mPrefPersistentKeyboard.isChecked()) {
					mPrefAutohideTimeout.setEnabled(true);
				}
				TeclaApp.getInstance().stopFullScreenSwitchMode();
			}
		}
		if (key.equals(Persistence.PREF_SELF_SCANNING)) {
			if (mPrefSelfScanning.isChecked()) {
				mPrefInverseScanning.setChecked(false);
				TeclaApp.getInstance().startScanningTeclaIME();
			} else {
				TeclaApp.getInstance().stopScanningTeclaIME();
				if (!mPrefInverseScanning.isChecked()) {
					mPrefFullScreenSwitch.setChecked(false);
					if (!mPrefConnectToShield.isChecked()) {
						mPrefSelfScanning.setEnabled(false);
					}
				}
			}
		}
		if (key.equals(Persistence.PREF_INVERSE_SCANNING)) {
			if (mPrefInverseScanning.isChecked()) {
				mPrefSelfScanning.setChecked(false);
				TeclaApp.persistence.setInverseScanningChanged();
			}
		}
		//FIXME: Tecla Access - Solve backup elsewhere
		//(new BackupManager(getApplicationContext())).dataChanged();
	}

	private void showDiscoveryDialog() {
		mProgressDialog = ProgressDialog.show(this, "", 
				getString(R.string.searching_for_shields), true, true);
	}

	private void closeDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

}
