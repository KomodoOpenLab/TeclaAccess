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

package ca.idi.tekla.ime;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import ca.idi.tecla.framework.TeclaIMEService;
import ca.idi.tecla.framework.TeclaStatic;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.AutoText;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import ca.idi.tecla.framework.TeclaShieldManager;
import ca.idi.tecla.sdk.SwitchEvent;
import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;
import ca.idi.tekla.TeclaPrefs;
import ca.idi.tecla.framework.TeclaShieldService;
import ca.idi.tekla.util.EmergencyCallout;
import ca.idi.tekla.util.Highlighter;
import ca.idi.tekla.util.Persistence;

/**
 * Input method implementation for Qwerty'ish keyboard.
 */
public class TeclaIME extends ca.idi.tecla.framework.TeclaIMEService
		implements KeyboardView.OnKeyboardActionListener {
	static final boolean TRACE = false;

	private static final String PREF_VIBRATE_ON = "vibrate_on";
	private static final String PREF_SOUND_ON = "sound_on";
	private static final String PREF_AUTO_CAP = "auto_cap";
	private static final String PREF_QUICK_FIXES = "quick_fixes";
	private static final String PREF_SHOW_SUGGESTIONS = "show_suggestions";
	private static final String PREF_AUTO_COMPLETE = "auto_complete";

	private static final int MSG_UPDATE_SUGGESTIONS = 0;
	private static final int MSG_START_TUTORIAL = 1;
	private static final int MSG_UPDATE_SHIFT_STATE = 2;
	private static final int UPDATE_SHIFT_DELAY = 300;

	// How many continuous deletes at which to start deleting at a higher speed.
	private static final int DELETE_ACCELERATE_AT = 20;
	// Key events coming any faster than this are long-presses.
	private static final int QUICK_PRESS = 200;
	// Weight added to a user picking a new word from the suggestion strip
	static final int FREQUENCY_FOR_PICKED = 3;
	// Weight added to a user typing a new word that doesn't get corrected (or is reverted)
	static final int FREQUENCY_FOR_TYPED = 1;
	// A word that is frequently typed and get's promoted to the user dictionary, uses this
	// frequency.
	static final int FREQUENCY_FOR_AUTO_ADD = 250;

	static final int KEYCODE_ENTER = '\n';
	static final int KEYCODE_SPACE = ' ';

	// Contextual menu positions
	private static final int POS_SETTINGS = 0;
	private static final int POS_METHOD = 1;
	
	private TeclaKeyboardView mIMEView;
	private CandidateViewContainer mCandidateViewContainer;
	private CandidateView mCandidateView;
	private Suggest mSuggest;
	private CompletionInfo[] mCompletions;

	private AlertDialog mOptionsDialog;

	KeyboardSwitcher mKeyboardSwitcher;
	
	// Morse variables	
	private TeclaMorse mTeclaMorse;
	private Keyboard.Key mSpaceKey;
	private Keyboard.Key mSendtoPCKey;
	private int mSpaceKeyIndex;
	private int mRepeatedKey;
	private long mMorseStartTime;
	
	//Morse key modes
	public static final int TRIPLE_KEY_MODE = 0;
	public static final int DOUBLE_KEY_MODE = 1;
	public static final int SINGLE_KEY_MODE = 2;
	
	//Morse typing error margin (single-key mode)
	private static final float ERROR_MARGIN = 1.15f;

	private UserDictionary mUserDictionary;
	private ContactsDictionary mContactsDictionary;
	private ExpandableDictionary mAutoDictionary;

	private String mLocale;

	private StringBuilder mComposing = new StringBuilder();
	private WordComposer mWord = new WordComposer();
	private int mCommittedLength;
	private boolean mPredicting;
	private CharSequence mBestWord;
	private boolean mPredictionOn;
	private boolean mCompletionOn;
	private boolean mAutoSpace;
	private boolean mAutoCorrectOn;
	private boolean mCapsLock;
	private boolean mVibrateOn;
	private boolean mSoundOn;
	private boolean mAutoCap;
	private boolean mQuickFixes;
	private boolean mShowSuggestions;
	
	
	private int     mCorrectionMode;
	private int     mOrientation;
	
	
	// Keycode of the key which is on repeat
	private int mRepeatingKeyCode;
	private boolean wasAutoRepeating;
	
	// Indicates whether the suggestion strip is to be on in landscape
	private boolean mJustAccepted;
	private CharSequence mJustRevertedSeparator;
	private int mDeleteCount;
	private long mLastKeyTime;

	private Tutorial mTutorial;

	private Vibrator mVibrator;
	private long mVibrateDuration;

	private AudioManager mAudioManager;
	// Align sound effect volume on music volume
	private final float FX_VOLUME = -1.0f;
	private boolean mSilentMode;
	private ToneGenerator mTone = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
	private int mToneType = ToneGenerator.TONE_CDMA_DIAL_TONE_LITE;

	private String mWordSeparators;
	private String mSentenceSeparators;

	
	
	private int wifi_ping_count=0;
	private Thread wifisearcherthread;
	private static TeclaIME instance;
	
	public  static TeclaIME getInstance(){
		return instance;
	}
	
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_SUGGESTIONS:
				updateSuggestions();
				break;
			case MSG_START_TUTORIAL:
				if (mTutorial == null) {
					if (mIMEView.isShown()) {
						mTutorial = new Tutorial(TeclaIME.this, mIMEView);
						mTutorial.start();
					} else {
						// Try again soon if the view is not yet showing
						sendMessageDelayed(obtainMessage(MSG_START_TUTORIAL), 100);
					}
				}
				break;
			case MSG_UPDATE_SHIFT_STATE:
				updateShiftKeyState(getCurrentInputEditorInfo());
				break;
			}
		}
	};
	
	@Override
	public void onCreate() {
		
		super.onCreate();
		mTeclaMorse = new TeclaMorse(this);
		instance=this;
		// Setup Debugging
		//if (TeclaApp.DEBUG) android.os.Debug.waitForDebugger();
		TeclaStatic.logD(CLASS_TAG, "Creating IME...");

		//setStatusIcon(R.drawable.ime_qwerty);
		mKeyboardSwitcher = new KeyboardSwitcher(this);
		final Configuration conf = getResources().getConfiguration();
		initSuggest(conf.locale.toString());
		mOrientation = conf.orientation;

		mVibrateDuration = getResources().getInteger(R.integer.vibrate_duration_ms);

		// register to receive ringer mode changes for silent mode
		registerReceiver(mReceiver, new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));

		initTeclaA11y();
//		initDesktop();
	}
	
	private void initSuggest(String locale) {
		mLocale = locale;
		mSuggest = new Suggest(this, R.raw.main);
		mSuggest.setCorrectionMode(mCorrectionMode);
		mUserDictionary = new UserDictionary(this);
		mContactsDictionary = new ContactsDictionary(this);
		mAutoDictionary = new AutoDictionary(this);
		mSuggest.setUserDictionary(mUserDictionary);
		mSuggest.setContactsDictionary(mContactsDictionary);
		mSuggest.setAutoDictionary(mAutoDictionary);
		mWordSeparators = getResources().getString(R.string.word_separators);
		mSentenceSeparators = getResources().getString(R.string.sentence_separators);
	}
	
