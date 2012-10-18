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

import java.util.Iterator;
import java.util.List;

import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.view.inputmethod.EditorInfo;

public class TeclaKeyboard extends Keyboard {

    static final int KEYCODE_MORSE_DIT = 500;
    static final int KEYCODE_MORSE_DAH = 501;
    static final int KEYCODE_MORSE_SPACEKEY = 562;
    static final int KEYCODE_MORSE_DELKEY = 67;
    static final int KEYCODE_SEND_TO_PC = -11;
    
	public static int KEYCODE_ALTNAV =
			TeclaApp.getInstance().getResources().getInteger(R.integer.key_altnav);
	public static int KEYCODE_VOICE =
			TeclaApp.getInstance().getResources().getInteger(R.integer.key_voice);
	public static int KEYCODE_VARIANTS =
			TeclaApp.getInstance().getResources().getInteger(R.integer.key_variants);
    public static final int KEYCODE_REPEAT_LOCK =
			TeclaApp.getInstance().getResources().getInteger(R.integer.key_repeat_lock);
	
    private Drawable mShiftLockIcon;
    private Drawable mShiftLockPreviewIcon;
    private Drawable mOldShiftIcon;
    private Drawable mOldShiftPreviewIcon;
    private Drawable mOldRepeatIcon;
    private Key mShiftKey;
    private Key mEnterKey;
    private Key mSpaceKey;
    private Key mCapsLockKey;
    private Key mSendtoPCKey;
    private static final int SHIFT_OFF = 0;
    private static final int SHIFT_ON = 1;
    private static final int SHIFT_LOCKED = 2;
    
    private int mShiftState = SHIFT_OFF;

    static int sSpacebarVerticalCorrection;

    public TeclaKeyboard(Context context, int xmlLayoutResId) {
        this(context, xmlLayoutResId, 0);
        customInit();
    }
    public Key getSendToPCKey(){
    	return mSendtoPCKey;
    }
    public TeclaKeyboard(Context context, int xmlLayoutResId, int mode) {
        super(context, xmlLayoutResId, mode);
        Resources res = context.getResources();
        mShiftLockIcon = res.getDrawable(R.drawable.sym_keyboard_shift_locked);
        mShiftLockPreviewIcon = res.getDrawable(R.drawable.sym_keyboard_feedback_shift_locked);
        mShiftLockPreviewIcon.setBounds(0, 0, 
                mShiftLockPreviewIcon.getIntrinsicWidth(),
                mShiftLockPreviewIcon.getIntrinsicHeight());
        sSpacebarVerticalCorrection = res.getDimensionPixelOffset(
                R.dimen.spacebar_vertical_correction); 
        customInit();
    }

