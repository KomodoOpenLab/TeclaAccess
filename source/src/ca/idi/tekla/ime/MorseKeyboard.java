package ca.idi.tekla.ime;

import java.util.Iterator;
import java.util.List;

import ca.idi.tekla.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;

public class MorseKeyboard extends Keyboard {

        public MorseKeyboard(Context context, int xmlLayoutResId) {
                super(context, xmlLayoutResId);
        }
        
        public MorseKeyboard(Context context, int layoutTemplateResId,
                        CharSequence characters, int columns, int horizontalPadding) {
                super(context, layoutTemplateResId, characters, columns,
                                horizontalPadding);
        }
        
        public void enableShiftLock() {
        	
        }

        private Keyboard.Key spaceKey;
        private Keyboard.Key capsLockKey;

        @Override
        protected Key createKeyFromXml(Resources res, Row parent, int x, int y,
                        XmlResourceParser parser) {
                // TODO Auto-generated method stub
                Key k = super.createKeyFromXml(res, parent, x, y, parser);
                if (k.codes[0] == 62) {
                        spaceKey = k;
                } else if (k.codes[0] == 59) {
                        capsLockKey = k;
                }
                return k;
        }

        public Key getSpaceKey() {
                return this.spaceKey;
        }

        public Key getCapsLockKey() {
                return this.capsLockKey;
        }

		public void setImeOptions(Resources resources, int mMode, int mImeOptions) {
			// TODO Auto-generated method stub
			
		}

		public boolean isShiftLocked() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean setShiftLocked(boolean shiftLocked) {
			// TODO Auto-generated method stub
			return false;
		}

		public void updateVariantsState() {
			// TODO Auto-generated method stub
			
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
		public Key getKeyFromCode(int keycode) {
			int index = getKeyIndexFromKeyCode(keycode);
			if (index > -1) {
				return getKeys().get(index);
			}
			return null;
		}

		public Key getVariantsKey() {
			// TODO Auto-generated method stub
			return null;
		}	public Integer getRowCount() {
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
}