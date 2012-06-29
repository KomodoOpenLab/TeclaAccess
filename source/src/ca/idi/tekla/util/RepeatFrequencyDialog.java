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

public class RepeatFrequencyDialog extends Dialog
		implements DialogInterface.OnKeyListener,
		SeekBar.OnSeekBarChangeListener,
		Button.OnClickListener {

	private static final int SEEK_BAR_MAX = 11;
	private static final double SPEED_CONVERSION_FACTOR = 0.935f;
	private SeekBar mSeekBar;
	private Button mButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int frequency = TeclaApp.persistence.getRepeatFrequency();
		int progress = frequencyToProgress(frequency);

		mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
		mButton = (Button) findViewById(R.id.speed_dlg_btn_done);

		mButton.setOnClickListener(this);
		mSeekBar.setMax(SEEK_BAR_MAX);
		mSeekBar.setProgress(progress);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.requestFocus();
	}
	
	private int frequencyToProgress(int frequency) {
		double progress =
			Math.log(1.f * frequency / Persistence.MAX_REPEAT_FREQ) / Math.log(SPEED_CONVERSION_FACTOR);
		return (int) Math.round(progress);
	}

	private int progressToFrequency(int progress) {
		double delay = Persistence.MAX_REPEAT_FREQ * Math.pow(SPEED_CONVERSION_FACTOR, progress);
		return (int) Math.round(delay);
	}

	public RepeatFrequencyDialog(Context context) {
		super(context);
		setTitle(R.string.mrepeat_frequency);
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
		int frequency = progressToFrequency(progress);
		TeclaApp.persistence.setRepeatFrequency(frequency);
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
