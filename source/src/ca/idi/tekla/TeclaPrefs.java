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
import java.util.ArrayList;
import java.util.HashMap;

import ca.idi.tecla.lib.InputAccess;
import ca.idi.tecla.lib.ListPreference;
import ca.idi.tecla.framework.TeclaShieldManager;
import ca.idi.tecla.framework.TeclaStatic;
import ca.idi.tekla.R;
import ca.idi.tekla.ime.TeclaIME;
import ca.idi.tekla.ime.TeclaKeyboardView;
import ca.idi.tecla.framework.TeclaShieldService;
import ca.idi.tekla.util.DefaultActionsDialog;
import ca.idi.tekla.util.FullResetTimeoutDialog;
import ca.idi.tekla.util.MorseTimeUnitDialog;
import ca.idi.tekla.util.NavKbdTimeoutDialog;
import ca.idi.tekla.util.Persistence;
import ca.idi.tekla.util.RepeatFrequencyDialog;
import ca.idi.tekla.util.ScanSpeedDialog;
import ca.idi.tekla.util.SwitchPreference;
import ca.idi.tekla.util.TeclaDesktopClient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.R.bool;
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
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.AutoText;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

public class TeclaPrefs extends PreferenceActivity
implements SharedPreferences.OnSharedPreferenceChangeListener{

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
	
	//Morse preferences
	private CheckBoxPreference mPrefMorse;
	private CheckBoxPreference mPrefMorseHUD;
	private ListPreference mPrefMorseKeyMode;
	private Preference mPrefMorseTimeUnit;
	private Preference mPrefMorseRepeat;
	
	private CheckBoxPreference mPrefPersistentKeyboard;
	private Preference mPrefAutohideTimeout;
	private CheckBoxPreference mPrefConnectToShield;
	private CheckBoxPreference mPrefTempDisconnect;
	private CheckBoxPreference mPrefFullScreenSwitch;
	private CheckBoxPreference mPrefSpeakerPhoneSwitch;
	private CheckBoxPreference mPrefSelfScanning;
	private CheckBoxPreference mPrefInverseScanning;
	private PreferenceScreen mPreferenceScreen;
	private PreferenceCategory mPredictionPreferences;
	private ProgressDialog mProgressDialog;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mShieldFound, mConnectionCancelled;
	private String mShieldAddress, mShieldName;
	
	private ScanSpeedDialog mScanSpeedDialog;
	private MorseTimeUnitDialog mMorseTimeUnitDialog;
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
	
	private static CheckBoxPreference mConnectToPC;
	private static CheckBoxPreference mShieldRelay;
	private static Preference setPasswordLaunch;
	private static Preference setDisconnectEvent;
	private static Preference setDictationEvent;

	private DefaultActionsDialog mDefaultActionsDialog;
	private static HashMap<String, String[]> mSwitchMap;
		
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		init();
		// FIXME: Not yet supported preferences
		mPredictionPreferences = (PreferenceCategory) findPreference("prediction_settings");
		mPredictionPreferences.removePreference(findPreference("show_suggestions"));
		mPredictionPreferences.removePreference(findPreference("auto_complete"));
		mPreferenceScreen = (PreferenceScreen) findPreference("english_ime_settings");
		mPreferenceScreen.removePreference(findPreference("alternative_output_settings"));
		
	}
	
	private void init() {

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		addPreferencesFromResource(R.layout.activity_prefs);
		mQuickFixes = (CheckBoxPreference) findPreference(QUICK_FIXES_KEY);
		mShowSuggestions = (CheckBoxPreference) findPreference(SHOW_SUGGESTIONS_KEY);
		mPrefVoiceInput = (CheckBoxPreference) findPreference(Persistence.PREF_VOICE_INPUT);
		mPrefVariantsKey = (CheckBoxPreference) findPreference(Persistence.PREF_VARIANTS_KEY);
		mPrefMorse = (CheckBoxPreference) findPreference(Persistence.PREF_MORSE_MODE);
		mPrefMorseHUD = (CheckBoxPreference) findPreference(Persistence.PREF_MORSE_SHOW_HUD);
		mPrefMorseKeyMode = (ListPreference) findPreference(Persistence.PREF_MORSE_KEY_MODE);
		mPrefMorseTimeUnit = (Preference) findPreference(Persistence.PREF_MORSE_TIME_UNIT);
		mPrefMorseRepeat = (Preference) findPreference(Persistence.PREF_MORSE_REPEAT_INT);
		mPrefPersistentKeyboard = (CheckBoxPreference) findPreference(Persistence.PREF_PERSISTENT_KEYBOARD);
		mPrefAutohideTimeout = (Preference) findPreference(Persistence.PREF_AUTOHIDE_TIMEOUT);
		mAutohideTimeoutDialog = new NavKbdTimeoutDialog(this);
		mAutohideTimeoutDialog.setContentView(R.layout.dialog_timeout);
		mFullResetTimeoutDialog = new FullResetTimeoutDialog(this);
		mFullResetTimeoutDialog.setContentView(R.layout.dialog_timeout);
		mPrefConnectToShield = (CheckBoxPreference) findPreference(Persistence.PREF_CONNECT_TO_SHIELD);
		mPrefTempDisconnect = (CheckBoxPreference) findPreference(Persistence.PREF_TEMP_SHIELD_DISCONNECT);
		mPrefFullScreenSwitch = (CheckBoxPreference) findPreference(Persistence.PREF_FULLSCREEN_SWITCH);
		mPrefSpeakerPhoneSwitch = (CheckBoxPreference) findPreference(Persistence.PREF_SPEAKERPHONE_SWITCH);
		mPrefSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_SELF_SCANNING);
		mPrefInverseScanning = (CheckBoxPreference) findPreference(Persistence.PREF_INVERSE_SCANNING);
		mScanSpeedDialog = new ScanSpeedDialog(this);
		mScanSpeedDialog.setContentView(R.layout.dialog_scan_speed);
		mMorseTimeUnitDialog = new MorseTimeUnitDialog(this);
		mMorseTimeUnitDialog.setContentView(R.layout.dialog_timeout);
		mRepeatFrequencyDialog = new RepeatFrequencyDialog(this);
		mRepeatFrequencyDialog.setContentView(R.layout.dialog_timeout);
		mProgressDialog = new ProgressDialog(this);
		mConfigureInputScreen = (PreferenceScreen) findPreference(Persistence.PREF_CONFIGURE_INPUT);
		mConfigureInputAdapter= (BaseAdapter) mConfigureInputScreen.getRootAdapter();
		
		//Desktop 
		final TeclaPrefs mTeclaPrefs=this;
		mConnectToPC=(CheckBoxPreference)findPreference(Persistence.CONNECT_TO_PC);
		
		mShieldRelay=(CheckBoxPreference)findPreference(Persistence.SEND_SHIELD_EVENTS);
		
		TeclaApp.sendflag=mShieldRelay.isChecked();
		TeclaApp.connect_to_desktop=mConnectToPC.isChecked();
		setDisconnectEvent=(Preference) findPreference ("set_disconnect_event");
		setDictationEvent=(Preference) findPreference("set_dictation_event");
		
		TeclaApp.dictation_event=setDictationEvent.getSharedPreferences().getInt("set_dictation_event", 55);

		
		setDictationEvent.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				showEventSelectionDialog(1);
				return true;
			}});
		
		setDisconnectEvent.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				showEventSelectionDialog(0);
				return true;
			}});
		
		TeclaApp.disconnect_event=setDisconnectEvent.getSharedPreferences().getInt("set_disconnect_event", 65);
		
		
		setPasswordLaunch=(Preference)findPreference(Persistence.SET_PASSWORD);
		
		setPasswordLaunch.setDefaultValue("Tecla123");
		TeclaApp.password=setPasswordLaunch.getSharedPreferences().getString(Persistence.SET_PASSWORD, "Tecla123");
		Log.v("set password",""+setPasswordLaunch);
		setPasswordLaunch.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				TeclaPrefs.createPreferenceDialog(mTeclaPrefs);
				return true;
			}
	
		});
		//
		TeclaApp.desktop=new TeclaDesktopClient(TeclaApp.getInstance());
		if(!mConnectToPC.isChecked()){
			setPasswordLaunch.setEnabled(false);
			setDisconnectEvent.setEnabled(false);
			setDictationEvent.setEnabled(false);
			mShieldRelay.setEnabled(false);
		}
		
		
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

		// If no voice apps available, disable voice input
		if (!(TeclaApp.getInstance().isVoiceInputSupported())) {
			if (mPrefVoiceInput.isChecked()) mPrefVoiceInput.setChecked(false);
			mPrefVoiceInput.setEnabled(false);
			mPrefVoiceInput.setSummary(R.string.no_voice_input_available);
		}
		
		// If no alternative input selected, disable scanning
		if (!mPrefConnectToShield.isChecked() && !mPrefFullScreenSwitch.isChecked()) {
			mPrefTempDisconnect.setChecked(false);
			mPrefTempDisconnect.setEnabled(false);
			mPrefSelfScanning.setChecked(false);
			mPrefInverseScanning.setChecked(false);
		}
		
		mPrefMorseKeyMode.setSummary(mPrefMorseKeyMode.getEntry());
		mPrefMorseTimeUnit.setSummary(TeclaApp.persistence.getMorseTimeUnit() + " ms");
		refreshSwitchesSummary();
		
		//If Morse mode is disabled, also disable all of the other prefs in the same category
		if (!mPrefMorse.isEnabled() || (mPrefMorse.isEnabled() && !mPrefMorse.isChecked())) {
			mPrefMorseHUD.setEnabled(false);
			mPrefMorseKeyMode.setEnabled(false);
			mPrefMorseTimeUnit.setEnabled(false);
			mPrefMorseRepeat.setEnabled(false);
		}
		
		if (mPrefMorse.isEnabled() && mPrefMorse.isChecked()) {
			enableKeyModePrefs();
		}

		//Tecla Access Intents & Intent Filters
		registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		registerReceiver(mReceiver, new IntentFilter(TeclaShieldService.ACTION_SHIELD_CONNECTED));
		registerReceiver(mReceiver, new IntentFilter(TeclaShieldService.ACTION_SHIELD_DISCONNECTED));

		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(TeclaPrefs.this);
		
		// DETERMINE WHICH PREFERENCES SHOULD BE ENABLED
		// If Tecla Access IME is not selected disable all alternative input preferences
		if (!TeclaStatic.isDefaultIME(getApplicationContext())) {
			//Tecla Access is not selected
			mPrefPersistentKeyboard.setEnabled(false);
			mPrefAutohideTimeout.setEnabled(false);
			mPrefFullScreenSwitch.setEnabled(false);
			mPrefConnectToShield.setEnabled(false);
			mPrefTempDisconnect.setEnabled(false);
			mPrefMorse.setEnabled(false);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.tecla_notselected);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					TeclaPrefs.this.finish();
				}
				
			});
			builder.setCancelable(false);
			AlertDialog alert = builder.create();
			alert.show();
			InputAccess.showBelowIME(alert);
		}		
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
	
	BroadcastReceiver btReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
			if (state == BluetoothAdapter.STATE_ON){
				Log.i(TeclaApp.TAG, "Bluetooth Turned On Successfully");
				mPrefConnectToShield.setChecked(true);
			}
		}
	};

	
	// All intents will be processed here
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND) && !mShieldFound) {
				BluetoothDevice dev = intent.getExtras().getParcelable(BluetoothDevice.EXTRA_DEVICE);
				if ((dev.getName() != null) && (
						dev.getName().startsWith(TeclaShieldService.SHIELD_PREFIX_2) ||
						dev.getName().startsWith(TeclaShieldService.SHIELD_PREFIX_3) )) {
					mShieldFound = true;
					mShieldAddress = dev.getAddress();
					mShieldName = dev.getName();
					if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Found a Tecla Access Shield candidate");
					cancelDiscovery();
				}
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
					if(!TeclaShieldManager.connect(TeclaPrefs.this, mShieldAddress)) {
						// Could not connect to Shield
						dismissDialog();
						TeclaApp.getInstance().showToast(R.string.couldnt_connect_shield);
					}
				} else {
					// Shield not found
					dismissDialog();
					if (!mConnectionCancelled) TeclaApp.getInstance().showToast(R.string.no_shields_inrange);
					mPrefConnectToShield.setChecked(false);
					mPrefTempDisconnect.setChecked(false);
					mPrefTempDisconnect.setEnabled(false);
				}
			}

			if (intent.getAction().equals(TeclaShieldService.ACTION_SHIELD_CONNECTED)) {
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Successfully started SEP");
				dismissDialog();
				TeclaApp.getInstance().showToast(R.string.shield_connected);
				mPrefTempDisconnect.setEnabled(true);
				mPrefMorse.setEnabled(true);
				mPrefPersistentKeyboard.setChecked(true);
			}

			if (intent.getAction().equals(TeclaShieldService.ACTION_SHIELD_DISCONNECTED)) {
				if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "SEP broadcast stopped");
				dismissDialog();
				mPrefTempDisconnect.setChecked(false);
				mPrefTempDisconnect.setEnabled(false);
			}
		}
	};

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference.getKey()==null){
			return false;
		}
		if (preference.getKey().equals(Persistence.PREF_SCAN_DELAY_INT)) {
			mScanSpeedDialog.show();
		}
		if (preference.getKey().equals(Persistence.PREF_MORSE_TIME_UNIT)) {
			mMorseTimeUnitDialog.show();
		}
		if (preference.getKey().equals(Persistence.PREF_MORSE_REPEAT_INT)) {
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
		if (preference.getKey().equals(Persistence.PREF_CONNECT_TO_SHIELD)) {
			if (mBluetoothAdapter == null) {
				showAlert(R.string.shield_connect_summary_BT_nosupport);
			} else if (!mBluetoothAdapter.isEnabled()) {
				registerReceiver(btReceiver, new IntentFilter(
						BluetoothAdapter.ACTION_STATE_CHANGED));
				startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
			}
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
				enableKeyModePrefs();
				mPrefMorseHUD.setEnabled(true);
				mPrefMorseKeyMode.setEnabled(true);
				TeclaApp.getInstance().enabledMorseIME();
				TeclaApp.getInstance().showToast(R.string.morse_enabled);
			}
			else {
				TeclaApp.getInstance().disabledMorseIME();
				mPrefMorseHUD.setEnabled(false);
				mPrefMorseKeyMode.setEnabled(false);
				mPrefMorseTimeUnit.setEnabled(false);
				mPrefMorseRepeat.setEnabled(false);
			}
			
		}
		if (key.equals(Persistence.PREF_MORSE_SHOW_HUD)) {
			//Reset IME
			TeclaApp.getInstance().requestHideIMEView();
			TeclaApp.getInstance().requestShowIMEView();
		}
		if (key.equals(Persistence.PREF_MORSE_KEY_MODE)) {
			mPrefMorseKeyMode.setSummary(mPrefMorseKeyMode.getEntry());
			enableKeyModePrefs();
		}
		if (key.equals(Persistence.PREF_MORSE_TIME_UNIT)) {
			mPrefMorseTimeUnit.setSummary(mMorseTimeUnitDialog.getLabel());
		}
		if (key.equals(Persistence.PREF_PERSISTENT_KEYBOARD)) {
			if (mPrefPersistentKeyboard.isChecked()) {
				mPrefAutohideTimeout.setEnabled(true);
				
				// Show keyboard immediately
				TeclaApp.getInstance().requestShowIMEView();
			} else {
				mPrefAutohideTimeout.setEnabled(false);
				mPrefSelfScanning.setChecked(false);
				mPrefInverseScanning.setChecked(false);
				mPrefFullScreenSwitch.setChecked(false);
				mPrefConnectToShield.setChecked(false);
				mPrefTempDisconnect.setChecked(false);
				mPrefTempDisconnect.setEnabled(false);
				TeclaApp.getInstance().requestHideIMEView();
			}
		}
		if (key.equals(Persistence.PREF_AUTOHIDE_TIMEOUT)) {
			if (mPrefPersistentKeyboard.isChecked()) {
				// Show keyboard immediately if Tecla Access IME is selected
				TeclaApp.getInstance().requestShowIMEView();
			} else {
				mPrefSelfScanning.setChecked(false);
				mPrefInverseScanning.setChecked(false);
				mPrefFullScreenSwitch.setChecked(false);
				mPrefConnectToShield.setChecked(false);
				mPrefTempDisconnect.setChecked(false);
				mPrefTempDisconnect.setEnabled(false);
				TeclaApp.getInstance().requestHideIMEView();
			}
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
			if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
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
						mPrefTempDisconnect.setChecked(false);
						mPrefTempDisconnect.setEnabled(false);
						mPrefSelfScanning.setChecked(false);
						mPrefInverseScanning.setChecked(false);
						mPrefPersistentKeyboard.setChecked(false);
					}
					stopShieldService();
				}
			} else {
				mPrefConnectToShield.setChecked(false);
			}
		}
		if (key.equals(Persistence.PREF_TEMP_SHIELD_DISCONNECT)) {
			if(mPrefTempDisconnect.isChecked()) {
				mPrefConnectToShield.setEnabled(false);
				stopShieldService();
				Handler mHandler = new Handler();
				Runnable mReconnect = new Runnable() {
					
					public void run() {
						if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Re-enabling discovery");
						discoverShield();
						mPrefConnectToShield.setEnabled(true);
					}
				};
				
				// See if the handler was posted
				if(mHandler.postDelayed(mReconnect, 90000))	// 90 second delay
				{
					if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Posted Runnable");
				}
				else
				{
					if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Could not post Runnable");
				}
				
			}
			else {
				
			}
		}
		if (key.equals(Persistence.PREF_FULLSCREEN_SWITCH)) {
			if (mPrefFullScreenSwitch.isChecked()) {
				mPrefPersistentKeyboard.setChecked(true);
				TeclaApp.getInstance().startFullScreenSwitchMode();
				if (!(mPrefSelfScanning.isChecked() || mPrefInverseScanning.isChecked())) {
					mPrefSelfScanning.setChecked(true);
				}
				mPrefAutohideTimeout.setEnabled(false);
				TeclaApp.persistence.setNeverHideNavigationKeyboard();
			} else {
				if (!mPrefConnectToShield.isChecked()) {
					mPrefTempDisconnect.setChecked(false);
					mPrefTempDisconnect.setEnabled(false);
					mPrefSelfScanning.setChecked(false);
					mPrefInverseScanning.setChecked(false);
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
						mPrefTempDisconnect.setChecked(false);
						mPrefTempDisconnect.setEnabled(false);
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
						mPrefTempDisconnect.setChecked(false);
						mPrefTempDisconnect.setEnabled(false);
					}
				}
			}
		}
		if(key.equals(Persistence.SEND_SHIELD_EVENTS)){
			TeclaApp.sendflag=mShieldRelay.isChecked();
		}
		if(key.equals(Persistence.CONNECT_TO_PC)){
			if(TeclaApp.mSendToPC && !mConnectToPC.isChecked()){
				TeclaIME.getInstance().onKey(TeclaKeyboardView.KEYCODE_SEND_TO_PC,null);
				//update the send to pc button lock
				TeclaIME.getInstance().onKey(TeclaKeyboardView.KEYCODE_SHOW_SECNAV_VOICE, null);
//				TeclaKeyboardView.getInstance().disableSendToPCKey();
				TeclaIME.getInstance().onKey(TeclaKeyboardView.KEYCODE_HIDE_SECNAV_VOICE, null);
			}
			TeclaApp.connect_to_desktop=mConnectToPC.isChecked();
			if(!mConnectToPC.isChecked()){
				setPasswordLaunch.setEnabled(false);
				setDisconnectEvent.setEnabled(false);
				setDictationEvent.setEnabled(false);
				mShieldRelay.setEnabled(false);
			}
			else{
				setPasswordLaunch.setEnabled(true);
				setDisconnectEvent.setEnabled(true);
				setDictationEvent.setEnabled(true);
				mShieldRelay.setEnabled(true);
				if(TeclaApp.desktop!=null && TeclaApp.desktop.isConnected())
				TeclaApp.desktop.disconnect();
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
				mPrefTempDisconnect.setChecked(false);
				mPrefTempDisconnect.setEnabled(false);
				//Since we have cancelled the discovery the check state needs to be reset
				//(triggers onSharedPreferenceChanged)
				//mPrefConnectToShield.setChecked(false);
			}
		});
		mProgressDialog.show();
	}
	
	/*
	 * Enables / disables prefs based on current Morse mode
	 */
	private void enableKeyModePrefs() {
		if (TeclaApp.persistence.getMorseKeyMode() == TeclaIME.TRIPLE_KEY_MODE) {
			mPrefMorseTimeUnit.setEnabled(false);
			mPrefMorseRepeat.setEnabled(true);
		}
		else {
			mPrefMorseTimeUnit.setEnabled(true);
			mPrefMorseRepeat.setEnabled(false);
		}
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
	private void stopShieldService() {
		if (TeclaShieldManager.isRunning(getApplicationContext())) {
			TeclaShieldManager.disconnect(getApplicationContext());
		}
	}
	
	private void cancelDiscovery() {
		if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
			// Triggers ACTION_DISCOVERY_FINISHED on mReceiver.onReceive
			mBluetoothAdapter.cancelDiscovery();
		}
	}
	
	/*
	 * Updates display of preference summaries
	 */
	private void refreshSwitchesSummary() {
		mSwitchJ1.refreshSummaries();
		mSwitchJ2.refreshSummaries();
		mSwitchJ3.refreshSummaries();
		mSwitchJ4.refreshSummaries();
		mSwitchE1.refreshSummaries();
		mSwitchE2.refreshSummaries();
	}
	
	/*
	 * Resets switch actions to default values
	 */
	public static void setDefaultSwitchActions() {
		TeclaApp.persistence.setFullResetTimeout(Persistence.DEFAULT_FULL_RESET_TIMEOUT);
		mSwitchJ1.setValues(1, 1);
		mSwitchJ2.setValues(2, 2);
		mSwitchJ3.setValues(3, 3);
		mSwitchJ4.setValues(4, 4);
		mSwitchE1.setValues(4, 0);
		mSwitchE2.setValues(3, 0);
	}
	
	public static void createPreferenceDialog(Context context){
		final Dialog passworddialog=new Dialog(context);
		passworddialog.setContentView(R.layout.passworddialog);
		final EditText psswdtext=(EditText)passworddialog.findViewById(R.id.passwordtext);
		Button save=(Button)passworddialog.findViewById(R.id.savebutton);
		Button cancel=(Button)passworddialog.findViewById(R.id.cancelbutton);
		
		psswdtext.setText(setPasswordLaunch.getSharedPreferences().getString(Persistence.SET_PASSWORD, "Tecla123"));
		
		save.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				TeclaApp.password=psswdtext.getText().toString();
				setPasswordLaunch.getEditor().putString(Persistence.SET_PASSWORD, TeclaApp.password).commit();
				passworddialog.dismiss();
			}
		});
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				passworddialog.dismiss();
			}
		});
		passworddialog.show();
	}
	
	public void showEventSelectionDialog(final int event){
		final Dialog d=new Dialog(TeclaPrefs.this);
		d.setContentView(R.layout.eventselectdialog);
		final Spinner switchchoice=(Spinner)d.findViewById(R.id.spinnerswitch);
		final Spinner eventchoice= (Spinner)d.findViewById(R.id.spinnerevent);
		Button save=(Button)d.findViewById(R.id.save_button);
		Button cancel=(Button)d.findViewById(R.id.cancel_button);
		
		
		String[] switcharray={"switch j1/Up","switch j2/Down","Switch J3/Left","Switch J4,Right",
				"Switch E1","Switch E2"};
		ArrayList<String> switchlist=new ArrayList<String>();
		for(int i=0;i<switcharray.length;i++)
			switchlist.add(switcharray[i]);
		ArrayAdapter<String> spin1adapter=new ArrayAdapter<String>(TeclaPrefs.this,
									android.R.layout.simple_spinner_item,switchlist);
		switchchoice.setAdapter(spin1adapter);
		
		String[] eventarray={"onPress","onRelease","onClick","onDoubleClick","onLongPress"};
		ArrayList<String> eventlist=new ArrayList<String>();
		for(int i=0;i<eventarray.length;i++)
			eventlist.add(eventarray[i]);
		ArrayAdapter<String> spin2adapter=new ArrayAdapter<String>(TeclaPrefs.this,
									android.R.layout.simple_spinner_item,eventlist);
		eventchoice.setAdapter(spin2adapter);
		if(event==0){
		eventchoice.setSelection(TeclaApp.disconnect_event%10-1);
		switchchoice.setSelection(TeclaApp.disconnect_event/10-1);
		}
		else if(event==1){
			eventchoice.setSelection(TeclaApp.dictation_event%10-1);
			switchchoice.setSelection(TeclaApp.dictation_event/10-1);
			}
		spin1adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin2adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				d.dismiss();
			}
		});
		save.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				int x,y;
				x=eventchoice.getSelectedItemPosition()+1;
				y=switchchoice.getSelectedItemPosition()+1;
				if(event==0 ){
					if(y*10+x != TeclaApp.dictation_event){
					TeclaApp.disconnect_event=y*10+x;
					setDisconnectEvent.getEditor().putInt("set_disconnect_event",TeclaApp.disconnect_event).commit();
					if(TeclaApp.desktop!=null && TeclaApp.desktop.isConnected())
						TeclaApp.desktop.send("disevent:"+TeclaApp.disconnect_event);
					}
					else
						eventchoice.setSelection(TeclaApp.dictation_event%10-1);
				}
				else if(event==1 ){
					if(y*10+x != TeclaApp.disconnect_event){
					TeclaApp.dictation_event=y*10+x;
					setDictationEvent.getEditor().putInt("set_dictation_event",TeclaApp.dictation_event).commit();
					if(TeclaApp.desktop!=null && TeclaApp.desktop.isConnected()){
						TeclaApp.desktop.send("dictevent:"+TeclaApp.dictation_event);
						Log.v("voice","Entering sending preference");
						}
					}
					else{
						eventchoice.setSelection(TeclaApp.dictation_event%10-1);
					}
				}
				// TODO Auto-generated method stub
				d.dismiss();
			}
		});
		d.show();
	}
	
	public void showAlert(int resid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(resid);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
			
		});
		AlertDialog alert = builder.create();
		alert.show();
        InputAccess.showBelowIME(alert);
	}
	
}
