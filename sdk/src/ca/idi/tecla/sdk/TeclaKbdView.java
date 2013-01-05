package ca.idi.tecla.sdk;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class TeclaKbdView extends KeyboardView {

	public TeclaKbdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TeclaKbdView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		//TODO Let running TeclaService or TeclaApp know that the view has been created 
	}

}
