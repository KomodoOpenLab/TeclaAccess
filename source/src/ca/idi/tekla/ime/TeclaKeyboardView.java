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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard.Key;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;

public class TeclaKeyboardView extends KeyboardView {

	private TeclaMorse mTeclaMorse;
	private TeclaIME mIME;
	
	private Dialog mHudDialog;

	private final int NOT_UPDATED = 0;
	private final int DIT_TABLE = 1;
	private final int DAH_TABLE = 2;
	private final int UTIL_TABLE = 3;
	private int mCurrentTable;
	

    static final int KEYCODE_OPTIONS = -100;
    static final int KEYCODE_SHIFT_LONGPRESS = -101;
    
    // Keycode for stepping out self scanning
    static final int KEYCODE_STEP_OUT = -7;

    private Keyboard mPhoneKeyboard;

    public TeclaKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TeclaKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPhoneKeyboard(Keyboard phoneKeyboard) {
        mPhoneKeyboard = phoneKeyboard;
    }
    
	public void setTeclaMorse(TeclaMorse tm) {
		mTeclaMorse = tm;
	}
	
	public void setService(TeclaIME service) {
		mIME = service;
	}

    @Override
    protected boolean onLongPress(Key key) {
        /*if (key.codes[0] == TeclaKeyboard.KEYCODE_MORSE_SPACEKEY && 
        		TeclaApp.persistence.getMorseKeyMode() != TeclaIME.SINGLE_KEY_MODE) {
        	if (cheatsheetDialog != null && cheatsheetDialog.isShowing())
        		cheatsheetDialog.dismiss();
        	else
        		showCheatSheet();
            return true;		
        } else*/ 
    	if (key.codes[0] == Keyboard.KEYCODE_MODE_CHANGE) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        } else if (key.codes[0] == Keyboard.KEYCODE_SHIFT) {
            getOnKeyboardActionListener().onKey(KEYCODE_SHIFT_LONGPRESS, null);
            invalidateAllKeys();
            return true;
        } else if (key.codes[0] == '0' && getKeyboard() == mPhoneKeyboard) {
            // Long pressing on 0 in phone number keypad gives you a '+'.
            getOnKeyboardActionListener().onKey('+', null);
            return true;
        } else {
            return super.onLongPress(key);
        }
    }
    
	@Override
	public TeclaKeyboard getKeyboard() {
		return (TeclaKeyboard) super.getKeyboard();
	}
	
	/**
	 * Creates a Morse cheat sheet (Morse mode only)
	 */
	public void createCheatSheet() {
		if (mHudDialog == null) {
			mHudDialog = new Dialog(mIME);
			mHudDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mHudDialog.setCancelable(false);
			mHudDialog.setCanceledOnTouchOutside(false);
			
			DisplayMetrics dm = getResources().getDisplayMetrics();
			Window window = mHudDialog.getWindow();
			window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.token = this.getWindowToken();
			lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
			lp.gravity = Gravity.BOTTOM;
			lp.y = (int) (Math.round(dm.heightPixels * 0.12f) / dm.density);
			window.setAttributes(lp);
			window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		}
	}

	public void updateHud() {
		if (mIME.mKeyboardSwitcher.isMorseMode() && TeclaApp.persistence.isMorseHudEnabled()) {
			createCheatSheet();

			if (TeclaApp.persistence.getMorseKeyMode() == TeclaIME.SINGLE_KEY_MODE) {
				mHudDialog.setContentView(R.layout.utility_table);
				mHudDialog.show();
			}
			else {
				String s = mTeclaMorse.getCurrentChar();
				if (s.equals("•")) {
					dismissHud();
					mCurrentTable = DIT_TABLE;
					mHudDialog.setContentView(R.layout.dit_table);
					mHudDialog.show();
				}
				else if (s.equals("-")) {
					dismissHud();
					mCurrentTable = DAH_TABLE;
					mHudDialog.setContentView(R.layout.dah_table);
					mHudDialog.show();
				}
				else if (mCurrentTable != UTIL_TABLE && s.equals("")) {
					dismissHud();
					mCurrentTable = UTIL_TABLE;
					mHudDialog.setContentView(R.layout.utility_table);
					mHudDialog.show();
				}
			}
		}
		else
			dismissHud();
	}
	
	public void updateHudTable() {
		mCurrentTable = NOT_UPDATED;
	}
	
	/**
	 * Dismisses the Morse cheat sheet (Morse mode only)
	 */
	public void dismissHud() {
        if (mHudDialog != null) {
                mHudDialog.dismiss();
        }
	}
	
	/****************************  INSTRUMENTATION  *******************************/

    static final boolean DEBUG_AUTO_PLAY = false;
    private static final int MSG_TOUCH_DOWN = 1;
    private static final int MSG_TOUCH_UP = 2;
    
    Handler mHandler2;
    
    private String mStringToPlay;
    private int mStringIndex;
    private boolean mDownDelivered;
    private Key[] mAsciiKeys = new Key[256];
    private boolean mPlaying;

    @Override
    public void setKeyboard(Keyboard k) {
        super.setKeyboard(k);
        if (DEBUG_AUTO_PLAY) {
            findKeys();
            if (mHandler2 == null) {
                mHandler2 = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        removeMessages(MSG_TOUCH_DOWN);
                        removeMessages(MSG_TOUCH_UP);
                        if (mPlaying == false) return;
                        
                        switch (msg.what) {
                            case MSG_TOUCH_DOWN:
                                if (mStringIndex >= mStringToPlay.length()) {
                                    mPlaying = false;
                                    return;
                                }
                                char c = mStringToPlay.charAt(mStringIndex);
                                while (c > 255 || mAsciiKeys[(int) c] == null) {
                                    mStringIndex++;
                                    if (mStringIndex >= mStringToPlay.length()) {
                                        mPlaying = false;
                                        return;
                                    }
                                    c = mStringToPlay.charAt(mStringIndex);
                                }
                                int x = mAsciiKeys[c].x + 10;
                                int y = mAsciiKeys[c].y + 26;
                                MotionEvent me = MotionEvent.obtain(SystemClock.uptimeMillis(), 
                                        SystemClock.uptimeMillis(), 
                                        MotionEvent.ACTION_DOWN, x, y, 0);
                                TeclaKeyboardView.this.dispatchTouchEvent(me);
                                me.recycle();
                                sendEmptyMessageDelayed(MSG_TOUCH_UP, 500); // Deliver up in 500ms if nothing else
                                // happens
                                mDownDelivered = true;
                                break;
                            case MSG_TOUCH_UP:
                                char cUp = mStringToPlay.charAt(mStringIndex);
                                int x2 = mAsciiKeys[cUp].x + 10;
                                int y2 = mAsciiKeys[cUp].y + 26;
                                mStringIndex++;
                                
                                MotionEvent me2 = MotionEvent.obtain(SystemClock.uptimeMillis(), 
                                        SystemClock.uptimeMillis(), 
                                        MotionEvent.ACTION_UP, x2, y2, 0);
                                TeclaKeyboardView.this.dispatchTouchEvent(me2);
                                me2.recycle();
                                sendEmptyMessageDelayed(MSG_TOUCH_DOWN, 500); // Deliver up in 500ms if nothing else
                                // happens
                                mDownDelivered = false;
                                break;
                        }
                    }
                };

            }
        }
    }

    private void findKeys() {
        List<Key> keys = getKeyboard().getKeys();
        // Get the keys on this keyboard
        for (int i = 0; i < keys.size(); i++) {
            int code = keys.get(i).codes[0];
            if (code >= 0 && code <= 255) { 
                mAsciiKeys[code] = keys.get(i);
            }
        }
    }
    
    void startPlaying(String s) {
        if (!DEBUG_AUTO_PLAY) return;
        if (s == null) return;
        mStringToPlay = s.toLowerCase();
        mPlaying = true;
        mDownDelivered = false;
        mStringIndex = 0;
        mHandler2.sendEmptyMessageDelayed(MSG_TOUCH_DOWN, 10);
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);
        if (DEBUG_AUTO_PLAY && mPlaying) {
            mHandler2.removeMessages(MSG_TOUCH_DOWN);
            mHandler2.removeMessages(MSG_TOUCH_UP);
            if (mDownDelivered) {
                mHandler2.sendEmptyMessageDelayed(MSG_TOUCH_UP, 20);
            } else {
                mHandler2.sendEmptyMessageDelayed(MSG_TOUCH_DOWN, 20);
            }
        }
    }
    
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
}
