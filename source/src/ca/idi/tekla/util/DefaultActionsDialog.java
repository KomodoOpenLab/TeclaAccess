package ca.idi.tekla.util;

import ca.idi.tekla.R;
import ca.idi.tekla.TeclaPrefs;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class DefaultActionsDialog extends Dialog 
	implements DialogInterface.OnKeyListener,
	Button.OnClickListener {
	
	private Button mButtonYes;
	private Button mButtonNo;
	
	public DefaultActionsDialog(Context context) {
		super(context);
		setTitle(R.string.switch_default);
		setOnKeyListener(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mButtonYes = (Button) findViewById(R.id.default_dlg_btn_yes);
		mButtonNo = (Button) findViewById(R.id.default_dlg_btn_no);

		mButtonYes.setOnClickListener(this);
		mButtonNo.setOnClickListener(this);
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

	public void onClick(View v) {
		if (v.equals(mButtonYes)) {
			TeclaPrefs.setDefaultSwitchActions();
		}
		dismiss();
	}
	

}
