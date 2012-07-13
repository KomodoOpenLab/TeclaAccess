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

import ca.idi.tecla.lib.ListPreference;
import ca.idi.tecla.sdk.SepManager;
import ca.idi.tekla.R;
import ca.idi.tekla.sep.SwitchEventProvider;
import ca.idi.tekla.util.DefaultActionsDialog;
import ca.idi.tekla.util.FullResetTimeoutDialog;
import ca.idi.tekla.util.NavKbdTimeoutDialog;
import ca.idi.tekla.util.Persistence;
import ca.idi.tekla.util.RepeatFrequencyDialog;
import ca.idi.tekla.util.ScanSpeedDialog;
import ca.idi.tekla.util.SwitchPreference;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.AutoText;
import android.util.Log;
import android.widget.BaseAdapter;
import android.view.KeyEvent;

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
	private CheckBoxPreference mPrefMorseHUD;
	private CheckBoxPreference mPrefPersistentKeyboard;
	private Preference mPrefMorseKeyMode;
	private ListPreference mPrefMorseTimeUnit;
	private Preference mPrefAutohideTimeout;
	private CheckBoxPreference mPrefConnectToShield;
	private CheckBoxPreference mPrefFullScreenSwitch;
	private CheckBoxPreference mPrefSpeakerPhoneSwitch;
	private CheckBoxPreference mPrefSelfScanning;
	private CheckBoxPreference mPrefInverseScanning;
	private ProgressDialog mProgressDialog;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mShieldFound, mConnectionCancelled;
	private String mShieldAddress, mShieldName;
	
	private ScanSpeedDialog mScanSpeedDialog;
	private RepeatFrequencyDialog mRepeatFrequencyDialog;
	private NavKbdTimeoutDialog mAutohideTimeoutDialog;
	private FullResetTimeoutDialog mFullResetTimeoutDialog;
	private PreferenceScreen mConfigureInputScreen;
	private BaseAdapter mConfigureInputAdapter;
	
	private static SwitchPreference mSwitchJ1;
	private static SwitchPreference mSwitchJ2;
	private static SwitchPreference mSwitchJ3;
	private static SwitchPreference mSwitchJ4;
	private static SwitchPreference mSwitchE1;
	private static SwitchPreference mSwitchE2;
	
	private DefaultActionsDialog mDefaultActionsDialog;
	private static HashMap<String, String[]> mSwitchMap;
		
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		//if (TeclaApp.DEBUG) android.os.Debug.waitForDebugger();
		
		init();
		
	}
	
	private void init() {

		addPreferencesFromResource(R.layout.activity_prefs);
		mQuickFixes = (CheckBoxPreference) findPreference(QUICK_FIXES_KEY);
		mShowSuggestions = (CheckBoxPreference) findPreference(SHOW_SUGGESTIONS_KEY);
		mPrefVoiceInput = (CheckBoxPreference) findPreference(Persistence.PREF_VOICE_INPUT);
		mPrefVariantsKey = (CheckBoxPreference) findPreference(Persistence.PREF_VARIANTS_KEY);
		mPrefMorse = (CheckBoxPreference) findPreference(Persistence.PREF_MORSE_MODE);
		mPrefMorseHUD = (CheckBoxPreference) findPreference(Persistence.PREF_MORSE_SHOW_HUD);
		mPrefMorseKeyMode = (ListPreference) findPreference(Persistence.PREF_MORSE_KEY_MODE);
		mPrefMorseTimeUnit = (ListPreference) findPreference(Persistence.PREF_MORSE_TIME_UNIT);
		mPrefPersistentKeyboard = (CheckBoxPreference) findPreference(Persistence.PREF_PERSISTENT_KEYBOARD);
		mPrefAutohideTimeout = (Preference) findPreference(Persistence.PREF_AUTOHIDE_TIMEOUT);
		mAutohideTimeoutDialog = new NavKbdTimeoutDialog(this);
		mAutohideTimeoutDialog.setContentView(R.layout.dialog_timeout);
		mFullResetTimeoutDialog = new FullResetTimeoutDialog(this);
		mFullResetTimeoutDialog.setContentView(R.layout.dialog_timeout);
		mPrefConnectToShield = (CheckBoxPreference) findPreference(Persistence.PREF_CONNECT_TO_SHIELD);
		mPrefFullScreenSwitch = (CheckBoxPreference) findPreference(Persistence.PREF_FULLSCREEN_SWITCH);
		mPrefSpeakerPhoneSwitch = (CheckBoxPreference) findPreference(Persistence.PREF_SPEAKERPHONE_SWITCH);
		mPrefSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_SELF_SCANNING);
		mPrefInverseScanning = (CheckBoxPreference) findPreference(Persistence.PREF_INVERSE_SCANNING);
		mScanSpeedDialog = new ScanSpeedDialog(this);
		mScanSpeedDialog.setContentView(R.layout.dialog_scan_speed);
		mRepeatFrequencyDialog = new RepeatFrequencyDialog(this);
		mRepeatFrequencyDialog.setContentView(R.layout.dialog_scan_speed);
		mProgressDialog = new ProgressDialog(this);
		mConfigureInputScreen = (PreferenceScreen) findPreference(Persistence.PREF_CONFIGURE_INPUT);
		mConfigureInputAdapter= (BaseAdapter) mConfigureInputScreen.getRootAdapter();
		
		mSwitchJ1 = new SwitchPreference((PreferenceScreen) findPreference(Persistence.PREF_SWITCH_J1), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_J1_TECLA), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_J1_MORSE));
		
		mSwitchJ2 = new SwitchPreference((PreferenceScreen) findPreference(Persistence.PREF_SWITCH_J2), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_J2_TECLA), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_J2_MORSE));
		
		mSwitchJ3 = new SwitchPreference((PreferenceScreen) findPreference(Persistence.PREF_SWITCH_J3), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_J3_TECLA), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_J3_MORSE));
		
		mSwitchJ4 = new SwitchPreference((PreferenceScreen) findPreference(Persistence.PREF_SWITCH_J4), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_J4_TECLA), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_J4_MORSE));
		
		mSwitchE1 = new SwitchPreference((PreferenceScreen) findPreference(Persistence.PREF_SWITCH_E1), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_E1_TECLA), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_E1_MORSE));
		
		mSwitchE2 = new SwitchPreference((PreferenceScreen) findPreference(Persistence.PREF_SWITCH_E2), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_E2_TECLA), 
				(ListPreference) findPreference(Persistence.PREF_SWITCH_E2_MORSE));
		
		mDefaultActionsDialog = new DefaultActionsDialog(this);
		mDefaultActionsDialog.setContentView(R.layout.reset_default);

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
		
		//If Morse pref is disabled, also disable all of the other prefs in the same category
		if (!mPrefMorse.isChecked()) {
			mPrefMorseHUD.setEnabled(false);
			mPrefMorseKeyMode.setEnabled(false);
			mPrefMorseTimeUnit.setEnabled(false);
		}

		// If no voice apps available, disable voice input
		if (!(TeclaApp.getInstance().isVoiceInputSupported() && 
				TeclaApp.getInstance().isVoiceActionsInstalled())) {
			if (mPrefVoiceInput.isChecked()) mPrefVoiceInput.setChecked(false);
			mPrefVoiceInput.setEnabled(false);
			mPrefVoiceInput.setSummary(R.string.no_voice_input_available);
		}
		
		updateShieldPreference();

		// If no alternative input selected, disable scanning
		if (!mPrefConnectToShield.isChecked() && !mPrefFullScreenSwitch.isChecked()) {
			mPrefSelfScanning.setChecked(false);
			mPrefInverseScanning.setChecked(false);
			mPrefSelfScanning.setEnabled(false);
			mPrefInverseScanning.setEnabled(false);
		}
		
		mPrefMorseTimeUnit.setSummary(mPrefMorseTimeUnit.getEntry());
		refreshSwitchesSummary();
		
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
			TeclaApp.getInstance().showToast(R.string.tecla_notselected);
		}

		//Tecla Access Intents & Intent Filters
		registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
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
			//FIXME: Enable when adding suggestions