/*	private void initDesktop(){
		TeclaApp.mSendToPC=false;
		wifisearcherthread=new Thread(desktopsearcher);
		
	}
*/
	@Override public void onDestroy() {
		super.onDestroy();
		mUserDictionary.close();
		mContactsDictionary.close();
		unregisterReceiver(mReceiver);
		TeclaApp.highlighter.stopSelfScanning();
		TeclaShieldManager.disconnect(this);
	}

	
	@Override
	public void onConfigurationChanged(Configuration conf) {
		if (!TextUtils.equals(conf.locale.toString(), mLocale)) {
			initSuggest(conf.locale.toString());
		}
		// If orientation changed while predicting, commit the change
		if (conf.orientation != mOrientation) {
			commitTyped(getCurrentInputConnection());
			mOrientation = conf.orientation;
			
			// If the fullscreen switch is enabled, change its size to match screen
			if(isFullScreenShowing()) {
				TeclaStatic.logD(CLASS_TAG, "Changing size of fullscreen overlay.");
				Display display = getDisplay();
				mSwitchPopup.update(display.getWidth(), display.getHeight());
			}
		}
		if (mKeyboardSwitcher == null) {
			mKeyboardSwitcher = new KeyboardSwitcher(this);
		}
		mKeyboardSwitcher.makeKeyboards(true);
		
		super.onConfigurationChanged(conf);
		
		if (mKeyboardSwitcher.isMorseMode()) {
			updateSpaceKey();
			mIMEView.invalidate();
		}
	}

	@Override
	public View onCreateInputView() {
		mIMEView = (TeclaKeyboardView) getLayoutInflater().inflate(
				R.xml.input, null);
		mKeyboardSwitcher.setInputView(mIMEView);
		mKeyboardSwitcher.makeKeyboards(true);
		mIMEView.setOnKeyboardActionListener(this);
		mIMEView.setTeclaMorse(mTeclaMorse);
		mIMEView.setService(this);
		
		if (TeclaApp.persistence.isMorseModeEnabled())
			mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_MORSE, 0);
		else
			mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_NAV, 0);
		
		TeclaStatic.logD(CLASS_TAG, "Soft IME view created.");
		TeclaApp.highlighter.setIMEView(mIMEView);
		return mIMEView;
	}

	@Override
	public View onCreateCandidatesView() {
		// No candidates view in Morse mode
		if (!TeclaApp.persistence.isMorseModeEnabled()) {
			mKeyboardSwitcher.makeKeyboards(true);
			mCandidateViewContainer = (CandidateViewContainer) getLayoutInflater().inflate(
					R.layout.candidates, null);
			mCandidateViewContainer.initViews();
			mCandidateView = (CandidateView) mCandidateViewContainer.findViewById(R.id.candidates);
			mCandidateView.setService(this);
			setCandidatesViewShown(true);
			// TODO: Tecla - uncomment to enable suggestions
			//return mCandidateViewContainer;
		}
		return null;
	}

	@Override 
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		// In landscape mode, this method gets called without the input view being created.
		if (mIMEView == null) {
			return;
		}
		
		mKeyboardSwitcher.makeKeyboards(false);

		TextEntryState.newSession(this);

		boolean disableAutoCorrect = false;
		mPredictionOn = false;
		mCompletionOn = false;
		mCompletions = null;
		mCapsLock = false;
		
		switch (attribute.inputType&EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_NUMBER:
		case EditorInfo.TYPE_CLASS_DATETIME:
			if (TeclaApp.persistence.isMorseModeEnabled())
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_MORSE, attribute.imeOptions);
			else
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_SYMBOLS, attribute.imeOptions);
			break;
		case EditorInfo.TYPE_CLASS_PHONE:
			if (TeclaApp.persistence.isMorseModeEnabled())
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_MORSE, attribute.imeOptions);
			else
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_PHONE, attribute.imeOptions);
			break;
		case EditorInfo.TYPE_CLASS_TEXT:
			if (TeclaApp.persistence.isMorseModeEnabled())
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_MORSE, attribute.imeOptions);
			else {
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, attribute.imeOptions);
				//startPrediction();
				mPredictionOn = true;
				// Make sure that passwords are not displayed in candidate view
				int variation = attribute.inputType &  EditorInfo.TYPE_MASK_VARIATION;
				if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
						variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ) {
					mPredictionOn = false;
				}
				if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
						|| variation == EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME) {
					mAutoSpace = false;
				} else {
					mAutoSpace = true;
				}
				if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
					mPredictionOn = false;
					mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_EMAIL, attribute.imeOptions);
				} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_URI) {
					mPredictionOn = false;
					mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_URL, attribute.imeOptions);
				} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
					mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_IM, attribute.imeOptions);
				} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
					mPredictionOn = false;
				} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT) {
					// If it's a browser edit field and auto correct is not ON explicitly, then
					// disable auto correction, but keep suggestions on.
					if ((attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT) == 0) {
						disableAutoCorrect = true;
					}
				}

				// If NO_SUGGESTIONS is set, don't do prediction.
				if ((attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0) {
					mPredictionOn = false;
					disableAutoCorrect = true;
				}
				// If it's not multiline and the autoCorrect flag is not set, then don't correct
				if ((attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT) == 0 &&
						(attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) == 0) {
					disableAutoCorrect = true;
				}
				if ((attribute.inputType&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
					mPredictionOn = false;
					mCompletionOn = true && isFullscreenMode();
				}
				updateShiftKeyState(attribute);
			}
			break;
		case EditorInfo.TYPE_NULL:
			mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_NAV, attribute.imeOptions);
			updateShiftKeyState(attribute);
			break;
		default:
			if (TeclaApp.persistence.isMorseModeEnabled())
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_MORSE, attribute.imeOptions);
			else
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, attribute.imeOptions);
			updateShiftKeyState(attribute);
		}
		mIMEView.closing();
		mComposing.setLength(0);
		mPredicting = false;
		mDeleteCount = 0;
		setCandidatesViewShown(false);
		if (mCandidateView != null) mCandidateView.setSuggestions(null, false, false, false);
		loadSettings();
		// Override auto correct
		if (disableAutoCorrect) {
			mAutoCorrectOn = false;
			if (mCorrectionMode == Suggest.CORRECTION_FULL) {
				mCorrectionMode = Suggest.CORRECTION_BASIC;
			}
		}
		mIMEView.setProximityCorrectionEnabled(true);
		if (mSuggest != null) {
			mSuggest.setCorrectionMode(mCorrectionMode);
		}
		mPredictionOn = mPredictionOn && mCorrectionMode > 0;
		checkTutorial(attribute.privateImeOptions);
		if (TRACE) Debug.startMethodTracing("/data/trace/latinime");

		int thisKBMode = mKeyboardSwitcher.getKeyboardMode();
		if(mLastKeyboardMode != thisKBMode) {
			mLastKeyboardMode = thisKBMode;
			evaluateStartScanning();
		}
		evaluateNavKbdTimeout();
	}
	
	@Override
	public void onFinishInput() {
		super.onFinishInput();

		if (mIMEView != null) {
			mIMEView.closing();
		}
	}

	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd,
			int candidatesStart, int candidatesEnd) {

		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
				candidatesStart, candidatesEnd);

		if (mKeyboardSwitcher.isMorseMode()) {
			updateShiftKeyState(getCurrentInputEditorInfo());
		} else {
			// If the current selection in the text view changes, we should
			// clear whatever candidate text we have.
			if (mComposing.length() > 0 && mPredicting && (newSelStart != candidatesEnd
					|| newSelEnd != candidatesEnd)) {
				mComposing.setLength(0);
				mPredicting = false;
				updateSuggestions();
				TextEntryState.reset();
				InputConnection ic = getCurrentInputConnection();
				if (ic != null) {
					ic.finishComposingText();
				}
			} else if (!mPredicting && !mJustAccepted
					&& TextEntryState.getState() == TextEntryState.STATE_ACCEPTED_DEFAULT) {
				TextEntryState.reset();
			}
			mJustAccepted = false;
			postUpdateShiftKeyState(UPDATE_SHIFT_DELAY);
		}
	}

	@Override
	public void hideWindow() {
		if (TeclaApp.highlighter.isSoftIMEShowing()) {
			TeclaApp.highlighter.stopSelfScanning();
			TeclaApp.highlighter.clear();
		}
		if (TRACE) Debug.stopMethodTracing();
		if (mOptionsDialog != null && mOptionsDialog.isShowing()) {
			mOptionsDialog.dismiss();
			mOptionsDialog = null;
		}
		if (mTutorial != null) {
			mTutorial.close();
			mTutorial = null;
		}
		super.hideWindow();
		TextEntryState.endSession();
	}

	@Override
	public boolean onEvaluateFullscreenMode() {
		// never go to fullscreen mode
		//return super.onEvaluateFullscreenMode();
		return false;
	}

	@Override
	public void onDisplayCompletions(CompletionInfo[] completions) {
//		if (false) {
//			Log.i("foo", "Received completions:");
//			for (int i=0; i<(completions != null ? completions.length : 0); i++) {
//				Log.i("foo", "  #" + i + ": " + completions[i]);
//			}
//		}
		if (mCompletionOn) {
			mCompletions = completions;
			if (completions == null) {
				mCandidateView.setSuggestions(null, false, false, false);
				return;
			}

			List<CharSequence> stringList = new ArrayList<CharSequence>();
			for (int i=0; i<(completions != null ? completions.length : 0); i++) {
				CompletionInfo ci = completions[i];
				if (ci != null) stringList.add(ci.getText());
			}
			//CharSequence typedWord = mWord.getTypedWord();
			mCandidateView.setSuggestions(stringList, true, true, true);
			mBestWord = null;
			setCandidatesViewShown(isCandidateStripVisible() || mCompletionOn);
		}
	}

	@Override
	public void setCandidatesViewShown(boolean shown) {
		// TODO: Remove this if we support candidates with hard keyboard
		if (onEvaluateInputViewShown()) {
			super.setCandidatesViewShown(shown);
		}
	}

	@Override
	public void onComputeInsets(TeclaIMEService.Insets outInsets) {
		super.onComputeInsets(outInsets);
		if (!isFullscreenMode()) {
			outInsets.contentTopInsets = outInsets.visibleTopInsets;
		}
	}

	
	/**
	 * Hardware key down!
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// FIXME: Tecla - Prevent soft input method from consuming the back key
			/*if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    } else if (mTutorial != null) {
                        mTutorial.close();
                        mTutorial = null;
                    }
                }
                break;*/
			return false;
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			// If tutorial is visible, don't allow dpad to work
			if (mTutorial != null) {
				return true;
			}
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			// If tutorial is visible, don't allow dpad to work
			if (mTutorial != null) {
				return true;
			}
			// Enable shift key and DPAD to do selections
			if (TeclaApp.highlighter.isSoftIMEShowing() && mIMEView.isShifted()) {
				event = new KeyEvent(event.getDownTime(), event.getEventTime(), 
						event.getAction(), event.getKeyCode(), event.getRepeatCount(),
						event.getDeviceId(), event.getScanCode(),
						KeyEvent.META_SHIFT_LEFT_ON | KeyEvent.META_SHIFT_ON);
				InputConnection ic = getCurrentInputConnection();
				if (ic != null) ic.sendKeyEvent(event);
				return true;
			}
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * This is called every time the soft IME window is hidden from the user.
	 */
	@Override
	public void onWindowHidden() {
		super.onWindowHidden();
		if (shouldShowIME() && !mIsNavKbdTimedOut) {
			showIMEView();
			if (TeclaApp.highlighter.isSoftIMEShowing()) {
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_NAV, 0);
				evaluateStartScanning();
			}
		}
	}

	@Override
	public boolean onEvaluateInputViewShown() {
		return shouldShowIME()?
				true:super.onEvaluateInputViewShown();
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION))
				// receive ringer mode changes to detect silent mode
				updateRingerMode();
			if (action.equals(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED)) {
				TeclaStatic.logD(CLASS_TAG, "Received switch event intent.");
				handleSwitchEvent(new SwitchEvent(intent.getExtras()));
			}
			if (action.equals(TeclaShieldService.ACTION_SHIELD_CONNECTED)) {
				TeclaStatic.logD(CLASS_TAG, "Received Shield connected intent.");
				if (!mShieldConnected) mShieldConnected = true;
				showIMEView();
				evaluateStartScanning();
			}
			if (action.equals(TeclaShieldService.ACTION_SHIELD_DISCONNECTED)) {
				TeclaStatic.logD(CLASS_TAG, "Received Shield disconnected intent.");
				if (mShieldConnected) mShieldConnected = false;
				evaluateStartScanning();
			}
			if (action.equals(TeclaApp.ACTION_SHOW_IME)) {
				TeclaStatic.logD(CLASS_TAG, "Received show IME intent.");
				showIMEView();
				evaluateStartScanning();
				evaluateNavKbdTimeout();
				//TODO: Assume/force persistent keyboard preference
			}
			if (action.equals(TeclaApp.ACTION_HIDE_IME)) {
				TeclaStatic.logD(CLASS_TAG, "Received hide IME intent.");
				hideSoftIME();
			}
			if (action.equals(TeclaApp.ACTION_START_FS_SWITCH_MODE)) {
				TeclaStatic.logD(CLASS_TAG, "Received start fullscreen switch mode intent.");
				startFullScreenSwitchMode(500);
			}
			if (action.equals(TeclaApp.ACTION_STOP_FS_SWITCH_MODE)) {
				TeclaStatic.logD(CLASS_TAG, "Received stop fullscreen switch mode intent.");
				stopFullScreenSwitchMode();
			}
			if (action.equals(Highlighter.ACTION_START_SCANNING)) {
				TeclaStatic.logD(CLASS_TAG, "Received start scanning IME intent.");
				evaluateStartScanning();
			}
			if (action.equals(Highlighter.ACTION_STOP_SCANNING)) {
				TeclaStatic.logD(CLASS_TAG, "Received stop scanning IME intent.");
				evaluateStartScanning();
			}
			if (action.equals(TeclaApp.ACTION_INPUT_STRING)) {
				TeclaStatic.logD(CLASS_TAG, "Received input string intent.");
				String input_string = intent.getExtras().getString(TeclaApp.EXTRA_INPUT_STRING);
				typeInputString(input_string);
			}
			if (action.equals(TeclaApp.ACTION_ENABLE_MORSE)) {
				TeclaStatic.logD(CLASS_TAG, "Received enable morse intent.");
				if (TeclaApp.highlighter.isSoftIMEShowing()) {
					hideSoftIME();
				}
				mLastFullKeyboardMode = KeyboardSwitcher.MODE_MORSE;
			}
			if (action.equals(TeclaApp.ACTION_DISABLE_MORSE)) {
				TeclaStatic.logD(CLASS_TAG, "Received disable morse intent.");
				if (TeclaApp.highlighter.isSoftIMEShowing()) {
					hideSoftIME();
				}
				mLastFullKeyboardMode = KeyboardSwitcher.MODE_TEXT;
			}
		}
	};
	
	private void commitTyped(InputConnection inputConnection) {
		if (mPredicting) {
			mPredicting = false;
			if (mComposing.length() > 0) {
				if (inputConnection != null) {
					inputConnection.commitText(mComposing, 1);
				}
				mCommittedLength = mComposing.length();
				TextEntryState.acceptedTyped(mComposing);
				mAutoDictionary.addWord(mComposing.toString(), FREQUENCY_FOR_TYPED);
			}
			updateSuggestions();
		}
	}

	private void postUpdateShiftKeyState(int delay) {
		mHandler.removeMessages(MSG_UPDATE_SHIFT_STATE);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_SHIFT_STATE), delay);
	}

	public void updateShiftKeyState(EditorInfo attr) {
		InputConnection ic = getCurrentInputConnection();
		if (attr != null && mIMEView != null && mKeyboardSwitcher.isAlphabetMode()
				&& ic != null) {
			int caps = 0;
			EditorInfo ei = getCurrentInputEditorInfo();
			if (mAutoCap && ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
				caps = ic.getCursorCapsMode(attr.inputType);
			}
			mIMEView.setShifted(mCapsLock || caps != 0);
		}
	}

	private void swapPunctuationAndSpace() {
		final InputConnection ic = getCurrentInputConnection();
		if (ic == null) return;
		CharSequence lastTwo = ic.getTextBeforeCursor(2, 0);
		if (lastTwo != null && lastTwo.length() == 2
				&& lastTwo.charAt(0) == KEYCODE_SPACE && isSentenceSeparator(lastTwo.charAt(1))) {
			ic.beginBatchEdit();
			ic.deleteSurroundingText(2, 0);
			ic.commitText(lastTwo.charAt(1) + " ", 1);
			ic.endBatchEdit();
			updateShiftKeyState(getCurrentInputEditorInfo());
		}
	}

	private void doubleSpace() {
		//if (!mAutoPunctuate) return;
		if (mCorrectionMode == Suggest.CORRECTION_NONE) return;
		final InputConnection ic = getCurrentInputConnection();
		if (ic == null) return;
		CharSequence lastThree = ic.getTextBeforeCursor(3, 0);
		if (lastThree != null && lastThree.length() == 3
				&& Character.isLetterOrDigit(lastThree.charAt(0))
				&& lastThree.charAt(1) == KEYCODE_SPACE && lastThree.charAt(2) == KEYCODE_SPACE) {
			ic.beginBatchEdit();
			ic.deleteSurroundingText(2, 0);
			ic.commitText(". ", 1);
			ic.endBatchEdit();
			updateShiftKeyState(getCurrentInputEditorInfo());
		}
	}

	public boolean addWordToDictionary(String word) {
		mUserDictionary.addWord(word, 128);
		return true;
	}

	private boolean isAlphabet(int code) {
		if (Character.isLetter(code)) {
			return true;
		} else {
			return false;
		}
	}

	// Implementation of KeyboardViewListener
	public void onKey(int primaryCode, int[] keyCodes) {
		long when = SystemClock.uptimeMillis();

		if (TeclaApp.persistence.isRepeatingKey()
				&& primaryCode != TeclaKeyboard.KEYCODE_REPEAT_LOCK
				&& !TeclaApp.persistence.isFullscreenSwitchEnabled()) stopRepeatingKey();

		if (keyCodes != null && keyCodes.length > 0) {
			TeclaStatic.logD(CLASS_TAG, "Keycode: " + keyCodes[0]);
		}
		
		if (primaryCode != Keyboard.KEYCODE_DELETE || 
				when > mLastKeyTime + QUICK_PRESS) {
			mDeleteCount = 0;
		}
		mLastKeyTime = when;
		/**
		 * Primary code processed with if-else statements for non constant comparisons,
		 * otherwise switch-case is used.
		 */
		if (primaryCode == TeclaKeyboardView.KEYCODE_STEPOUT) {
			TeclaApp.highlighter.externalstepOut();
			TeclaStatic.logD(CLASS_TAG, "Hidden key.Stepping out...");
		}/* else if (primaryCode == TeclaKeyboardView.KEYCODE_DICTATION) {
			//TODO: Add dictation actions here
			if(TeclaApp.desktop==null)
				TeclaApp.desktop=new TeclaDesktopClient(TeclaApp.getInstance());

			if(!TeclaApp.desktop.isConnected()&& !wifisearcherthread.isAlive()) {
				wifisearcherthread=new Thread(desktopsearcher);
				wifisearcherthread.start();
			}
			TeclaApp.dict_lock=TeclaApp.mSendToPC;
			TeclaApp.mSendToPC=false;
			if (TeclaApp.DEBUG) Log.d ("mSendToPC",""+TeclaApp.mSendToPC);
			TeclaApp.dictation_lock=new Object();				
			TeclaApp.getInstance().startVoiceDictation(RecognizerIntent.EXTRA_LANGUAGE_MODEL);
		} else if (primaryCode == TeclaKeyboardView.KEYCODE_SEND_TO_PC) {
			//TODO: Add send to pc handling here
			if(TeclaApp.desktop==null)
				TeclaApp.desktop=new TeclaDesktopClient(TeclaApp.getInstance());
			TeclaApp.mSendToPC=!TeclaApp.mSendToPC;
			if (TeclaApp.DEBUG)
				Log.d("voice","" + TeclaApp.desktop.isConnected() + " " + TeclaApp.mSendToPC + " "
						+ wifisearcherthread.isAlive() + " " + TeclaApp.connect_to_desktop);
			if(TeclaApp.mSendToPC && TeclaApp.connect_to_desktop && !TeclaApp.desktop.isConnected()&& !wifisearcherthread.isAlive()) {
				if (TeclaApp.DEBUG) Log.d("connection","entering new thread");
				wifisearcherthread=new Thread(wificonnector);
				wifisearcherthread.start();
				}
			else if(TeclaApp.desktop != null &&!TeclaApp.mSendToPC && TeclaApp.desktop.isConnected()){
				TeclaApp.desktop.disconnect();
			}
		}*/ else {
			switch (primaryCode) {
			case Keyboard.KEYCODE_DELETE:
				handleBackspace();
				mDeleteCount++;
				break;
			case Keyboard.KEYCODE_SHIFT:
				handleShift();
				if (mKeyboardSwitcher.isMorseMode()) {
					updateSpaceKey();
				}
				break;
			case Keyboard.KEYCODE_CANCEL:
				if (mOptionsDialog == null || !mOptionsDialog.isShowing()) {
					handleClose();
				}
				break;
			case TeclaKeyboardView.KEYCODE_OPTIONS:
				showOptionsMenu();
				break;
			case TeclaKeyboardView.KEYCODE_SHIFT_LONGPRESS:
				if (mCapsLock) {
					handleShift();
				} else {
					toggleCapsLock();
				}
				break;
			case Keyboard.KEYCODE_MODE_CHANGE:
				changeKeyboardMode();
				break;
			default:
				if (isMorseKeyboardKey(primaryCode)) {
					onKeyMorse(primaryCode);
				} else if (isWordSeparator(primaryCode)) {
					handleSeparator(primaryCode);
				} else if (isSpecialKey(primaryCode)) {
					handleSpecialKey(primaryCode);
				} else { 
					handleCharacter(primaryCode, keyCodes);
				}
				// Cancel the just reverted state
				mJustRevertedSeparator = null;
			}
		}
		if (mKeyboardSwitcher.onKey(primaryCode)) {
			changeKeyboardMode();
		}
		evaluateNavKbdTimeout();
	}

	
	/**
	 * Handles key input on the Morse Code keyboard
	 * @param primaryCode
	 */
	public void onKeyMorse(int primaryCode) {
		switch (primaryCode) {

		case TeclaKeyboard.KEYCODE_MORSE_DIT:
		case TeclaKeyboard.KEYCODE_MORSE_DAH:
			
			// Set a limit to the Morse sequence length
			if (mTeclaMorse.getCurrentChar().length() < mTeclaMorse.getMorseDictionary().getMaxCodeLength()) {

				if(primaryCode == TeclaKeyboard.KEYCODE_MORSE_DIT)
					mTeclaMorse.addDit();
				else
					mTeclaMorse.addDah();
				evaluateEndOfChar();
			}
			break;

		case TeclaKeyboard.KEYCODE_MORSE_SPACEKEY:
			if (TeclaApp.persistence.getMorseKeyMode() != SINGLE_KEY_MODE)
				handleMorseSpaceKey();
			break;

		case TeclaKeyboard.KEYCODE_MORSE_DELKEY:
			handleMorseBackspace(true);
			break;
		}

		updateSpaceKey();
		mIMEView.invalidate();
	}
	
	private void clearCharInProgress() {
		mTeclaMorse.clearCharInProgress();
	}
	
	/**
	 * Handles the space / end-of-character event (Morse keyboard only)
	 */	
	private void handleMorseSpaceKey() {
		String currentChar = mTeclaMorse.getCurrentChar();
		String curCharMatch = mTeclaMorse.morseToChar(currentChar);
		clearCharInProgress();
		
		if (currentChar.length() == 0) {
			getCurrentInputConnection().commitText(" ", 1);
			
		}
		else {
			if (curCharMatch != null) {

				if (curCharMatch.contentEquals("↵")) {
					sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
				} else if (curCharMatch.contentEquals("DEL")) {
					handleMorseBackspace(false);	
				} else if (curCharMatch.contentEquals("✓")) {
					updateSpaceKey();
					handleSpecialKey(Keyboard.KEYCODE_DONE);
				} else if (curCharMatch.contentEquals("space")) {
					getCurrentInputConnection().commitText(" ", 1);
				} else if (curCharMatch.contentEquals("⇪")) {
					int[] shift = {Keyboard.KEYCODE_SHIFT};
					emulateKeyPress(shift);
				} else if (curCharMatch.contentEquals("↶")) {
					updateSpaceKey();
					handleSpecialKey(KeyEvent.KEYCODE_BACK);
				} else if (curCharMatch.contentEquals("\\n")) {
					getCurrentInputConnection().commitText("\n", 1);
				} else {

					if (mIMEView.getKeyboard().isShifted()) {
						curCharMatch = curCharMatch.toUpperCase();
					}

					getCurrentInputConnection().commitText(curCharMatch, curCharMatch.length());
				}
			}
		}
	}

	/**
	 * Handles the backspace event (Morse keyboard only)
	 * @param clearEnabled
	 */
	private void handleMorseBackspace(boolean clearEnabled) {
		// If there's a character in progress, clear it
		// otherwise, send through a backspace keypress
		if (mTeclaMorse.getCurrentChar().length() > 0 && clearEnabled) {
			clearCharInProgress();
		}else {
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			clearCharInProgress();
			updateSpaceKey();
		}
	}
	
	/**
	 * Updates the state of the Space key (Morse keyboard only)
	 */
	private void updateSpaceKey() {
		String sequence = mTeclaMorse.getCurrentChar();
		String charac = mTeclaMorse.morseToChar(sequence);
		mSpaceKey = mIMEView.getKeyboard().getMorseSpaceKey();
		mSpaceKeyIndex = mIMEView.getKeyboard().getMorseSpaceKeyIndex();
		
		if (mSpaceKey == null)
			return;

		if (charac == null && sequence.length() > 0)
			mSpaceKey.label = sequence;

		else if (!sequence.equals("") &&
				sequence.length() <= mTeclaMorse.getMorseDictionary().getMaxCodeLength()) {
			//Update the key label according the current character
			if (mIMEView.getKeyboard().isShifted()) {
				charac = charac.toUpperCase();
			}
			mSpaceKey.label = charac + "  " + sequence;
			mSpaceKey.icon = null;
		}
		
		else {
			//Icon should take precedence over label, but it does not work,
			//so set label to null
			mSpaceKey.label = null;
			mSpaceKey.icon = TeclaApp.getInstance().getResources().getDrawable(R.drawable.sym_keyboard_space);
		}

		mIMEView.invalidateKey(mSpaceKeyIndex);
	}

	public void onText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();
		if (ic == null) return;
		ic.beginBatchEdit();
		if (mPredicting) {
			commitTyped(ic);
		}
		ic.commitText(text, 1);
		ic.endBatchEdit();
		updateShiftKeyState(getCurrentInputEditorInfo());
		mJustRevertedSeparator = null;
	}

	private void handleBackspace() {
		boolean deleteChar = false;
		InputConnection ic = getCurrentInputConnection();
		if (ic == null) return;
		if (mPredicting) {
			final int length = mComposing.length();
			if (length > 0) {
				mComposing.delete(length - 1, length);
				mWord.deleteLast();
				ic.setComposingText(mComposing, 1);
				if (mComposing.length() == 0) {
					mPredicting = false;
				}
				postUpdateSuggestions();
			} else {
				ic.deleteSurroundingText(1, 0);
			}
		} else {
			deleteChar = true;
		}
		postUpdateShiftKeyState(UPDATE_SHIFT_DELAY);
		TextEntryState.backspace();
		if (TextEntryState.getState() == TextEntryState.STATE_UNDO_COMMIT) {
			revertLastWord(deleteChar);
			return;
		} else if (deleteChar) {
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			if (mDeleteCount > DELETE_ACCELERATE_AT) {
				sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			}
		}
		mJustRevertedSeparator = null;
	}

	private void handleShift() {
		//Keyboard currentKeyboard = mIMEView.getKeyboard();
		if (mKeyboardSwitcher.isAlphabetMode()) {
			// Alphabet keyboard
			checkToggleCapsLock();
			mIMEView.setShifted(mCapsLock || !mIMEView.isShifted());
		} else {
			mKeyboardSwitcher.toggleShift();
		}
		mIMEView.invalidateKey(mIMEView.getKeyboard().getShiftKeyIndex());
	}

	private void handleCharacter(int primaryCode, int[] keyCodes) {
		CharSequence variants = null;
		TeclaKeyboard keyboard = mIMEView.getKeyboard();
		Key key = keyboard.getKeyFromKeyCode(primaryCode);
		if (key != null) variants = key.popupCharacters;
		if (TeclaApp.persistence.isVariantsKeyOn() && variants != null && variants.length() > 0) {
			// Key has variants!
			mWasSymbols = mKeyboardSwitcher.isSymbols();
			mWasShifted = keyboard.isShifted();
			TeclaApp.persistence.setVariantsShowing(true);
			switch (variants.length()) {
			case 1:
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_1X3);
				break;
			case 2:
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_1X4);
				break;
			case 3:
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_1X5);
				break;
			case 4:
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_1X6);
				break;
			case 5:
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_1X7);
				break;
			case 6:
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_1X8);
				break;
			case 7:
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_1X9);
				break;
			case 8:
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_1X10);
				break;
			}
			populateVariants(key.label, variants);
			mIMEView.getKeyboard().setShifted(mWasShifted);
			evaluateStartScanning();
		} else {
			if (isAlphabet(primaryCode) && isPredictionOn() && !isCursorTouchingWord()) {
				if (!mPredicting) {
					mPredicting = true;
					mComposing.setLength(0);
					mWord.reset();
				}
			}
			if (mIMEView.isShifted()) {
				// TODO: This doesn't work with ß, need to fix it in the next release.
				if (keyCodes == null || keyCodes[0] < Character.MIN_CODE_POINT
						|| keyCodes[0] > Character.MAX_CODE_POINT) {
					return;
				}
				primaryCode = new String(keyCodes, 0, 1).toUpperCase().charAt(0);
			}
			if (mPredicting) {
				if (mIMEView.isShifted() && mComposing.length() == 0) {
					mWord.setCapitalized(true);
				}
				mComposing.append((char) primaryCode);
				mWord.add(primaryCode, keyCodes);
				InputConnection ic = getCurrentInputConnection();
				if (ic != null) {
					ic.setComposingText(mComposing, 1);
				}
				postUpdateSuggestions();
			} else {
				sendKeyChar((char)primaryCode);
			}
			updateShiftKeyState(getCurrentInputEditorInfo());
			measureCps();
			TextEntryState.typedCharacter((char) primaryCode, isWordSeparator(primaryCode));
			if (mKeyboardSwitcher.isVariants()) {
				doVariantsExit(primaryCode);
				evaluateStartScanning();
			}
		}
	}
	
	private void handleSeparator(int primaryCode) {
		boolean pickedDefault = false;
		// Handle separator
		InputConnection ic = getCurrentInputConnection();
		if (ic != null) {
			ic.beginBatchEdit();
		}
		if (mPredicting) {
			// In certain languages where single quote is a separator, it's better
			// not to auto correct, but accept the typed word. For instance, 
			// in Italian dov' should not be expanded to dove' because the elision
			// requires the last vowel to be removed.
			if (mAutoCorrectOn && primaryCode != '\'' && 
					(mJustRevertedSeparator == null 
							|| mJustRevertedSeparator.length() == 0 
							|| mJustRevertedSeparator.charAt(0) != primaryCode)) {
				pickDefaultSuggestion();
				pickedDefault = true;
			} else {
				commitTyped(ic);
			}
		}
		sendKeyChar((char)primaryCode);
		TextEntryState.typedCharacter((char) primaryCode, true);
		if (TextEntryState.getState() == TextEntryState.STATE_PUNCTUATION_AFTER_ACCEPTED 
				&& primaryCode != KEYCODE_ENTER) {
			swapPunctuationAndSpace();
		} else if (isPredictionOn() && primaryCode == ' ') { 
			//else if (TextEntryState.STATE_SPACE_AFTER_ACCEPTED) {
			doubleSpace();
		}
		if (pickedDefault && mBestWord != null) {
			TextEntryState.acceptedDefault(mWord.getTypedWord(), mBestWord);
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
		if (ic != null) {
			ic.endBatchEdit();
		}
	}

	private void handleClose() {
		commitTyped(getCurrentInputConnection());
		requestHideSelf(0);
		mIMEView.closing();
		TextEntryState.endSession();
	}

	private void checkToggleCapsLock() {
		if (mIMEView.getKeyboard().isShifted()) {
			toggleCapsLock();
		}
	}

	private void toggleCapsLock() {
		mCapsLock = !mCapsLock;
		if (mKeyboardSwitcher.isAlphabetMode()) {
			mIMEView.getKeyboard().setShiftLocked(mCapsLock);
		}
	}

	private void postUpdateSuggestions() {
		mHandler.removeMessages(MSG_UPDATE_SUGGESTIONS);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_SUGGESTIONS), 100);
	}

	private boolean isPredictionOn() {
		boolean predictionOn = mPredictionOn;
		//if (isFullscreenMode()) predictionOn &= mPredictionLandscape;
		return predictionOn;
	}

	private boolean isCandidateStripVisible() {
		return isPredictionOn() && mShowSuggestions;
	}

	private void updateSuggestions() {
		// Check if we have a suggestion engine attached.
		if (mSuggest == null || !isPredictionOn()) {
			return;
		}

		if (!mPredicting) {
			mCandidateView.setSuggestions(null, false, false, false);
			return;
		}

		List<CharSequence> stringList = mSuggest.getSuggestions(mIMEView, mWord, false);
		boolean correctionAvailable = mSuggest.hasMinimalCorrection();
		//|| mCorrectionMode == mSuggest.CORRECTION_FULL;
		CharSequence typedWord = mWord.getTypedWord();
		// If we're in basic correct
		boolean typedWordValid = mSuggest.isValidWord(typedWord);
		if (mCorrectionMode == Suggest.CORRECTION_FULL) {
			correctionAvailable |= typedWordValid;
		}
		// Don't auto-correct words with multiple capital letter
		correctionAvailable &= !mWord.isMostlyCaps();

		mCandidateView.setSuggestions(stringList, false, typedWordValid, correctionAvailable); 
		if (stringList.size() > 0) {
			if (correctionAvailable && !typedWordValid && stringList.size() > 1) {
				mBestWord = stringList.get(1);
			} else {
				mBestWord = typedWord;
			}
		} else {
			mBestWord = null;
		}
		setCandidatesViewShown(isCandidateStripVisible() || mCompletionOn);
	}

	private void pickDefaultSuggestion() {
		// Complete any pending candidate query first
		if (mHandler.hasMessages(MSG_UPDATE_SUGGESTIONS)) {
			mHandler.removeMessages(MSG_UPDATE_SUGGESTIONS);
			updateSuggestions();
		}
		if (mBestWord != null) {
			TextEntryState.acceptedDefault(mWord.getTypedWord(), mBestWord);
			mJustAccepted = true;
			pickSuggestion(mBestWord);
		}
	}

	public void pickSuggestionManually(int index, CharSequence suggestion) {
		if (mCompletionOn && mCompletions != null && index >= 0
				&& index < mCompletions.length) {
			CompletionInfo ci = mCompletions[index];
			InputConnection ic = getCurrentInputConnection();
			if (ic != null) {
				ic.commitCompletion(ci);
			}
			mCommittedLength = suggestion.length();
			if (mCandidateView != null) {
				mCandidateView.clear();
			}
			updateShiftKeyState(getCurrentInputEditorInfo());
			return;
		}
		pickSuggestion(suggestion);
		TextEntryState.acceptedSuggestion(mComposing.toString(), suggestion);
		// Follow it with a space
		if (mAutoSpace) {
			sendSpace();
		}
		// Fool the state watcher so that a subsequent backspace will not do a revert
		TextEntryState.typedCharacter((char) KEYCODE_SPACE, true);
	}

	private void pickSuggestion(CharSequence suggestion) {
		if (mCapsLock) {
			suggestion = suggestion.toString().toUpperCase();
		} else if (preferCapitalization() 
				|| (mKeyboardSwitcher.isAlphabetMode() && mIMEView.isShifted())) {
			suggestion = suggestion.toString().toUpperCase().charAt(0)
			+ suggestion.subSequence(1, suggestion.length()).toString();
		}
		InputConnection ic = getCurrentInputConnection();
		if (ic != null) {
			ic.commitText(suggestion, 1);
		}
		// Add the word to the auto dictionary if it's not a known word
		if (mAutoDictionary.isValidWord(suggestion) || !mSuggest.isValidWord(suggestion)) {
			mAutoDictionary.addWord(suggestion.toString(), FREQUENCY_FOR_PICKED);
		}
		mPredicting = false;
		mCommittedLength = suggestion.length();
		if (mCandidateView != null) {
			mCandidateView.setSuggestions(null, false, false, false);
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	private boolean isCursorTouchingWord() {
		InputConnection ic = getCurrentInputConnection();
		if (ic == null) return false;
		CharSequence toLeft = ic.getTextBeforeCursor(1, 0);
		CharSequence toRight = ic.getTextAfterCursor(1, 0);
		if (!TextUtils.isEmpty(toLeft)
				&& !isWordSeparator(toLeft.charAt(0))) {
			return true;
		}
		if (!TextUtils.isEmpty(toRight) 
				&& !isWordSeparator(toRight.charAt(0))) {
			return true;
		}
		return false;
	}

	public void revertLastWord(boolean deleteChar) {
		final int length = mComposing.length();
		if (!mPredicting && length > 0) {
			final InputConnection ic = getCurrentInputConnection();
			mPredicting = true;
			ic.beginBatchEdit();
			mJustRevertedSeparator = ic.getTextBeforeCursor(1, 0);
			if (deleteChar) ic.deleteSurroundingText(1, 0);
			int toDelete = mCommittedLength;
			CharSequence toTheLeft = ic.getTextBeforeCursor(mCommittedLength, 0);
			if (toTheLeft != null && toTheLeft.length() > 0 
					&& isWordSeparator(toTheLeft.charAt(0))) {
				toDelete--;
			}
			ic.deleteSurroundingText(toDelete, 0);
			ic.setComposingText(mComposing, 1);
			TextEntryState.backspace();
			ic.endBatchEdit();
			postUpdateSuggestions();
		} else {
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			mJustRevertedSeparator = null;
		}
	}

	protected String getWordSeparators() {
		return mWordSeparators;
	}

	public boolean isWordSeparator(int code) {
		String separators = getWordSeparators();
		return separators.contains(String.valueOf((char)code));
	}

	public boolean isSentenceSeparator(int code) {
		return mSentenceSeparators.contains(String.valueOf((char)code));
	}

	private void sendSpace() {
		sendKeyChar((char)KEYCODE_SPACE);
		updateShiftKeyState(getCurrentInputEditorInfo());
		//onKey(KEY_SPACE[0], KEY_SPACE);
	}

	public boolean preferCapitalization() {
		return mWord.isCapitalized();
	}

	public void swipeRight() {
		if (TeclaKeyboardView.DEBUG_AUTO_PLAY) {
			ClipboardManager cm = ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE));
			CharSequence text = cm.getText();
			if (!TextUtils.isEmpty(text)) {
				mIMEView.startPlaying(text.toString());
			}
		}
	}

	public void swipeLeft() {
		//handleBackspace();
	}

	public void swipeDown() {
		handleClose();
	}

	public void swipeUp() {
		//launchSettings();
	}

	public void onPress(int primaryCode) {
		if (primaryCode == TeclaKeyboard.KEYCODE_MORSE_SPACEKEY) {
			if (TeclaApp.persistence.getMorseKeyMode() == SINGLE_KEY_MODE) {
				evaluateMorsePress();
			}
		}
		else {
			vibrate();
			playKeySound(primaryCode);
		}
	}

	public void onRelease(int primaryCode) {
		if (primaryCode == TeclaKeyboard.KEYCODE_MORSE_SPACEKEY) {
			if (TeclaApp.persistence.getMorseKeyMode() == SINGLE_KEY_MODE) {
				pauseMorseRepeating();
				evaluateEndOfChar();
			}
		}
		//vibrate();
	}

	// update flags for silent mode
	private void updateRingerMode() {
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		}
		if (mAudioManager != null) {
			mSilentMode = (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL);
		}
	}
	
	private void checkRingerMode() {
		// if mAudioManager is null, we don't have the ringer state yet
		// mAudioManager will be set by updateRingerMode
		if (mAudioManager == null) {
			if (mIMEView != null) {
				updateRingerMode();
			}
		}
	}

	private void playKeySound(int primaryCode) {
		checkRingerMode();
		if (mSoundOn && !mSilentMode) {
			// FIXME: Volume and enable should come from UI settings
			// FIXME: These should be triggered after auto-repeat logic
			int sound = AudioManager.FX_KEYPRESS_STANDARD;
			int duration;
			if (TeclaApp.persistence.getMorseKeyMode() == TRIPLE_KEY_MODE)
				duration = 100;
			else
				duration = TeclaApp.persistence.getMorseTimeUnit();
			
			switch (primaryCode) {
			case Keyboard.KEYCODE_DELETE:
				sound = AudioManager.FX_KEYPRESS_DELETE;
				mAudioManager.playSoundEffect(sound, FX_VOLUME);
				break;
			case KEYCODE_ENTER:
				sound = AudioManager.FX_KEYPRESS_RETURN;
				mAudioManager.playSoundEffect(sound, FX_VOLUME);
				break;
			case KEYCODE_SPACE:
				sound = AudioManager.FX_KEYPRESS_SPACEBAR;
				mAudioManager.playSoundEffect(sound, FX_VOLUME);
				break;
				
			case TeclaKeyboard.KEYCODE_MORSE_DIT:
				mTone.startTone(mToneType, duration);
				break;
			case TeclaKeyboard.KEYCODE_MORSE_DAH:
				mTone.startTone(mToneType, duration * 3);
				break;
			}
		}
	}

	private void vibrate() {
		if (!mVibrateOn) {
			return;
		}
		if (mVibrator == null) {
			mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		}
		mVibrator.vibrate(mVibrateDuration);
	}

	private void checkTutorial(String privateImeOptions) {
		if (privateImeOptions == null) return;
		if (privateImeOptions.equals("com.android.setupwizard:ShowTutorial")) {
			if (mTutorial == null) startTutorial();
		} else if (privateImeOptions.equals("com.android.setupwizard:HideTutorial")) {
			if (mTutorial != null) {
				if (mTutorial.close()) {
					mTutorial = null;
				}
			}
		}
	}

	private void startTutorial() {
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_START_TUTORIAL), 500);
	}

	void tutorialDone() {
		mTutorial = null;
	}

	void promoteToUserDictionary(String word, int frequency) {
		if (mUserDictionary.isValidWord(word)) return;
		mUserDictionary.addWord(word, frequency);
	}

	private void launchSettings() {
		handleClose();
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), TeclaPrefs.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private void loadSettings() {
		// Get the settings preferences
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		mVibrateOn = sp.getBoolean(PREF_VIBRATE_ON, false);
		mSoundOn = sp.getBoolean(PREF_SOUND_ON, false);
		mAutoCap = sp.getBoolean(PREF_AUTO_CAP, true);
		mQuickFixes = sp.getBoolean(PREF_QUICK_FIXES, true);
		// If there is no auto text data, then quickfix is forced to "on", so that the other options
		// will continue to work
		if (AutoText.getSize(mIMEView) < 1) mQuickFixes = true;
		//TODO: Tecla - changed default show_suggestions to false
		//      need to change back when the dictionary is ready!
		//mShowSuggestions = sp.getBoolean(PREF_SHOW_SUGGESTIONS, true) & mQuickFixes;
		mShowSuggestions = sp.getBoolean(PREF_SHOW_SUGGESTIONS, false) & mQuickFixes;
		boolean autoComplete = sp.getBoolean(PREF_AUTO_COMPLETE,
				getResources().getBoolean(R.bool.enable_autocorrect)) & mShowSuggestions;
		mAutoCorrectOn = mSuggest != null && (autoComplete || mQuickFixes);
		mCorrectionMode = autoComplete
		? Suggest.CORRECTION_FULL
				: (mQuickFixes ? Suggest.CORRECTION_BASIC : Suggest.CORRECTION_NONE);
	}

	private void showOptionsMenu() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setIcon(R.drawable.ic_dialog_keyboard);
		builder.setNegativeButton(android.R.string.cancel, null);
		CharSequence itemSettings = getString(R.string.english_ime_settings);
		CharSequence itemInputMethod = getString(R.string.inputMethod);
		builder.setItems(new CharSequence[] {
				itemSettings, itemInputMethod},
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface di, int position) {
				di.dismiss();
				switch (position) {
				case POS_SETTINGS:
					launchSettings();
					break;
				case POS_METHOD:
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.showInputMethodPicker();
					break;
				}
			}
		});
		builder.setTitle(getResources().getString(R.string.english_ime_name));
		mOptionsDialog = builder.create();
		Window window = mOptionsDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.token = mIMEView.getWindowToken();
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		window.setAttributes(lp);
		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		mOptionsDialog.show();
	}

	private void changeKeyboardMode() {
		mKeyboardSwitcher.toggleSymbols();
		if (mCapsLock && mKeyboardSwitcher.isAlphabetMode()) {
			mIMEView.getKeyboard().setShiftLocked(mCapsLock);
		}

		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	@Override protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
		super.dump(fd, fout, args);

		final Printer p = new PrintWriterPrinter(fout);
		p.println("TeclaIME state :");
		p.println("  Keyboard mode = " + mKeyboardSwitcher.getKeyboardMode());
		p.println("  mCapsLock=" + mCapsLock);
		p.println("  mComposing=" + mComposing.toString());
		p.println("  mPredictionOn=" + mPredictionOn);
		p.println("  mCorrectionMode=" + mCorrectionMode);
		p.println("  mPredicting=" + mPredicting);
		p.println("  mAutoCorrectOn=" + mAutoCorrectOn);
		p.println("  mAutoSpace=" + mAutoSpace);
		p.println("  mCompletionOn=" + mCompletionOn);
		p.println("  TextEntryState.state=" + TextEntryState.getState());
		p.println("  mSoundOn=" + mSoundOn);
		p.println("  mVibrateOn=" + mVibrateOn);
	}

	// Characters per second measurement
	
	private static final boolean PERF_DEBUG = false;
	private long mLastCpsTime;
	private static final int CPS_BUFFER_SIZE = 16;
	private long[] mCpsIntervals = new long[CPS_BUFFER_SIZE];
	private int mCpsIndex;

	private void measureCps() {
		if (!TeclaIME.PERF_DEBUG) return;
		long now = System.currentTimeMillis();
		if (mLastCpsTime == 0) mLastCpsTime = now - 100; // Initial
		mCpsIntervals[mCpsIndex] = now - mLastCpsTime;
		mLastCpsTime = now;
		mCpsIndex = (mCpsIndex + 1) % CPS_BUFFER_SIZE;
		long total = 0;
		for (int i = 0; i < CPS_BUFFER_SIZE; i++) total += mCpsIntervals[i];
		System.out.println("CPS = " + ((CPS_BUFFER_SIZE * 1000f) / total));
	}

	class AutoDictionary extends ExpandableDictionary {
		// If the user touches a typed word 2 times or more, it will become valid.
		private static final int VALIDITY_THRESHOLD = 2 * FREQUENCY_FOR_PICKED;
		// If the user touches a typed word 5 times or more, it will be added to the user dict.
		private static final int PROMOTION_THRESHOLD = 5 * FREQUENCY_FOR_PICKED;

		public AutoDictionary(Context context) {
			super(context);
		}

		@Override
		public boolean isValidWord(CharSequence word) {
			final int frequency = getWordFrequency(word);
			return frequency > VALIDITY_THRESHOLD;
		}

		@Override
		public void addWord(String word, int addFrequency) {
			final int length = word.length();
			// Don't add very short or very long words.
			if (length < 2 || length > getMaxWordLength()) return;
			super.addWord(word, addFrequency);
			final int freq = getWordFrequency(word);
			if (freq > PROMOTION_THRESHOLD) {
				TeclaIME.this.promoteToUserDictionary(word, FREQUENCY_FOR_AUTO_ADD);
			}
		}
	}

	//TECLA CONSTANTS AND VARIABLES
	/**
	 * Tag used for logging in this class
	 */
	private static final String CLASS_TAG = "IME: ";

	//TODO: Try moving these variables to TeclaApp class
	private String mVoiceInputString;
	private int mLastKeyboardMode, mLastFullKeyboardMode;
	private boolean mShieldConnected;
	private PopupWindow mSwitchPopup;
	private View mSwitch;
	private Handler mTeclaHandler;
	private int[] mKeyCodes;
	private boolean mIsNavKbdTimedOut;
	private boolean mWasSymbols, mWasShifted;

	private void initTeclaA11y() {

		// register to receive switch events from Tecla shield
		registerReceiver(mReceiver, new IntentFilter(TeclaShieldService.ACTION_SHIELD_CONNECTED));
		registerReceiver(mReceiver, new IntentFilter(TeclaShieldService.ACTION_SHIELD_DISCONNECTED));
		registerReceiver(mReceiver, new IntentFilter(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED));
		registerReceiver(mReceiver, new IntentFilter(TeclaApp.ACTION_SHOW_IME));
		registerReceiver(mReceiver, new IntentFilter(TeclaApp.ACTION_HIDE_IME));
		registerReceiver(mReceiver, new IntentFilter(TeclaApp.ACTION_ENABLE_MORSE));
		registerReceiver(mReceiver, new IntentFilter(TeclaApp.ACTION_DISABLE_MORSE));
		registerReceiver(mReceiver, new IntentFilter(TeclaApp.ACTION_START_FS_SWITCH_MODE));
		registerReceiver(mReceiver, new IntentFilter(TeclaApp.ACTION_STOP_FS_SWITCH_MODE));
		registerReceiver(mReceiver, new IntentFilter(Highlighter.ACTION_START_SCANNING));
		registerReceiver(mReceiver, new IntentFilter(Highlighter.ACTION_STOP_SCANNING));
		registerReceiver(mReceiver, new IntentFilter(TeclaApp.ACTION_INPUT_STRING));

		 mLastFullKeyboardMode = TeclaApp.persistence.isMorseModeEnabled() ? KeyboardSwitcher.MODE_MORSE : KeyboardSwitcher.MODE_TEXT;
		 mTeclaHandler = new Handler();
		 mShieldConnected = false;
		 mWasSymbols = false;
		 mWasShifted = false;
		 
		if (TeclaApp.persistence.isPersistentKeyboardEnabled()) {
			TeclaApp.getInstance().queueSplash();
		}

	}
	
	private void typeInputString(String input_string) {
		Log.d(TeclaApp.TAG, CLASS_TAG + "Received input string: " + input_string);
		mVoiceInputString = input_string;
		mTeclaHandler.removeCallbacks(mAutoPlayRunnable);
		mTeclaHandler.postDelayed(mAutoPlayRunnable, 1000);
	}
	
	public Runnable mAutoPlayRunnable = new Runnable() {

		public void run() {
			//mIMEView.startPlaying(mVoiceInputString);
			onText(mVoiceInputString);
		}
		
	};
	
	//Used to track the duration of a single-key press 
	private void startTimer() {
		mMorseStartTime = System.currentTimeMillis();
	}
	
	/**
	 * Starts the Morse repeat runnable
	 * @param delay
	 */
	public void evaluateRepeating(long delay) {
		pauseMorseRepeating();
		mTeclaHandler.postDelayed(mStartMorseRepeatRunnable, delay);
	}
	
	/**
	 * Evaluates a Morse key press, based on the current key mode
	 */
	public void evaluateMorsePress() {
		switch(TeclaApp.persistence.getMorseKeyMode()) {
		case TRIPLE_KEY_MODE:
			evaluateRepeating(0);
			break;
			
		case DOUBLE_KEY_MODE:
			int[] key = {mRepeatedKey};
			emulateKeyPress(key);
			break;
			
		case SINGLE_KEY_MODE:
			startTimer();
			//Play audio feedback while key is pressed
			checkRingerMode();
			if (mSoundOn && !mSilentMode)
				mTone.startTone(mToneType);
			break;
		}
	}
	
	/**
	 * Handles a Morse key-up event in single-key mode
	 * @return true if a dit/dah has been added, false otherwise
	 */
	private boolean handleSingleKeyUp() {
		boolean addedDitDah = false;
		mTone.stopTone();
		long duration = System.currentTimeMillis() - mMorseStartTime;

		if (mTeclaMorse.getCurrentChar().length() < mTeclaMorse.getMorseDictionary().getMaxCodeLength()) {
			if (duration < TeclaApp.persistence.getMorseTimeUnit() * ERROR_MARGIN) {
				mTeclaMorse.addDit();
				addedDitDah = true;
			}

			else if (duration < (TeclaApp.persistence.getMorseTimeUnit() * 3) * ERROR_MARGIN) {
				mTeclaMorse.addDah();
				addedDitDah = true;
			}
		}
		
		updateSpaceKey();
		mIMEView.invalidate();
		return addedDitDah;
	}

	/**
	 * Pauses Morse repeating process
	 */
	public void pauseMorseRepeating() {
		mTeclaHandler.removeCallbacks(mRepeatMorseRunnable);
		mTeclaHandler.removeCallbacks(mStartMorseRepeatRunnable);
		mTeclaHandler.removeCallbacks(mMorseEndOfCharRunnable);
	}
	
	/**
	 * Runnable used to repeat an occurence of a Morse key
	 */
	private Runnable mRepeatMorseRunnable = new Runnable() {
		public void run() {
			final long start = SystemClock.uptimeMillis();
			int[] key = {mRepeatedKey};
			emulateKeyPress(key);
			mTeclaHandler.postAtTime(this, start + TeclaApp.persistence.getRepeatFrequency());
		}
	};
	
	/**
	 * Runnable used to start the Morse key repetition process
	 */
	private Runnable mStartMorseRepeatRunnable = new Runnable() {
		public void run() {
			int[] key = {mRepeatedKey};
			emulateKeyPress(key);
			int frequency = TeclaApp.persistence.getRepeatFrequency();
			if (frequency != Persistence.NEVER_REPEAT)
				mTeclaHandler.postDelayed(mRepeatMorseRunnable, frequency);
		}
	};
	
	/**
	 * Runnable used to process a Morse end-of-character event
	 */
	private Runnable mMorseEndOfCharRunnable = new Runnable() {
		public void run() {
			handleMorseSpaceKey();
			updateSpaceKey();
			mIMEView.invalidate();
		}
	};
	
	/**
	 * Evalutes the Morse end-of-character event, based on the current key mode
	 */
	private void evaluateEndOfChar() {
		switch (TeclaApp.persistence.getMorseKeyMode()) {
		case TRIPLE_KEY_MODE:
			break;
		

		case DOUBLE_KEY_MODE:
			mTeclaHandler.removeCallbacks(mMorseEndOfCharRunnable);
			mTeclaHandler.postDelayed(mMorseEndOfCharRunnable, 3 * TeclaApp.persistence.getMorseTimeUnit());
			break;

		case SINGLE_KEY_MODE:
			if (handleSingleKeyUp() == true) {
				mTeclaHandler.removeCallbacks(mMorseEndOfCharRunnable);
				mTeclaHandler.postDelayed(mMorseEndOfCharRunnable, 3 * TeclaApp.persistence.getMorseTimeUnit());
			}
			break;
		}
	}
	
	/**
	 * Handler of Morse switch events
	 * @param switchEvent
	 * @param action
	 */
	private void handleMorseSwitch(SwitchEvent switchEvent, int action) {
		switch(action) {

		case 1:
			//Add a dit to the current Morse sequence (repeatable)
			if (switchEvent.isPressed(switchEvent.getSwitchChanges())) {
				mRepeatedKey = TeclaKeyboard.KEYCODE_MORSE_DIT;
				evaluateMorsePress();
			}
			if (switchEvent.isReleased(switchEvent.getSwitchChanges())) {
				pauseMorseRepeating();
				evaluateEndOfChar();
			}
			break;

		case 2:
			//Add a dah to the current Morse sequence (repeatable)
			if (switchEvent.isPressed(switchEvent.getSwitchChanges())) {
				mRepeatedKey = TeclaKeyboard.KEYCODE_MORSE_DAH;
				evaluateMorsePress();
			}
			if (switchEvent.isReleased(switchEvent.getSwitchChanges())) {
				pauseMorseRepeating();
				evaluateEndOfChar();
			}
			break;

		case 3:
			//Send through a space key event
			if (switchEvent.isPressed(switchEvent.getSwitchChanges())) {
				int[] key = {TeclaKeyboard.KEYCODE_MORSE_SPACEKEY};
				emulateKeyPress(key);
			}
			break;

		case 4:
			//Send through a backspace event (repeatable)
			if (switchEvent.isPressed(switchEvent.getSwitchChanges())) {
				mRepeatedKey = TeclaKeyboard.KEYCODE_MORSE_DELKEY;
				evaluateMorsePress();
			}
			if (switchEvent.isReleased(switchEvent.getSwitchChanges())) {
				pauseMorseRepeating();
			}
			break;

		case 5:
			//Hide the Morse IME view
			if (switchEvent.isPressed(switchEvent.getSwitchChanges())) {
				int[] key = {Keyboard.KEYCODE_DONE};
				emulateKeyPress(key);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * Switch events are processed here
	 * @param switchEvent
	 */
	private void handleSwitchEvent(SwitchEvent switchEvent) {
		
		if (switchEvent.isAnyPressed()) {
			if (TeclaApp.persistence.isRepeatingKey()) stopRepeatingKey();
		}
		
		//Emulator issue (temporary fix): if typing too fast, or holding a long press
		//while in auto-release mode, some switch events are null
		if (switchEvent.toString() == null) {
			Log.w(TeclaApp.TAG, "Captured null switch event");
			return;
		}

		cancelNavKbdTimeout();
		if (!TeclaApp.highlighter.isSoftIMEShowing() && TeclaApp.persistence.isPersistentKeyboardEnabled()) {
			showIMEView();
			evaluateStartScanning();
		} else {
			
			//Collect the mapped actions of the current switch
			String[] switchActions = TeclaApp.persistence.getSwitchMap().get(switchEvent.toString());
			
			if (mKeyboardSwitcher.isMorseMode()) {
				//Switches have different actions when Morse keyboard is showing
				handleMorseSwitch(switchEvent, Integer.parseInt(switchActions[1]));
			}
			
			else {
				String action_tecla = switchActions[0];
				TeclaStatic.logI(TeclaApp.TAG, CLASS_TAG + "action_tecla: "+action_tecla.toString());
				switch(Integer.parseInt(action_tecla)) {

				case SwitchEvent.ACTION_NEXT:
					if (switchEvent.isPressed(switchEvent.getSwitchChanges()))
						TeclaApp.highlighter.move(Highlighter.HIGHLIGHT_NEXT);
					break;

				case SwitchEvent.ACTION_PREV:
					if (switchEvent.isPressed(switchEvent.getSwitchChanges()))
						TeclaApp.highlighter.move(Highlighter.HIGHLIGHT_PREV);
					break;

				case SwitchEvent.ACTION_CANCEL:
					if (switchEvent.isPressed(switchEvent.getSwitchChanges()))
						TeclaApp.highlighter.stepOut();
					break;

				case SwitchEvent.ACTION_SELECT:
					if (switchEvent.isPressed(switchEvent.getSwitchChanges())) {
						if (TeclaApp.persistence.isInverseScanningEnabled()) {
							TeclaApp.highlighter.resumeSelfScanning();
						} else {
							selectHighlighted(true);
						}
					}
					if (switchEvent.isReleased(switchEvent.getSwitchChanges())) {
						if (TeclaApp.persistence.isInverseScanningEnabled()) {
							if (TeclaApp.persistence.isInverseScanningChanged()) {
								//Ignore event right after Inverse Scanning is Enabled
								TeclaApp.persistence.unsetInverseScanningChanged();
								Log.w(TeclaApp.TAG, CLASS_TAG + "Ignoring switch event because Inverse Scanning was just enabled");
							} else {
								selectHighlighted(false);
							}
						}
					}
					break;
					
				case SwitchEvent.ACTION_EMERGENCY:
					TeclaApp.emergencyCallout.Callout(); 
					break;
					
				default:
					break;
				}
			}
			
			TeclaStatic.logD(CLASS_TAG, "Switch event received: " +
					TeclaApp.getInstance().byte2Hex(switchEvent.getSwitchChanges()) + ":" +
					TeclaApp.getInstance().byte2Hex(switchEvent.getSwitchStates()));
			
			TeclaStatic.logD(CLASS_TAG, "Byte handled: " +
					TeclaApp.getInstance().byte2Hex(switchEvent.getSwitchStates()) + " at " + SystemClock.uptimeMillis());
		}
		
		evaluateNavKbdTimeout();		
	}
	
	/**
	 * Determine weather the current keyboard should auto-hide.
	 */
	private void evaluateNavKbdTimeout() {
		if(mKeyboardSwitcher.isNavigation()) {
			resetNavKbdTimeout();
		} else {
			cancelNavKbdTimeout();
		}
	}

	/**
	 * Cancel any currently active calls to auto-hide the keyboard.
	 */
	private void cancelNavKbdTimeout() {
		mIsNavKbdTimedOut = false;
		mTeclaHandler.removeCallbacks(hideNavKbdRunnable);
	}

	/**
	 * Do not use this method, use {@link #evaluateNavKbdTimeout()} instead.
	 */
	private void resetNavKbdTimeout() {
		cancelNavKbdTimeout();
		int navKbdTimeout = TeclaApp.persistence.getNavigationKeyboardTimeout();
		TeclaStatic.logD(CLASS_TAG, "Navigation keyboard timeout in: " + navKbdTimeout + " seconds");
		if (navKbdTimeout != Persistence.NEVER_AUTOHIDE)
			mTeclaHandler.postDelayed(hideNavKbdRunnable, navKbdTimeout * 1000);
	}

	private Runnable hideNavKbdRunnable = new Runnable() {
		public void run() {
			TeclaStatic.logD(CLASS_TAG, "Navigation keyboard timed out!");
			mIsNavKbdTimedOut = true;
			hideSoftIME();
		}
	};

	/**
	 * Select the currently highlighted item.
	 * @param repeat true if a key should be repeated on hold, false otherwise.
	 */
	private void selectHighlighted(Boolean repeat) {
		//FIXME: Repeat key has been implemented only for navigation keys...
		// will disable it here for now.
		repeat = false;
		TeclaApp.highlighter.pauseSelfScanning();
		if (TeclaApp.highlighter.getScanDepth() == Highlighter.DEPTH_KEY) {
			//Selected item is a key
			mKeyCodes = TeclaApp.highlighter.getCurrentKey().codes;
			if (repeat && isRepeatableWithTecla(mKeyCodes[0])) {
				mTeclaHandler.post(mRepeatKeyRunnable);
			} else {
				emulateKeyPress(mKeyCodes);
				TeclaApp.highlighter.doSelectKey(mKeyCodes[0]);
			}
		} else {
			//Selected item is a row
			TeclaApp.highlighter.doSelectRow();
		}
	}
	
	private boolean isMorseKeyboardKey(int keycode) {
		return (keycode == TeclaKeyboard.KEYCODE_MORSE_DIT)
				|| (keycode == TeclaKeyboard.KEYCODE_MORSE_DAH)
				|| (keycode == TeclaKeyboard.KEYCODE_MORSE_SPACEKEY)
				|| (keycode == TeclaKeyboard.KEYCODE_MORSE_DELKEY);
	}

	private boolean isSpecialKey(int keycode) {
		return ((keycode>=KeyEvent.KEYCODE_DPAD_UP)
				&& (keycode<=KeyEvent.KEYCODE_DPAD_CENTER))
				|| (keycode == KeyEvent.KEYCODE_BACK)
				|| (keycode == KeyEvent.KEYCODE_SEARCH)
				|| (keycode == KeyEvent.KEYCODE_VOLUME_DOWN)
				|| (keycode == KeyEvent.KEYCODE_VOLUME_UP)
				|| (keycode == Keyboard.KEYCODE_DONE)
				|| (keycode == TeclaKeyboard.KEYCODE_REPEAT_LOCK)
				|| (keycode == TeclaKeyboard.KEYCODE_VOICE)
				|| (keycode == TeclaKeyboard.KEYCODE_VARIANTS)
				|| (keycode == TeclaKeyboard.KEYCODE_ALTNAV);
	}

	private void handleSpecialKey(int keyEventCode) {
		TeclaKeyboard keyboard = mIMEView.getKeyboard();
		if ((keyEventCode == KeyEvent.KEYCODE_SEARCH)
				|| (keyEventCode == TeclaKeyboard.KEYCODE_VOICE)) {
			hideAltNavKeyboard();
		}
		if (keyEventCode == Keyboard.KEYCODE_DONE) {
			if (!mKeyboardSwitcher.isNavigation() && !mKeyboardSwitcher.isVariants()) {
				// Closing
				mLastFullKeyboardMode = mKeyboardSwitcher.getKeyboardMode();
				mWasShifted = keyboard.isShifted();
				hideSoftIME();
			} else {
				// Opening
				if (mKeyboardSwitcher.isVariants()) {
					doVariantsExit(keyEventCode);
				} else {
					mKeyboardSwitcher.setKeyboardMode(mLastFullKeyboardMode);
					keyboard.setShifted(mWasShifted);
				}
				
				evaluateStartScanning();
			}
		} else if (keyEventCode == KeyEvent.KEYCODE_BACK) {
			if (mKeyboardSwitcher.isMorseMode()) {
				hideSoftIME();
			}
			keyDownUp(keyEventCode);
		} else if (keyEventCode ==  TeclaKeyboard.KEYCODE_REPEAT_LOCK) {
			if (TeclaApp.persistence.isRepeatLockOn()) {
				stopRepeatingKey();
			} else {
				TeclaApp.persistence.setRepeatLockOn();
				redrawKeyboard();
			}
		} else if (keyEventCode ==  TeclaKeyboard.KEYCODE_VARIANTS) {
			if (TeclaApp.persistence.isVariantsKeyOn()) {
				TeclaApp.persistence.setVariantsKeyOff();
			} else {
				TeclaApp.persistence.setVariantsKeyOn();
			}
			redrawKeyboard();
		} else if (keyEventCode == TeclaKeyboard.KEYCODE_VOICE) {
			if (mKeyboardSwitcher.isNavigation()) {
				TeclaApp.getInstance().startVoiceActions();
			} else {
				TeclaApp.getInstance().startVoiceInput(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			}
		} else if (keyEventCode ==  TeclaKeyboard.KEYCODE_ALTNAV) {
			TeclaApp.persistence.setAltNavKeyboardOn(!TeclaApp.persistence.isAltNavKeyboardOn());
			redrawKeyboard();
		} else {
			if (TeclaApp.persistence.isRepeatLockOn() && !TeclaApp.persistence.isRepeatingKey() && isRepeatableWithTecla(keyEventCode)) {
				startRepeatingKey(keyEventCode);
			} else {
				keyDownUp(keyEventCode);
			}
		}
	}
	
	private void redrawKeyboard () {
		mLastKeyboardMode = mKeyboardSwitcher.getKeyboardMode();
		mKeyboardSwitcher.setKeyboardMode(mLastKeyboardMode);
		TeclaKeyboard keyboard = mIMEView.getKeyboard();
		keyboard.updateVariantsState();
		keyboard.updateRepeatLockState();
		mIMEView.invalidateAllKeys();
	}
	
	private void hideAltNavKeyboard() {
		if (TeclaApp.persistence.isAltNavKeyboardOn()) {
			TeclaApp.persistence.setAltNavKeyboardOn(false);
		}
		mKeyboardSwitcher.setKeyboardMode(mLastKeyboardMode);
	}

	/**
	 * Helper to send a key down / key up pair to the current editor.
	 */
	private void keyDownUp(int keyEventCode) {
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}

	private boolean isRepeatableWithTecla(int code) {
		if (code == TeclaKeyboard.KEYCODE_DONE ||
				code == TeclaKeyboard.KEYCODE_VOICE ||
				code == TeclaKeyboard.KEYCODE_VARIANTS) {
			return false;
		}
		return true;
	}
	
	private void startRepeatingKey(int keycode) {
		mTeclaHandler.removeCallbacks(mRepeatKeyRunnable);
		mRepeatingKeyCode = keycode;
		TeclaApp.persistence.setRepeatingKey(true);
		mTeclaHandler.post(mRepeatKeyRunnable);
	}
	
	private void stopRepeatingKey() {
		mTeclaHandler.removeCallbacks(mRepeatKeyRunnable);
		TeclaApp.persistence.setRepeatingKey(false);
		TeclaApp.persistence.setRepeatLockOff();
		redrawKeyboard();
	}

	private Runnable mRepeatKeyRunnable = new Runnable() {
		public void run() {
			keyDownUp(mRepeatingKeyCode);
			mTeclaHandler.postDelayed(mRepeatKeyRunnable, TeclaApp.persistence.getScanDelay());
		}
	};

	private void emulateKeyPress(int[] key_codes) {
		// Process key as if it had been pressed
		onKey(key_codes[0], key_codes);
		if (mKeyboardSwitcher.isMorseMode())
			playKeySound(key_codes[0]);
	}


	private void startFullScreenSwitchMode(int delay) {
		mTeclaHandler.removeCallbacks(mCreateSwitchRunnable);
		mTeclaHandler.postDelayed(mCreateSwitchRunnable, delay);
		TeclaStatic.logD(CLASS_TAG, "Sent delayed broadcast to show fullscreen switch");
	}
	
	/**
	 * Runnable used to create full-screen switch overlay
	 */
	private Runnable mCreateSwitchRunnable = new Runnable () {

		public void run() {
			if (TeclaApp.highlighter.isSoftIMEShowing()) {
				Display display = getDisplay();
				if (mSwitchPopup == null) {
					//Create single-switch pop-up
					mSwitch = getLayoutInflater().inflate(R.layout.popup_fullscreen_transparent, null);
					mSwitch.setOnTouchListener(mSwitchTouchListener);
					mSwitch.setOnClickListener(mSwitchClickListener);
					mSwitch.setOnLongClickListener(mSwitchLongPressListener);
					mSwitchPopup = new PopupWindow(mSwitch);
				}
				if (mSwitchPopup.isShowing()) mSwitchPopup.dismiss();
				mSwitchPopup.setWidth(display.getWidth());
				mSwitchPopup.setHeight(display.getHeight());
				mSwitchPopup.showAtLocation(mIMEView, Gravity.NO_GRAVITY, 0, 0);
				TeclaApp.getInstance().showToast(R.string.fullscreen_enabled);
				TeclaStatic.logD(CLASS_TAG, "Fullscreen switch shown");
				evaluateStartScanning();
			} else {
				startFullScreenSwitchMode(1000);
			}
		}
	};

	/**
	 * Listener for full-screen single switch long press
	 */
	private View.OnLongClickListener mSwitchLongPressListener = new View.OnLongClickListener() {
		
		public boolean onLongClick(View v) {
			if (!TeclaApp.persistence.isInverseScanningEnabled()) {
				launchSettings();
				//Doing this here again because the ACTION_UP event in the onTouch listener doesn't always work.
				mSwitch.setBackgroundResource(R.drawable.screen_switch_background_normal);
				mSwitchPopup.setBackgroundDrawable(null);
				return true;
			}
			return false;
		}
	};

	/**
	 * Listener for full-screen switch actions
	 */
	private View.OnTouchListener mSwitchTouchListener = new View.OnTouchListener() {
		
		public boolean onTouch(View v, MotionEvent event) {
			int changes = SwitchEvent.MASK_SWITCH_E1;
			int states = SwitchEvent.SWITCH_STATES_DEFAULT;
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				states &= ~changes;
				injectSwitchEvent(changes, states); //Primary switch pressed
				TeclaStatic.logD(CLASS_TAG, "Fullscreen switch down!");
				mSwitchPopup.setBackgroundDrawable(new ColorDrawable(R.color.switch_pressed));
				mSwitch.setBackgroundResource(R.drawable.screen_switch_background_pressed);
				break;
			case MotionEvent.ACTION_UP:
				injectSwitchEvent(changes, states); //Primary switch released
				TeclaStatic.logD(CLASS_TAG, "Fullscreen switch up!");
				mSwitch.setBackgroundResource(R.drawable.screen_switch_background_normal);
				mSwitchPopup.setBackgroundDrawable(null);
				break;
			default:
				break;
			}
			return false;
		}
	};

	private View.OnClickListener mSwitchClickListener =  new View.OnClickListener() {
		
		public void onClick(View v) {
			//Doing this here again because the ACTION_UP event in the onTouch listener doesn't always work.
			mSwitch.setBackgroundResource(R.drawable.screen_switch_background_normal);
			mSwitchPopup.setBackgroundDrawable(null);
		}
	};

	private void stopFullScreenSwitchMode() {
		if (isFullScreenShowing()) {
			mSwitchPopup.dismiss();
		}
		evaluateStartScanning();
		TeclaApp.getInstance().showToast(R.string.fullscreen_disabled);
	}
	
	private boolean isFullScreenShowing() {
		if (mSwitchPopup != null) {
			if (mSwitchPopup.isShowing())
				return true;
		}
		return false;
	}
	
	private void evaluateStartScanning() {
		if (TeclaApp.highlighter.isSoftIMEShowing()) {
			if (mShieldConnected || isFullScreenShowing()) {
				TeclaApp.highlighter.startSelfScanning();
			} else {
				TeclaApp.highlighter.stopSelfScanning();
			}
		} else {
			Log.w(TeclaApp.TAG, CLASS_TAG + "Could not reset scanning, InputView is not ready!");
		}
	}
	
	private Display getDisplay() {
		return ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	}
	
	private boolean shouldShowIME() {
		return TeclaApp.persistence.isPersistentKeyboardEnabled();
	}

	private void showIMEView() {
		if (TeclaApp.highlighter.isSoftIMEShowing()) {
			TeclaStatic.logD(CLASS_TAG, "Soft IME is already showing");
		} else {
			showWindow(true);
			updateInputViewShown();
			
			// Fixes https://github.com/jorgesilva/TeclaAccess/issues/3
			if (TeclaApp.highlighter.isSoftIMEShowing()) {
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_NAV);
			}
			// This call causes a looped intent call until the IME View is created
			TeclaApp.getInstance().callShowSoftIMEWatchDog(350);
		}
	}
	
	private void hideSoftIME() {
		hideAltNavKeyboard();
		hideWindow();
		updateInputViewShown();
	}
	
	// TODO: Consider moving to TeclaKeyboardView or TeclaKeyboard
	private void populateVariants (CharSequence keyLabel, CharSequence popupChars) {
		List<Key> keyList = mIMEView.getKeyboard().getKeys();
		Key key = keyList.get(1);
		CharSequence sequence;
		
		key.label = keyLabel;
		key.codes = new int[1];
		key.codes[0] = (int) keyLabel.charAt(0);
		for (int i=0; i < popupChars.length(); i++) {
			key = keyList.get(i+2);
			sequence = popupChars.subSequence(i, i+1);
			key.label = sequence;
			key.codes = new int[1];
			key.codes[0] = (int) sequence.charAt(0);
			TeclaStatic.logD(CLASS_TAG, "Populating char: " + sequence.toString());
		}
	}

	private void doVariantsExit(int keyCode) {
		TeclaApp.persistence.setVariantsShowing(false);
		mKeyboardSwitcher.setKeyboardMode(mLastFullKeyboardMode);
		if (mWasSymbols && !mKeyboardSwitcher.isSymbols()) {
			mKeyboardSwitcher.toggleSymbols();
		}
		if (mWasShifted && mWasSymbols) {
			handleShift();
		} else {
			mIMEView.setShifted(mWasShifted);
		}
		if (keyCode != TeclaKeyboard.KEYCODE_DONE) {
			TeclaApp.persistence.setVariantsKeyOff();
			Key key = mIMEView.getKeyboard().getVariantsKey();
			key.on = false;
		}
	}

	public KeyboardSwitcher getKeyboardSwitcher() {
		return mKeyboardSwitcher;
	}
	
/*	public void ConnectToDesktop(){
		Log.v("dictation","started connecting");
		if(TeclaApp.desktop ==null)
		TeclaApp.desktop=new TeclaDesktopClient(TeclaApp.getInstance());
		
		
		if(!TeclaApp.desktop.isConnected()&&TeclaApp.connect_to_desktop)
		new Thread(desktopsearcher).start();
	}
	Runnable desktopsearcher=new Runnable(){

		public void run() {
			// TODO Auto-generated method stub
			
			Log.v("dictation","attempting connection");
			
			if(TeclaApp.desktop== null)
				TeclaApp.desktop=new TeclaDesktopClient(TeclaApp.getInstance());
			TeclaApp.desktop.connect();
			
			if(TeclaApp.desktop.isConnected()){
				Log.v("dictation","connected to Desktop");
				new Thread(wifipinger).start();
				new Thread(wifireceiver).start();
			}
		}
		
	};
	private Runnable wifipinger=new Runnable(){

		public void run() {
			// TODO Auto-generated method stub
			while(TeclaApp.desktop.isConnected()){
			TeclaApp.desktop.send("ping");
			try {
				Thread.sleep(1000*2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			wifi_ping_count++;
			
			Log.v("pinger",""+wifi_ping_count);
				if(wifi_ping_count>5){
					TeclaApp.desktop.disconnect();
				}
			}
			
		}
	
	};
	private Runnable wifireceiver=new Runnable(){
		
		public void run() {
			// TODO Auto-generated method stub
			Log.v("connection","starting receiver");
			while(TeclaApp.desktop.isConnected())
			{
				
				String rec=TeclaApp.desktop.receive();
				if(rec!=null&&rec.equals("ping")){
					wifi_ping_count=0;
				}else if(rec !=null && rec.equals("dictation")){
					onKey(TeclaKeyboardView.KEYCODE_DICTATION,null);
				}
				
			}
		}
		
	};
	
	private Runnable wificonnector=new Runnable(){

		public void run() {
			// TODO Auto-generated method stub
			Log.v("dictation","Started WiFiConnector");
			if(TeclaApp.desktop!= null)
				TeclaApp.desktop=new TeclaDesktopClient(TeclaApp.getInstance());
			while( TeclaApp.mSendToPC){
				if(TeclaApp.mSendToPC && TeclaApp.connect_to_desktop && !TeclaApp.desktop.isConnected())
				{
				Log.v("dictation","attempting connection");
				TeclaApp.desktop.connect();
				
				if(TeclaApp.desktop.isConnected()){
					Log.v("dictation","connected to Desktop");
					new Thread(wifipinger).start();
					new Thread(wifireceiver).start();
					// TODO :onConnect change the send to pc button to connect state
				}
				}
				else{
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			Log.v("dictation",""+wifisearcherthread.isAlive());
		}
	};
*/	
	
}

