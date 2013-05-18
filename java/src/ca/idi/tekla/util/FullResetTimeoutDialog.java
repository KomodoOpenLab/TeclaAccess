/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla.util;

import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class FullResetTimeoutDialog extends Dialog
		implements DialogInterface.OnKeyListener,
		SeekBar.OnSeekBarChangeListener,
		Button.OnClickListener {

	private SeekBar mSeekBar;
	private Button mButton;
	private TextView mMinLabel, mMaxLabel, mTimeoutLabel;
	private String[] mTimeoutStrings;
	private int[] mTimeoutValues;
	private int mSeekBarPos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	protected void onStart() {

		int timeout = TeclaApp.persistence.getFullResetTimeout();
		mTimeoutStrings = TeclaApp.getInstance().getResources().getStringArray(R.array.fullreset_strings);
		mTimeoutValues = TeclaApp.getInstance().getResources().getIntArray(R.array.full_reset_values);

		mSeekBarPos = -1;
		int value = Persistence.FULLRESET_NULL;
		while (value != timeout  && mSeekBarPos < (mTimeoutValues.length - 1)) {
			mSeekBarPos++;
			value = mTimeoutValues[mSeekBarPos];
		}
		
		mMinLabel = (TextView) findViewById(R.id.dlg_min_label);
		mMaxLabel = (TextView) findViewById(R.id.dlg_max_label);
		mTimeoutLabel = (TextView) findViewById(R.id.dlg_timeout_label);
		mMinLabel.setText(mTimeoutStrings[0]);
		mMaxLabel.setText(mTimeoutStrings[mTimeoutStrings.length - 1]);
		mTimeoutLabel.setText(mTimeoutStrings[mSeekBarPos]);

		mButton = (Button) findViewById(R.id.dlg_btn_done);
		mButton.setOnClickListener(this);

		mSeekBar = (SeekBar) findViewById(R.id.dlg_seekbar);
		mSeekBar.setMax(mTimeoutValues.length - 1);
		mSeekBar.setProgress(mSeekBarPos);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.requestFocus();
		super.onStart();
	}

	public FullResetTimeoutDialog(Context context) {
		super(context);
		setOnKeyListener(this);
		setTitle(R.string.fullreset_timeout);
	}

	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK ||
				keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
				keyCode == KeyEvent.KEYCODE_ENTER) {
			dismiss();
			return true;
		}
		return false;
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		mTimeoutLabel.setText(mTimeoutStrings[progress]);
		TeclaApp.persistence.setFullResetTimeout(mTimeoutValues[progress]);
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	public void onClick(View arg0) {
		dismiss();
	}
	
}