//			mShowSuggestions.setDependency(QUICK_FIXES_KEY);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		cancelDialog();
		// FIXME: Supposed to force a refresh of preference states, but too aggressive?
		//finish(); 
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
		mConnectionCancelled = false;
		cancelDiscovery();
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
					mShieldFound = true;
					mShieldAddress = dev.getAddress();
					mShieldName = dev.getName();
					if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Found a Tecla Access Shield candidate");
					cancelDiscovery();
				}
			}

			if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				updateShieldPreference();
			}
			
			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				if (mShieldFound) {
					// Shield found, try to connect
					mProgressDialog.setOnCancelListener(null); //Don't do anything if dialog cancelled
					mProgressDialog.setOnKeyListener(new OnKeyListener() {

						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							return true; //Consume all keys once Shield is found (can't cancel with back key)
						}
						
					});
					mProgressDialog.setMessage(getString(R.string.connecting_tecla_shield) +
							" " + mShieldName);
					if(!SepManager.start(TeclaPrefs.this, mShieldAddress)) {
						// Could not connect to Shield
						dismissDialog();
						TeclaApp.getInstance().showToast(R.string.couldnt_connect_shield);
					}
				} else {
					// Shield not found
					dismissDialog();
					if (!mConnectionCancelled) TeclaApp.getInstance().showToast(R.string.no_shields_inrange);
					mPrefConnectToShield.setChecked(false);
				}
			}

			if (intent.getAction().equals(SwitchEventProvider.ACTION_SHIELD_CONNECTED)) {
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Successfully started SEP");
				dismissDialog();
				TeclaApp.getInstance().showToast(R.string.shield_connected);
				// Enable scanning checkboxes so they can be turned on/off
				mPrefSelfScanning.setEnabled(true);
				mPrefInverseScanning.setEnabled(true);
				mPrefMorse.setEnabled(true);
				mPrefPersistentKeyboard.setChecked(true);
			}

			if (intent.getAction().equals(SwitchEventProvider.ACTION_SHIELD_DISCONNECTED)) {
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "SEP broadcast stopped");
				dismissDialog();
			}
		}
	};

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference.getKey().equals(Persistence.PREF_SCAN_DELAY_INT)) {
			mScanSpeedDialog.show();
		}
		if (preference.getKey().equals(Persistence.PREF_REPEAT_DELAY_INT)) {
			mRepeatFrequencyDialog.show();
		}
		if (preference.getKey().equals(Persistence.PREF_AUTOHIDE_TIMEOUT)) {
			mAutohideTimeoutDialog.show();
		}
		if (preference.getKey().equals(Persistence.PREF_SWITCH_DEFAULT)) {
			mDefaultActionsDialog.show();
		}
		if (preference.getKey().equals(Persistence.PREF_FULL_RESET_TIMEOUT)) {
			mFullResetTimeoutDialog.show();
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
				mPrefMorseHUD.setEnabled(true);
				mPrefMorseKeyMode.setEnabled(true);
				mPrefMorseTimeUnit.setEnabled(true);
				TeclaApp.getInstance().enabledMorseIME();
				TeclaApp.getInstance().showToast(R.string.morse_enabled);
			}
			else {
				TeclaApp.getInstance().disabledMorseIME();
				mPrefMorseHUD.setEnabled(false);
				mPrefMorseKeyMode.setEnabled(false);
				mPrefMorseTimeUnit.setEnabled(false);
			}
			
		}
		if (key.equals(Persistence.PREF_MORSE_SHOW_HUD)) {
			//Reset IME
			TeclaApp.getInstance().requestHideIMEView();
			TeclaApp.getInstance().requestShowIMEView();
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
		if (key.equals(Persistence.PREF_MORSE_TIME_UNIT)) {
			mPrefMorseTimeUnit.setSummary(mPrefMorseTimeUnit.getEntry());
			TeclaApp.persistence.setMorseTimeUnit(mPrefMorseTimeUnit.getValue());
		}
		if (key.equals(Persistence.PREF_SWITCH_J1_TECLA) || key.equals(Persistence.PREF_SWITCH_J1_MORSE)) {
			mSwitchJ1.onPreferenceChanged(key);
			mConfigureInputAdapter.notifyDataSetChanged();
		}
		if (key.equals(Persistence.PREF_SWITCH_J2_TECLA) || key.equals(Persistence.PREF_SWITCH_J2_MORSE)) {
			mSwitchJ2.onPreferenceChanged(key);
			mConfigureInputAdapter.notifyDataSetChanged();
		}
		if (key.equals(Persistence.PREF_SWITCH_J3_TECLA) || key.equals(Persistence.PREF_SWITCH_J3_MORSE)) {
			mSwitchJ3.onPreferenceChanged(key);
			mConfigureInputAdapter.notifyDataSetChanged();
		}
		if (key.equals(Persistence.PREF_SWITCH_J4_TECLA) || key.equals(Persistence.PREF_SWITCH_J4_MORSE)) {
			mSwitchJ4.onPreferenceChanged(key);
			mConfigureInputAdapter.notifyDataSetChanged();
		}
		if (key.equals(Persistence.PREF_SWITCH_E1_TECLA) || key.equals(Persistence.PREF_SWITCH_E1_MORSE)) {
			mSwitchE1.onPreferenceChanged(key);
			mConfigureInputAdapter.notifyDataSetChanged();
		}
		if (key.equals(Persistence.PREF_SWITCH_E2_TECLA) || key.equals(Persistence.PREF_SWITCH_E2_MORSE)) {
			mSwitchE2.onPreferenceChanged(key);
			mConfigureInputAdapter.notifyDataSetChanged();
		}
		if (key.equals(Persistence.PREF_CONNECT_TO_SHIELD)) {
			if (mPrefConnectToShield.isChecked()) {
				// Connect to shield
				discoverShield();
			} else {
				// FIXME: Tecla Access - Find out how to disconnect
				// switch event provider without breaking
				// connection with other potential clients.
				// Should perhaps use Binding?
				dismissDialog();
				if (!mPrefFullScreenSwitch.isChecked()) {
					mPrefSelfScanning.setChecked(false);
					mPrefSelfScanning.setEnabled(false);
					mPrefInverseScanning.setChecked(false);
					mPrefInverseScanning.setEnabled(false);
				}
				stopSEP();
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
			} else {
				TeclaApp.getInstance().stopScanningTeclaIME();
				if (!mPrefSelfScanning.isChecked()) {
					mPrefFullScreenSwitch.setChecked(false);
					if (!mPrefConnectToShield.isChecked()) {
						mPrefInverseScanning.setEnabled(false);
					}
				}
			}
		}
		//FIXME: Tecla Access - Solve backup elsewhere
		//(new BackupManager(getApplicationContext())).dataChanged();
	}

	private void showDiscoveryDialog() {
		mProgressDialog.setMessage(getString(R.string.searching_for_shields));
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				cancelDiscovery();
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Tecla Shield discovery cancelled");
				TeclaApp.getInstance().showToast(R.string.shield_connection_cancelled);
				mConnectionCancelled = true;
				//Since we have cancelled the discovery the check state needs to be reset
				//(triggers onSharedPreferenceChanged)
				//mPrefConnectToShield.setChecked(false);
			}
		});
		mProgressDialog.show();
	}

	/*
	 * Dismisses progress dialog and triggers it's OnCancelListener
	 */
	private void cancelDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.cancel();
		}
	}

	/*
	 * Dismisses progress dialog without triggerint it's OnCancelListener
	 */
	private void dismissDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	/*
	 * Stops the SEP if it is running
	 */
	private void stopSEP() {
		if (SepManager.isRunning(getApplicationContext())) {
			SepManager.stop(getApplicationContext());
		}
	}
	
	private void cancelDiscovery() {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
			// Triggers ACTION_DISCOVERY_FINISHED on mReceiver.onReceive
			mBluetoothAdapter.cancelDiscovery();
		}
	}
	
	private void updateShieldPreference() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			mPrefConnectToShield.setSummary(R.string.shield_connect_summary_BT_nosupport);
			mPrefConnectToShield.setEnabled(false);
		} else if (!mBluetoothAdapter.isEnabled()) {
			mPrefConnectToShield.setSummary(R.string.shield_connect_summary_BT_disabled);
			mPrefConnectToShield.setEnabled(false);
		} else {
			mPrefConnectToShield.setSummary(R.string.shield_connect_summary);
			mPrefConnectToShield.setEnabled(true);
		}
	}
	
	private void refreshSwitchesSummary() {
		mSwitchJ1.refreshSummaries();
		mSwitchJ2.refreshSummaries();
		mSwitchJ3.refreshSummaries();
		mSwitchJ4.refreshSummaries();
		mSwitchE1.refreshSummaries();
		mSwitchE2.refreshSummaries();
	}
	
	public static void setDefaultSwitchActions() {
		TeclaApp.persistence.setFullResetTimeout(Persistence.DEFAULT_FULL_RESET_TIMEOUT);
		mSwitchJ1.setValues(1, 1);
		mSwitchJ2.setValues(2, 2);
		mSwitchJ3.setValues(3, 3);
		mSwitchJ4.setValues(4, 4);
		mSwitchE1.setValues(4, 0);
		mSwitchE2.setValues(3, 0);
	}

}
