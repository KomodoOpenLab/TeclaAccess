/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla.util;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import ca.idi.tekla.TeclaApp;
import ca.idi.tekla.ime.TeclaKeyboardView;

public class Highlighter {

	private static final String CLASS_TAG = "Highlighter: ";

	//Scanning constants and variables
	public static final int REDRAW_KEYBOARD = 99999; //this is a true arbitrary number.
	public static final int HIGHLIGHT_NEXT = 0x55;
	public static final int HIGHLIGHT_PREV = 0xAA;
	public static final int DEPTH_ROW = 0x55;
	public static final int DEPTH_KEY = 0xAA;
	
	private int mScanDepth;
	private int mScanKeyCounter, mScanRowCounter;
	private TeclaKeyboardView mIMEView;
	private Handler mHandler;

	public Highlighter(Context context) {

		mScanDepth = Highlighter.DEPTH_ROW;
		mScanKeyCounter = 0;
		mScanRowCounter = 0;
		mHandler = new Handler();

	}
	
	public void setIMEView(TeclaKeyboardView imeView) {
		TeclaApp.getInstance().broadcastInputViewCreated();
		mIMEView = imeView;
		if (getRowCount(mIMEView.getKeyboard()) == 1) {
			mScanDepth = Highlighter.DEPTH_KEY;
		}
	}
	
	public boolean isSoftIMEShowing() {
		if (mIMEView != null && mIMEView.isShown()) {
			return true;
		}
		return false;
	}

	public void initHighlight() {
		// Ignore keyboards with only one row (will always scan continuously)
		if (getRowCount(mIMEView.getKeyboard()) != 1) {
			mScanDepth = DEPTH_ROW;
			mScanKeyCounter = 0;
			mScanRowCounter = 0;
		}
		resetHighlight();
	}

	public void clearHighlight() {
		highlightKeys(-1, -1);
	}

	public void initRowHighlighting(int scanRowCount) {
		Keyboard keyboard = mIMEView.getKeyboard();
		mScanDepth = DEPTH_KEY;
		mScanRowCounter = scanRowCount;
		mScanKeyCounter = getRowStart(keyboard, mScanRowCounter);
		resetHighlight();
	}

	public void moveHighlight(int direction) {
		Keyboard keyboard = mIMEView.getKeyboard();
		int rowCount = getRowCount(keyboard);

		if (rowCount == 1) {
			mScanDepth = DEPTH_KEY;
			mScanRowCounter = 0;
		}
		if (mScanDepth == Highlighter.DEPTH_ROW) {
			if (direction == Highlighter.HIGHLIGHT_NEXT) mScanRowCounter++;
			if (direction == Highlighter.HIGHLIGHT_PREV) mScanRowCounter--;
			mScanRowCounter = wrapCounter(mScanRowCounter, 0, rowCount - 1);
		}
		int fromIndex = getRowStart(keyboard, mScanRowCounter);
		int toIndex = getRowEnd(keyboard, mScanRowCounter);
		if (mScanDepth == Highlighter.DEPTH_KEY) {
			if (direction == Highlighter.HIGHLIGHT_NEXT) mScanKeyCounter++;
			if (direction == Highlighter.HIGHLIGHT_PREV) mScanKeyCounter--;
			mScanKeyCounter = wrapCounter(mScanKeyCounter, fromIndex, toIndex);
			highlightKeys(mScanKeyCounter,mScanKeyCounter);
		} else 
			highlightKeys(fromIndex,toIndex);
	}
	
	public void stepOut() {
		Keyboard keyboard = mIMEView.getKeyboard();
		if (getRowCount(keyboard) != 1) {
			mScanDepth = Highlighter.DEPTH_ROW;		
			highlightKeys(
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
	
	public void startSelfScanning(long delay) {
		mHandler.postDelayed(mStartScanRunnable, delay);
	}
	
	public void pauseSelfScanning() {
		mHandler.removeCallbacks(mScanRunnable);
		mHandler.removeCallbacks(mStartScanRunnable);
	}
	
	public void resumeSelfScanning() {
		pauseSelfScanning();
		mHandler.postDelayed(mScanRunnable, TeclaApp.persistence.getScanDelay());
	}
	
	public void stopSelfScanning() {
		pauseSelfScanning();
		clearHighlight();
	}
	
	/**
	 * Runnable used to auto scan
	 */
	private Runnable mScanRunnable = new Runnable() {
		public void run() {
			final long start = SystemClock.uptimeMillis();
			if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Scanning to next item");
			moveHighlight(Highlighter.HIGHLIGHT_NEXT);
			mHandler.postAtTime(this, start + TeclaApp.persistence.getScanDelay());
		}
	};
	
	/**
	 * Runnable used to reset auto scan
	 */
	private Runnable mStartScanRunnable = new Runnable () {

		public void run() {
			TeclaApp.highlighter.initHighlight();
			if (TeclaApp.persistence.isSelfScanningEnabled())
				resumeSelfScanning();
		}
	};

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

	private void highlightKeys(int fromIndex, int toIndex) {
		Keyboard keyboard = mIMEView.getKeyboard();
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
		redrawInputView();
	}

	private int wrapCounter(int counter, int min, int max) {
		if (counter > max) counter = min;
		if (counter < min) counter = max;
		return counter;
	}

	private void resetHighlight() {
		moveHighlight(HIGHLIGHT_NEXT);
		moveHighlight(HIGHLIGHT_PREV);
	}

	private void redrawInputView () {
		// Should't mess with GUI from within a thread,
		// and threads call this method, so we'll use a
		// handler to take care of it.
		Message msg = Message.obtain();
		msg.what = Highlighter.REDRAW_KEYBOARD;
		redrawHandler.sendMessage(msg);  
	}

	private Handler redrawHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Highlighter.REDRAW_KEYBOARD:
				mIMEView.invalidateAllKeys(); // Redraw keyboard
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
