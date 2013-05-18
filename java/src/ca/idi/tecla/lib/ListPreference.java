package ca.idi.tecla.lib;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

public class ListPreference extends android.preference.ListPreference{

	public ListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListPreference(Context context) {
		super(context);
	}
	
	protected void showDialog(Bundle b){
		super.showDialog(b);
		Dialog dialog = getDialog();
		if(dialog != null)
			InputAccess.showBelowIME(dialog);
	}
}