    public TeclaKeyboard(Context context, int layoutTemplateResId, 
            CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
        customInit();
    }

    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, 
            XmlResourceParser parser) {
        Key key = new TeclaKey(res, parent, x, y, parser);
        int code = key.codes[0];
        if (code == 10) {
            mEnterKey = key;
        } else if (code == KEYCODE_SEND_TO_PC) {
        	mSendtoPCKey = key;
        }
        return key;
    }
    
	public Key getSendtoPCKey() {
		return this.mSendtoPCKey;
	}
   
    void setImeOptions(Resources res, int mode, int options) {
        if (mEnterKey != null) {
            // Reset some of the rarely used attributes.
            mEnterKey.popupCharacters = null;
            mEnterKey.popupResId = 0;
            mEnterKey.text = null;
            switch (options&(EditorInfo.IME_MASK_ACTION|EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
                case EditorInfo.IME_ACTION_GO:
                    mEnterKey.iconPreview = null;
                    mEnterKey.icon = null;
                    mEnterKey.label = res.getText(R.string.label_go_key);
                    break;
                case EditorInfo.IME_ACTION_NEXT:
                    mEnterKey.iconPreview = null;
                    mEnterKey.icon = null;
                    mEnterKey.label = res.getText(R.string.label_next_key);
                    break;
                case EditorInfo.IME_ACTION_DONE:
                    mEnterKey.iconPreview = null;
                    mEnterKey.icon = null;
                    mEnterKey.label = res.getText(R.string.label_done_key);
                    break;
                case EditorInfo.IME_ACTION_SEARCH:
                    mEnterKey.iconPreview = res.getDrawable(
                            R.drawable.sym_keyboard_feedback_search);
                    mEnterKey.icon = res.getDrawable(
                            R.drawable.sym_keyboard_search);
                    mEnterKey.label = null;
                    break;
                case EditorInfo.IME_ACTION_SEND:
                    mEnterKey.iconPreview = null;
                    mEnterKey.icon = null;
                    mEnterKey.label = res.getText(R.string.label_send_key);
                    break;
                default:
                    if (mode == KeyboardSwitcher.MODE_IM) {
                        mEnterKey.icon = null;
                        mEnterKey.iconPreview = null;
                        mEnterKey.label = ":-)";
                        mEnterKey.text = ":-) ";
                        mEnterKey.popupResId = R.xml.popup_smileys;
                    } else {
                        mEnterKey.iconPreview = res.getDrawable(
                                R.drawable.sym_keyboard_feedback_return);
                        mEnterKey.icon = res.getDrawable(
                                R.drawable.sym_keyboard_return);
                        mEnterKey.label = null;
                    }
                    break;
            }
            // Set the initial size of the preview icon
            if (mEnterKey.iconPreview != null) {
                mEnterKey.iconPreview.setBounds(0, 0, 
                        mEnterKey.iconPreview.getIntrinsicWidth(),
                        mEnterKey.iconPreview.getIntrinsicHeight());
            }
        }
    }
    
    void enableShiftLock() {
        int index = getShiftKeyIndex();
        if (index >= 0) {
            mShiftKey = getKeys().get(index);
            if (mShiftKey instanceof TeclaKey) {
                ((TeclaKey)mShiftKey).enableShiftLock();
            }
            mOldShiftIcon = mShiftKey.icon;
            mOldShiftPreviewIcon = mShiftKey.iconPreview;
        }
    }

    void setShiftLocked(boolean shiftLocked) {
        if (mShiftKey != null) {
            if (shiftLocked) {
                mShiftKey.on = true;
                mShiftKey.icon = mShiftLockIcon;
                mShiftState = SHIFT_LOCKED;
            } else {
                mShiftKey.on = false;
                mShiftKey.icon = mShiftLockIcon;
                mShiftState = SHIFT_ON;
            }
        }
    }

    boolean isShiftLocked() {
        return mShiftState == SHIFT_LOCKED;
    }
    
    @Override
    public boolean setShifted(boolean shiftState) {
        boolean shiftChanged = false;
        if (mShiftKey != null) {
            if (shiftState == false) {
                shiftChanged = mShiftState != SHIFT_OFF;
                mShiftState = SHIFT_OFF;
                mShiftKey.on = false;
                mShiftKey.icon = mOldShiftIcon;
            } else {
                if (mShiftState == SHIFT_OFF) {
                    shiftChanged = mShiftState == SHIFT_OFF;
                    mShiftState = SHIFT_ON;
                    mShiftKey.icon = mShiftLockIcon;
                }
            }
        } else {
            return super.setShifted(shiftState);
        }
        return shiftChanged;
    }
        
    @Override
    public boolean isShifted() {
        if (mShiftKey != null) {
            return mShiftState != SHIFT_OFF;
        } else {
            return super.isShifted();
        }
    }

    static class TeclaKey extends Keyboard.Key {
        
        private boolean mShiftLockEnabled;
        
        public TeclaKey(Resources res, Keyboard.Row parent, int x, int y, 
                XmlResourceParser parser) {
            super(res, parent, x, y, parser);
            if (popupCharacters != null && popupCharacters.length() == 0) {
                // If there is a keyboard with no keys specified in popupCharacters
                popupResId = 0;
            }
        }
        
        void enableShiftLock() {
            mShiftLockEnabled = true;
        }

        @Override
        public void onReleased(boolean inside) {
            if (!mShiftLockEnabled) {
                super.onReleased(inside);
            } else {
                pressed = !pressed;
            }
        }

        /**
         * Overriding this method so that we can reduce the target area for certain keys.
         */
        @Override
        public boolean isInside(int x, int y) {
            final int code = codes[0];
            if (code == KEYCODE_SHIFT ||
                    code == KEYCODE_DELETE) {
                y -= height / 10;
                if (code == KEYCODE_SHIFT) x += width / 6;
                if (code == KEYCODE_DELETE) x -= width / 6;
            } else if (code == TeclaIME.KEYCODE_SPACE) {
                y += TeclaKeyboard.sSpacebarVerticalCorrection;
            }
            return super.isInside(x, y);
        }
    }
    
	/**
	 * Tag used for logging in this class
	 */
	private static final String CLASS_TAG = "TeclaKeyboard: ";

	public Integer getRowCount() {
		List<Key> keyList = getKeys();
		Key key;
		int rowCounter = 0;
		int coord = 0;
		for (Iterator<Key> i = keyList.iterator(); i.hasNext();) {
			key = i.next();
			if (rowCounter == 0) {
				rowCounter++;
				coord = key.y;
			}
			if (coord != key.y) {
				rowCounter++;
				coord = key.y;
			}
		}
		return rowCounter;
	}

	public Integer getRowStart(int rowNumber) {
		int keyCounter = 0;
		if (rowNumber != 0) {
			List<Key> keyList = getKeys();
			Key key;
			int rowCounter = 0;
			int prevCoord = keyList.get(0).y;
			int thisCoord;
			while (rowCounter != rowNumber) {
				keyCounter++;
				key = keyList.get(keyCounter);
				thisCoord = key.y;
				if (thisCoord != prevCoord) {
					// Changed rows
					rowCounter++;
					prevCoord = thisCoord;
				}
			}
		}
		return keyCounter;
	}

	public Integer getRowEnd(int rowNumber) {
		List<Key> keyList = getKeys();
		int totalKeys = keyList.size();
		int keyCounter = 0;
		if (rowNumber == (getRowCount() - 1)) {
			keyCounter = totalKeys - 1;
		} else {
			Key key;
			int rowCounter = 0;
			int prevCoord = keyList.get(0).y;
			int thisCoord;
			while (rowCounter <= rowNumber) {
				keyCounter++;
				key = keyList.get(keyCounter);
				thisCoord = key.y;
				if (thisCoord != prevCoord) {
					// Changed rows
					rowCounter++;
					prevCoord = thisCoord;
				}
			}
			keyCounter--;
		}
		return keyCounter;
	}

	private int getKeyIndexFromKeyCode(int keycode) {
		List<Key> keys = getKeys();
		int i = 0;
		Key key = keys.get(i);
		while (((i + 1) < keys.size()) && (key.codes[0] != keycode)) {
			i++;
			key = keys.get(i);
		}
		return key.codes[0] == keycode? i : -1;
	}
	
	/**
	 * Return the key with the specified keycode
	 * @return the key or null if the keyboard doesn't have a key with the keycode provided
	 */
	public Key getKeyFromKeyCode(int keycode) {
		int index = getKeyIndexFromKeyCode(keycode);
		if (index > -1) {
			return getKeys().get(index);
		}
		return null;
	}
	
	public Key getAltNavKey() {
		return getKeyFromKeyCode(KEYCODE_ALTNAV);
	}
	
	public Key getVariantsKey() {
		return getKeyFromKeyCode(KEYCODE_VARIANTS);
	}
	
	public Key getRepeatLockKey() {
		return getKeyFromKeyCode(KEYCODE_REPEAT_LOCK);
	}
	
	public Key getMorseSpaceKey() {
		return getKeyFromKeyCode(KEYCODE_MORSE_SPACEKEY);
	}
	
	public int getMorseSpaceKeyIndex() {
		return getKeyIndexFromKeyCode(KEYCODE_MORSE_SPACEKEY);
	}
	
	public int getShiftKeyIndex() {
		return getKeyIndexFromKeyCode(Keyboard.KEYCODE_SHIFT);
	}

	private void customInit() {
		Key key = getVariantsKey();
		if (key != null) {
			key.on = TeclaApp.persistence.isVariantsKeyOn();
		}
		key = getRepeatLockKey();
		if (key != null) {
			key.on = TeclaApp.persistence.isRepeatLockOn();
		}
	}

	public void updateRepeatLockState() {
		Key key = getRepeatLockKey();
		if (key != null) {
			key.on = TeclaApp.persistence.isRepeatLockOn();
		}
	}
	
	public void updateVariantsState() {
		Key key = getVariantsKey();
		if (key != null) {
			key.on = TeclaApp.persistence.isVariantsKeyOn();
		}
	}
	
}
