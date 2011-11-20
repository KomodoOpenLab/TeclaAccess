/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla.util;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard.Key;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import ca.idi.tekla.TeclaApp;

public class Highlighter {

	private static final String CLASS_TAG = "Highlighter: ";

	//Scanning constants and variables
	public static final String ACTION_START_SCANNING = "ca.idi.tekla.action.START_SCANNING";
	public static final String ACTION_STOP_SCANNING = "ca.idi.tekla.action.STOP_SCANNING";
	public static final int REDRAW_KEYBOARD = 0x22; // arbitrary number.
	public static final int HIGHLIGHT_NEXT = 0x55; // arbitrary number.
	public static final int HIGHLIGHT_PREV = 0xAA; // arbitrary number.
	public static final int DEPTH_ROW = 0x0F; // arbitrary number.
	public static final int DEPTH_KEY = 0xF0; // arbitrary number.
	
	private int mScanDepth;
	private int mScanKeyCounter, mScanRowCounter;
	private KeyboardView mIMEView;
	private Handler mHandler;

	public Highlighter(Context context) {

		mScanDepth = Highlighter.DEPTH_ROW;
		mScanKeyCounter = 0;
		mScanRowCounter = 0;
		mHandler = new Handler() {
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

	}
	
	/**
	 * Set the input method view that the highlighter will work on.
	 * @param imeView the input method view the highlighter class will work on.
	 */
	public void setView(KeyboardView imeView) {
		TeclaApp.getInstance().broadcastInputViewCreated();
		mIMEView = imeView;
		if (getRowCount(mIMEView.getKeyboard()) == 1) {
			mScanDepth = Highlighter.DEPTH_KEY;
		}
	}
	
	/**
	 * Determine if the input method view is currently showing
	 * @return true if the input method view is showing, false otherwise.
	 */
	public boolean isSoftIMEShowing() {
		if (mIMEView != null && mIMEView.isShown()) {
			return true;
		}
		return false;
	}

	/** 
	 * Remove highlights from all keys in the current keyboard
	 */
	public void clear() {
		highlightKeys(-1, -1);
	}
	
	/** 
	 * Update highlighting after a key selection. Key should be obtained with {@link #getCurrentKey()}
	 */
	public void doSelectKey (int keyCode) {
		if (shouldDelayKey(keyCode)) {
			startSelfScanning(Math.round(1.5 * TeclaApp.persistence.getScanDelay()));
		} else {
			startSelfScanning();
		}
	}

	/**
	 * Update highlighting after the selecting current row.
	 */
	public void doSelectRow() {
		initRowHighlighting();
		if (TeclaApp.persistence.isSelfScanningEnabled()) {
			resumeSelfScanning();
		}
	}
	
	public void move(int direction) {
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

	public Keyboard.Key getCurrentKey() {
		Keyboard keyboard = mIMEView.getKeyboard();
		List<Key> keyList = keyboard.getKeys();
		return keyList.get(mScanKeyCounter);
	}

	/**
	 * Start auto scan on the current keyboard. Resets scanning initiating it from the top-most depth.
	 * @param delay the number of milliseconds to wait before the method is executed.
	 * This allows for keys to be repeated before the highlighting is reset.
	 */
	public void startSelfScanning(long delay) {
		pauseSelfScanning();
		mHandler.postDelayed(mStartScanRunnable, delay);
	}
	
	public void startSelfScanning() {
		startSelfScanning(0);
	}

	/**
	 * Pause auto scanning. Resume it again from where it was paused with {@link #resumeSelfScanning()}.
	 */
	public void pauseSelfScanning() {
		mHandler.removeCallbacks(mScanRunnable);
		mHandler.removeCallbacks(mStartScanRunnable);
	}
	
	/**
	 * Resume auto scanning. Call after {@link #pauseSelfScanning()} to resume scanning from where it was last paused.
	 */
	public void resumeSelfScanning() {
		if (TeclaApp.persistence.isScanningEnabled()) {
			mHandler.postDelayed(mScanRunnable, TeclaApp.persistence.getScanDelay());
		}
	}
	
	/**
	 * Stop auto scanning on the current keyboard. Resets all scanning variables and clears all highlights.
	 */
	public void stopSelfScanning() {
		pauseSelfScanning();
		clear();
	}
	
	public void restoreHighlight() {
		move(HIGHLIGHT_NEXT);
		move(HIGHLIGHT_PREV);
	}

	/**
	 * Runnable used to auto scan
	 */
	private Runnable mScanRunnable = new Runnable() {
		public void run() {
			final long start = SystemClock.uptimeMillis();
			if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Scanning to next item");
			move(Highlighter.HIGHLIGHT_NEXT);
			mHandler.postAtTime(this, start + TeclaApp.persistence.getScanDelay());
		}
	};
	
	/**
	 * Runnable used only to start auto scan. It resets highlighting before calling the self scanning methods.
	 */
	private Runnable mStartScanRunnable = new Runnable () {

		public void run() {
			init();
			if (TeclaApp.persistence.isSelfScanningEnabled()) {
				resumeSelfScanning();
			}
		}
	};

	/** 
	 * Start highlighting the current keyboard. Automatically handles single row keyboards.
	 */
	private void init() {
		if (getRowCount(mIMEView.getKeyboard()) > 1) {
			//Keyboard has multiple rows
			mScanDepth = DEPTH_ROW;
			mScanKeyCounter = 0;
			mScanRowCounter = 0;
		} else {
			//Keyboard has only one row so don't reset highlighting
			mScanDepth = DEPTH_KEY;
		}
		restoreHighlight();
	}

	private boolean shouldDelayKey(int keyCode) {
		if (keyCode == Keyboard.KEYCODE_DONE ||
				keyCode == Keyboard.KEYCODE_MODE_CHANGE ||
				keyCode == Keyboard.KEYCODE_SHIFT) {
			return false;
		}
		return true;
	}
	
	private void initRowHighlighting() {
		mScanDepth = DEPTH_KEY;
		mScanKeyCounter = getRowStart(mIMEView.getKeyboard(), mScanRowCounter);
		restoreHighlight();
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

	private void redrawInputView () {
		// Should't mess with GUI from within a thread,
		// and threads call this method, so we'll use a
		// handler to take care of it.
		Message msg = Message.obtain();
		msg.what = Highlighter.REDRAW_KEYBOARD;
		mHandler.sendMessage(msg);  
	}

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
