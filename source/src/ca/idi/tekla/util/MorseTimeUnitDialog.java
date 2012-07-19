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

public class MorseTimeUnitDialog extends Dialog
		implements DialogInterface.OnKeyListener,
		SeekBar.OnSeekBarChangeListener,
		Button.OnClickListener {
	
	private SeekBar mSeekBar;
	private Button mButton;
	private TextView mMinLabel, mMaxLabel, mTimeUnitLabel;
	private String[] mRepeatStrings;
	private int[] mTimeUnitValues;
	private int mSeekBarPos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.morse_time_unit);
		
		int timeUnit = TeclaApp.persistence.getMorseTimeUnit();
		mRepeatStrings = TeclaApp.getInstance().getResources().getStringArray(R.array.morse_time_unit_array);
		mTimeUnitValues = TeclaApp.getInstance().getResources().getIntArray(R.array.morse_time_unit_values);

		mSeekBarPos = -1;
		int value = Persistence.AUTOHIDE_NULL;
		while (value != timeUnit  && mSeekBarPos < (mTimeUnitValues.length - 1)) {
			mSeekBarPos++;
			value = mTimeUnitValues[mSeekBarPos];
		}
		
		mMinLabel = (TextView) findViewById(R.id.dlg_min_label);
		mMaxLabel = (TextView) findViewById(R.id.dlg_max_label);
		mTimeUnitLabel = (TextView) findViewById(R.id.dlg_timeout_label);
		mMinLabel.setText(mRepeatStrings[0]);
		mMaxLabel.setText(mRepeatStrings[mRepeatStrings.length - 1]);
		mTimeUnitLabel.setText(mRepeatStrings[mSeekBarPos]);

		mButton = (Button) findViewById(R.id.dlg_btn_done);
		mButton.setOnClickListener(this);

		mSeekBar = (SeekBar) findViewById(R.id.dlg_seekbar);
		mSeekBar.setMax(mTimeUnitValues.length - 1);
		mSeekBar.setProgress(mSeekBarPos);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.requestFocus();
	}

	public MorseTimeUnitDialog(Context context) {
		super(context);
		setOnKeyListener(this);
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
		mTimeUnitLabel.setText(mRepeatStrings[progress]);
		TeclaApp.persistence.setMorseTimeUnit(mTimeUnitValues[progress]);
	}
	
	public CharSequence getLabel() {
		return mTimeUnitLabel.getText();
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