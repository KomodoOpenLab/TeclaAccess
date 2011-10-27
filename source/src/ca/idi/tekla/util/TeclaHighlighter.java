/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla.util;

import java.util.Iterator;
import java.util.List;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.os.Handler;
import android.os.Message;
import ca.idi.tekla.ime.TeclaKeyboardView;

public class TeclaHighlighter {

	//Scanning constants and variables
	public static final int REDRAW_KEYBOARD = 99999; //this is a true arbitrary number.
	public static final int HIGHLIGHT_NEXT = 0x55;
	public static final int HIGHLIGHT_PREV = 0xAA;
	public static final int DEPTH_ROW = 0x55;
	public static final int DEPTH_KEY = 0xAA;
	private int mScanDepth = TeclaHighlighter.DEPTH_ROW;
	private int mScanKeyCounter = 0;
	private int mScanRowCounter = 0;

	private static TeclaHighlighter tHighlighter = null;
	private TeclaKeyboardView mImeView;

	/**
	 * Create a new instance of this class or return one if it already exists
	 * @return TeklaScanner
	 */
	public static TeclaHighlighter getInstance() {
		if(tHighlighter == null) {
			return new TeclaHighlighter();
		}
		return tHighlighter;
	}

	public void initHighlighting(TeclaKeyboardView imeView) {
		if (getRowCount(imeView.getKeyboard()) != 1) {
			mScanDepth = DEPTH_ROW;
			mScanKeyCounter = 0;
			mScanRowCounter = 0;
		}
		resetFocus(imeView);
	}

	public void clearHighlight(TeclaKeyboardView imeView) {
		highlightKeys(imeView, -1, -1);
	}

	public void initRowHighlighting(TeclaKeyboardView imeView, int scanRowCount) {
		Keyboard keyboard = imeView.getKeyboard();
		mScanDepth = DEPTH_KEY;
		mScanRowCounter = scanRowCount;
		mScanKeyCounter = getRowStart(keyboard, mScanRowCounter);
		resetFocus(imeView);
	}

	public void moveHighlight(TeclaKeyboardView imeView, int direction) {
		Keyboard keyboard = imeView.getKeyboard();
		int rowCount = getRowCount(keyboard);

		if (rowCount == 1) {
			mScanDepth = DEPTH_KEY;
			mScanRowCounter = 0;
		}
		if (mScanDepth == TeclaHighlighter.DEPTH_ROW) {
			if (direction == TeclaHighlighter.HIGHLIGHT_NEXT) mScanRowCounter++;
			if (direction == TeclaHighlighter.HIGHLIGHT_PREV) mScanRowCounter--;
			mScanRowCounter = wrapCounter(mScanRowCounter, 0, rowCount - 1);
		}
		int fromIndex = getRowStart(keyboard, mScanRowCounter);
		int toIndex = getRowEnd(keyboard, mScanRowCounter);
		if (mScanDepth == TeclaHighlighter.DEPTH_KEY) {
			if (direction == TeclaHighlighter.HIGHLIGHT_NEXT) mScanKeyCounter++;
			if (direction == TeclaHighlighter.HIGHLIGHT_PREV) mScanKeyCounter--;
			mScanKeyCounter = wrapCounter(mScanKeyCounter, fromIndex, toIndex);
			highlightKeys(imeView,mScanKeyCounter,mScanKeyCounter);
		} else 
			highlightKeys(imeView,fromIndex,toIndex);
	}

	public void stepOut(TeclaKeyboardView imeView) {
		Keyboard keyboard = imeView.getKeyboard();
		if (getRowCount(keyboard) != 1) {
			mScanDepth = TeclaHighlighter.DEPTH_ROW;		
			highlightKeys(imeView,
					getRowStart(keyboard, mScanRowCounter),
					getRowEnd(keyboard, mScanRowCounter));
		}
	}

	public int getScanDepth() {
		return mScanDepth;
	}

	public int getCurrentKey() {
		return mScanKeyCounter;
	}

	public int getCurrentRow() {
		return mScanRowCounter;
	}

	private Integer getRowCount(Keyboard keyboard) {
		List<Key> keyList = keyboard.getKeys();
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

	private void highlightKeys(TeclaKeyboardView imeView, int fromIndex, int toIndex) {
		Keyboard keyboard = imeView.getKeyboard();
		List<Key> keyList = keyboard.getKeys();
		int totalKeys = keyList.size();
		Key key;
		for (int i=0;i < totalKeys;i++) {
			key = keyList.get(i);
			if ((i >= fromIndex) && (i <= toIndex)) {
				key.pressed = true;
			} else {
				key.pressed = false;
			}
		}
		redrawInputView(imeView);
	}

	private int wrapCounter(int counter, int min, int max) {
		if (counter > max) counter = min;
		if (counter < min) counter = max;
		return counter;
	}

	private void resetFocus(TeclaKeyboardView imeView) {
		moveHighlight(imeView, HIGHLIGHT_NEXT);
		moveHighlight(imeView, HIGHLIGHT_PREV);
	}

	private void redrawInputView (TeclaKeyboardView imeView) {
		// Should't mess with GUI from within a thread,
		// and threads call this method, so we'll use a
		// handler to take care of it.
		mImeView = imeView;
		Message msg = Message.obtain();
		msg.what = TeclaHighlighter.REDRAW_KEYBOARD;
		redrawHandler.sendMessage(msg);  
	}

	private Handler redrawHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TeclaHighlighter.REDRAW_KEYBOARD:
				mImeView.invalidateAllKeys(); // Redraw keyboard
				break;          
			default:
				super.handleMessage(msg);
				break;          
			}
		}
	};

	private Integer getRowStart(Keyboard keyboard, int rowNumber) {
		int keyCounter = 0;
		if (rowNumber != 0) {
			List<Key> keyList = keyboard.getKeys();
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

	private Integer getRowEnd(Keyboard keyboard, int rowNumber) {
		List<Key> keyList = keyboard.getKeys();
		int totalKeys = keyList.size();
		int keyCounter = 0;
		if (rowNumber == (getRowCount(keyboard) - 1)) {
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
