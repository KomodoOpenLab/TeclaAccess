package ca.idi.tekla.ime;

import java.util.Hashtable;
import java.util.List;

import ca.idi.tekla.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

public class MorseIME extends InputMethodService implements
KeyboardView.OnKeyboardActionListener, OnSharedPreferenceChangeListener {
	private String TAG = "MorseIME: ";
	private MorseKeyboardView inputView;
	public MorseKeyboard mMorseKeyboard;
	public Keyboard utilityKeyboard;
	private Keyboard.Key spaceKey;
	int spaceKeyIndex;
	private Keyboard.Key capsLockKey;
	int capsLockKeyIndex;
	private Hashtable<String, String> morseMap;
	private StringBuilder charInProgress;
	
	// Preferences
	private String AUTOCAP = "autocap";
	private String ENABLEUTILKBD = "enableutilkbd";

	private static final int CAPS_LOCK_OFF = 0;
	private static final int CAPS_LOCK_NEXT = 1;
	private static final int CAPS_LOCK_ALL = 2;
	private Integer capsLockState = CAPS_LOCK_OFF;

	private static final int AUTO_CAP_MIDSENTENCE = 0;
	private static final int AUTO_CAP_SENTENCE_ENDED = 1;
	private Integer autoCapState = AUTO_CAP_MIDSENTENCE;

	// Keycodes used in the utility keyboard
	public static final int KEYCODE_UP = -10;
	public static final int KEYCODE_LEFT = -11;
	public static final int KEYCODE_RIGHT = -12;
	public static final int KEYCODE_DOWN = -13;
	public static final int KEYCODE_HOME = -20;
	public static final int KEYCODE_END = -21;
	public static final int KEYCODE_DEL = -30;

	private SharedPreferences prefs;
	public String[] newlineGroups;
	private int maxCodeLength;

	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onInitializeInterface() {
		// TODO Auto-generated method stub
		super.onInitializeInterface();
		mMorseKeyboard = new MorseKeyboard(this, R.layout.morse_kbd);
		spaceKey = mMorseKeyboard.getSpaceKey();
		capsLockKey = mMorseKeyboard.getCapsLockKey();
		List<Keyboard.Key> keys = mMorseKeyboard.getKeys();
		spaceKeyIndex = keys.indexOf(spaceKey);
		capsLockKeyIndex = keys.indexOf(capsLockKey);

		//mTeclaMorse = new TeclaMorse();
	}

	@Override
	public View onCreateInputView() {
		/*inputView = (MorseKeyboardView) getLayoutInflater().inflate(R.layout.morse_input, null);
		inputView.setOnKeyboardActionListener(this);
		inputView.setKeyboard(mMorseKeyboard);
		inputView.setService(this);
		return inputView;*/
		return null;
	}

	public void onKey(int primaryCode, int[] keyCodes) {
		onKeyMorse(primaryCode, keyCodes);
	}

	/**
	 * Handle key input on the Morse Code keyboard. It has 5 keys and each of
	 * them does something different.
	 * 
	 * @param primaryCode
	 * @param keyCodes
	 */
	public void onKeyMorse(int primaryCode, int[] keyCodes) {
		// Log.d(TAG, "primaryCode: " + Integer.toString(primaryCode));
		String curCharMatch = morseMap.get(charInProgress.toString());

		switch (primaryCode) {

		// 0 represents a dot, 1 represents a dash
		case 0:
		case 1:

			if (charInProgress.length() < maxCodeLength) {
				charInProgress.append(primaryCode == 1 ? "-" : ".");
			}

			break;

			// Space button ends the current dotdash sequence
			// Space twice in a row sends through a standard space character
		case KeyEvent.KEYCODE_SPACE:
			if (charInProgress.length() == 0) {
				getCurrentInputConnection().commitText(" ", 1);

				if (autoCapState == AUTO_CAP_SENTENCE_ENDED
						&& prefs.getBoolean(AUTOCAP, false)) {
					capsLockState = CAPS_LOCK_NEXT;
					updateCapsLockKey(true);
				}
			} else {
				// Log.d(TAG, "Pressed space, look for " +
				// charInProgress.toString());

				if (curCharMatch != null) {

					if (curCharMatch.contentEquals("\n")) {
						sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
					} else if (curCharMatch.contentEquals("END")) {
						requestHideSelf(0);
						inputView.closing();
					} else {

						boolean uppercase = false;
						if (capsLockState == CAPS_LOCK_NEXT) {
							uppercase = true;
							capsLockState = CAPS_LOCK_OFF;
							updateCapsLockKey(true);
						} else if (capsLockState == CAPS_LOCK_ALL) {
							uppercase = true;
						}
						if (uppercase) {
							curCharMatch = curCharMatch.toUpperCase();
						}

						// Log.d(TAG, "Char identified as " + curCharMatch);
						getCurrentInputConnection().commitText(curCharMatch,
								curCharMatch.length());
					}
				}
			}
			clearCharInProgress();
			break;

			// If there's a character in progress, clear it
			// otherwise, send through a backspace keypress
		case KeyEvent.KEYCODE_DEL:
			if (charInProgress.length() > 0) {
				clearCharInProgress();
			} else {
				sendDownUpKeyEvents(primaryCode);
				clearCharInProgress();
				updateSpaceKey(true);

				if (capsLockState == CAPS_LOCK_NEXT) {
					// If you've hit delete and you were in caps_next state,
					// then caps_off
					capsLockState = CAPS_LOCK_OFF;
					updateCapsLockKey(true);
				}
			}
			break;

		case KeyEvent.KEYCODE_SHIFT_LEFT:
			switch (capsLockState) {
			case CAPS_LOCK_OFF:
				capsLockState = CAPS_LOCK_NEXT;
				break;
			case CAPS_LOCK_NEXT:
				capsLockState = CAPS_LOCK_ALL;
				break;
			default:
				capsLockState = CAPS_LOCK_OFF;
			}
			updateCapsLockKey(true);
			break;
		}

		updateSpaceKey(true);
	}

	private void clearCharInProgress() {
		charInProgress.setLength(0);
	}

	public void onPress(int arg0) {
		// TODO Auto-generated method stub

	}

	public void onRelease(int arg0) {
		// TODO Auto-generated method stub

	}

	public void onText(CharSequence arg0) {
		// TODO Auto-generated method stub

	}

	public void swipeDown() {
		// TODO Auto-generated method stub

	}

	public void swipeLeft() {
		// TODO Auto-generated method stub

	}

	public void swipeRight() {
		// TODO Auto-generated method stub

	}

	public void swipeUp() {
		// TODO Auto-generated method stub

	}

	public void clearEverything() {
		clearCharInProgress();
		capsLockState = CAPS_LOCK_OFF;
		updateCapsLockKey(false);
		updateSpaceKey(false);
	}

	public void updateCapsLockKey(boolean refreshScreen) {

		Context context = this.getApplicationContext();
		switch (capsLockState) {
		case CAPS_LOCK_OFF:
			capsLockKey.on = false;
			capsLockKey.label = context.getText(R.string.caps_lock_off);
			break;
		case CAPS_LOCK_NEXT:
			capsLockKey.on = false;
			capsLockKey.label = context.getText(R.string.caps_lock_next);
			break;
		case CAPS_LOCK_ALL:
			capsLockKey.on = true;
			capsLockKey.label = context.getText(R.string.caps_lock_all);
			break;
		}

		if (refreshScreen) {
			inputView.invalidateKey(capsLockKeyIndex);
		}
	}

	public void updateSpaceKey(boolean refreshScreen) {
		if (!spaceKey.label.toString().equals(charInProgress.toString())) {
			// Log.d(TAG, "!spaceKey.label.equals(charInProgress)");
			spaceKey.label = charInProgress.toString();
			if (refreshScreen) {
				inputView.invalidateKey(spaceKeyIndex);
			}
		}
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
	}

	public void onStartInputView(android.view.inputmethod.EditorInfo info,
			boolean restarting) {
		super.onStartInputView(info, restarting);
		inputView.invalidateKey(spaceKeyIndex);
		updateAutoCap();
		updateCapsLockKey(true);
	};

	@Override
	public void onFinishInputView(boolean finishingInput) {
		super.onFinishInputView(finishingInput);
		clearEverything();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
	}



	/**
	 * The cursor position (selection position) has changed
	 */
	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd, int candidatesStart,
			int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
				candidatesStart, candidatesEnd);
		updateAutoCap();
	}

	/**
	 * Update the shift state if autocap is turned on, based on current cursor
	 * position (using InputConnection.getCursorCapsMode())
	 */
	public void updateAutoCap() {

		// Autocap has no effect if Caps Lock is on
		if (capsLockState == CAPS_LOCK_ALL) {
			return;
		}

		// Don't bother with any of this is autocap is turned off
		if (!prefs.getBoolean(AUTOCAP, false)) {
			return;
		}

		int origCapsLockState = capsLockState;
		int newCapsLockState = CAPS_LOCK_OFF;

		EditorInfo ei = getCurrentInputEditorInfo();
		if (ei != null
				&& ei.inputType != EditorInfo.TYPE_NULL
				&& getCurrentInputConnection().getCursorCapsMode(ei.inputType) > 0) {
			newCapsLockState = CAPS_LOCK_NEXT;
		}
		capsLockState = newCapsLockState;
		if (capsLockState != origCapsLockState) {
			updateCapsLockKey(true);
		}
	}
}