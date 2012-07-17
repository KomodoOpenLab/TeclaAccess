/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla.util;

import java.util.List;

import android.content.Context;
import android.inputmethodservice.Keyboard.Key;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import ca.idi.tekla.TeclaApp;
import ca.idi.tekla.ime.TeclaKeyboard;
import ca.idi.tekla.ime.TeclaKeyboardView;

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

	
	private long mScanDelay = Math.round(1.5 * TeclaApp.persistence.getScanDelay());
	private int mScanDepth;
	private int mScanKeyCounter, mScanRowCounter;
	private int mLastKeyCounter, mLastRowCounter;
	private int mInactiveScans,mStartScanKeyCounter;
	private boolean mWasShowingVariants;
	private TeclaKeyboardView mIMEView;
	private Handler mHandler;

	public Highlighter(Context context) {

		mScanDepth = Highlighter.DEPTH_ROW;
		mScanKeyCounter = 0;
		mScanRowCounter = 0;
		mWasShowingVariants = false;
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
	public void setIMEView(TeclaKeyboardView imeView) {
		TeclaApp.getInstance().broadcastInputViewCreated();
		mIMEView = imeView;
		if (mIMEView.getKeyboard().getRowCount() == 1) {
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
		if (mWasShowingVariants && !TeclaApp.persistence.isVariantsShowing()) {
			mScanKeyCounter = mLastKeyCounter;
			mScanRowCounter = mLastRowCounter;
			mWasShowingVariants = false;
		}
		if (shouldDelayKey(keyCode)) {
			startSelfScanning(mScanDelay);
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
			// Extends dwell time for first key after a row-key transition.
			pauseSelfScanning();
			mHandler.postDelayed(mScanRunnable, mScanDelay);
		}
	}
	
	public void move(int direction) {
		TeclaKeyboard keyboard = mIMEView.getKeyboard();
		int rowCount = keyboard.getRowCount();

		if (rowCount == 1) {
			mScanDepth = DEPTH_KEY;
			mScanRowCounter = 0;
		}
		if (mScanDepth == Highlighter.DEPTH_ROW) {
			if (direction == Highlighter.HIGHLIGHT_NEXT) mScanRowCounter++;
			if (direction == Highlighter.HIGHLIGHT_PREV) mScanRowCounter--;
			mScanRowCounter = wrapCounter(mScanRowCounter, 0, rowCount - 1);
		}
		int fromIndex = keyboard.getRowStart(mScanRowCounter);
		int toIndex = keyboard.getRowEnd(mScanRowCounter);
		mStartScanKeyCounter = fromIndex;
		if (mScanDepth == Highlighter.DEPTH_KEY) {
			if (direction == Highlighter.HIGHLIGHT_NEXT) mScanKeyCounter++;
			if (direction == Highlighter.HIGHLIGHT_PREV) mScanKeyCounter--;
			mScanKeyCounter = wrapCounter(mScanKeyCounter, fromIndex, toIndex);
			highlightKeys(mScanKeyCounter,mScanKeyCounter);
		} else 
			highlightKeys(fromIndex,toIndex);
	}
	
	public void stepOut() {
		TeclaKeyboard keyboard = mIMEView.getKeyboard();
		if (keyboard.getRowCount() != 1) {
			mScanDepth = Highlighter.DEPTH_ROW;		
			highlightKeys(
					keyboard.getRowStart(mScanRowCounter),
					keyboard.getRowEnd(mScanRowCounter));
		}
	}

	public int getScanDepth() {
		return mScanDepth;
	}

	public TeclaKeyboard.Key getCurrentKey() {
		TeclaKeyboard keyboard = mIMEView.getKeyboard();
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
				if(mScanKeyCounter==mStartScanKeyCounter)  mInactiveScans++;
				if(mInactiveScans==2 && getScanDepth()==Highlighter.DEPTH_KEY)
				{
					mInactiveScans=0;
					stepOut();
			        if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "No activity.Stepping out...");
			    }
			mHandler.postAtTime(this, start + TeclaApp.persistence.getScanDelay());
		}
		
	};
	
	/*
	 * Runnable used to step out with hidden key
	 */
	public void externalstepOut()
	{
		Runnable mScanRunnable = new Runnable() {
			
			public void run() {
				mInactiveScans = 0;
				pauseSelfScanning();
				stepOut();
				resumeSelfScanning();
				Log.d(TeclaApp.TAG,CLASS_TAG + "Hidden Key pressed");	
			}	
		}; 
		mHandler.postDelayed(mScanRunnable,TeclaApp.persistence.getScanDelay());
	}
	
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
		if (mIMEView.getKeyboard().getRowCount() > 1) {
			//Keyboard has multiple rows
			mScanDepth = DEPTH_ROW;
			mScanKeyCounter = 0;
			mScanRowCounter = 0;
		} else {
			//Keyboard has only one row, don't reset highlighting unless it is a variants keyboard
			mScanDepth = DEPTH_KEY;
			if (TeclaApp.persistence.isVariantsShowing()) {
				mLastKeyCounter = mScanKeyCounter;
				mLastRowCounter = mScanRowCounter;
				mWasShowingVariants = true;
				mScanKeyCounter = 0;
				mScanRowCounter = 0;
			}
		}
		restoreHighlight();
		mStartScanKeyCounter=mScanKeyCounter;  // Counter to check which key
		mInactiveScans=0;  // Counter to check number of eventless self scan
	}
	
	private boolean shouldDelayKey(int keyCode){
		if (TeclaApp.persistence.isVariantsShowing()) return false;
		if (keyCode == TeclaKeyboard.KEYCODE_DONE ||
				keyCode == TeclaKeyboard.KEYCODE_MODE_CHANGE ||
				keyCode == TeclaKeyboard.KEYCODE_SHIFT ||
				keyCode == TeclaKeyboard.KEYCODE_VARIANTS) {
			return false;
		}
		return true;
	}
	
	private void initRowHighlighting() {
		mScanDepth = DEPTH_KEY;
		mScanKeyCounter = mIMEView.getKeyboard().getRowStart(mScanRowCounter);
		restoreHighlight();
	}

	private void highlightKeys(int fromIndex, int toIndex) {
		if (mIMEView != null) {
			TeclaKeyboard keyboard = mIMEView.getKeyboard();
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

}
