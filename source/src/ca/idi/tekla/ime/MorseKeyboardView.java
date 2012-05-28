package ca.idi.tekla.ime;


import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard.Key;
import android.util.AttributeSet;


public class MorseKeyboardView extends KeyboardView {

	private MorseIME mIMEService;

	public static final int KBD_NONE = 0;
	public static final int KBD_DITDAH = 1;

	public void setService(MorseIME service) {
		mIMEService = service;
	}

	public MorseKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPreviewEnabled(false);
	}

	public MorseKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setPreviewEnabled(false);
	}
	
	@Override
	public MorseKeyboard getKeyboard() {
		return (MorseKeyboard) super.getKeyboard();
	}


	/**
	 * Updates the newline code printed in the cheat sheet, based on the user's
	 * current preference.
	 */
	 public void updateNewlineCode() {

	}
	 
	 /*@Override
	 protected boolean onLongPress(Key key) {
		 invalidateAllKeys();
		 return true;
	 }*/


	public void setPhoneKeyboard(MorseKeyboard keyboard) {
		// TODO Auto-generated method stub
		
	}

	public void startPlaying(String string) {
		// TODO Auto-generated method stub
	}
}