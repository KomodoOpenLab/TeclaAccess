package ca.idi.tekla.ime;



import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class MorseKeyboardView extends KeyboardView {

	private TeclaMorse mTeclaMorse;
	private MorseChart mMorseChart;
	private TeclaIME mIME;
	private boolean mUpdated = true;
	
	private Dialog cheatsheetDialog;
	private View cheatsheet1;
    private View cheatsheet2;
	

	public MorseKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPreviewEnabled(false);
	}

	public MorseKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setPreviewEnabled(false);
	}
	
	public void setTeclaMorse(TeclaMorse tm) {
		mTeclaMorse = tm;
	}
	
	public void setService(TeclaIME service) {
		mIME = service;
	}
	
	@Override
	public MorseKeyboard getKeyboard() {
		return (MorseKeyboard) super.getKeyboard();
	}
	
	public void createCheatSheet() {
		if (this.cheatsheet1 == null) {
			this.cheatsheet1 = mIME.getLayoutInflater().inflate(R.layout.cheat_sheet1, null);
		}
		if (this.cheatsheet2 == null) {
			this.cheatsheet2 = mIME.getLayoutInflater().inflate(R.layout.cheat_sheet2, null);
		}
		if (this.cheatsheetDialog == null) {
			this.cheatsheetDialog = new Dialog(mIME);

			cheatsheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

			cheatsheetDialog.setCancelable(true);
			cheatsheetDialog.setCanceledOnTouchOutside(true);
			cheatsheetDialog.setContentView(cheatsheet1);
			cheatsheet1.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					FixedSizeView fsv = (FixedSizeView) cheatsheet2;
					fsv.fixedHeight = cheatsheet1.getMeasuredHeight();
					fsv.fixedWidth = cheatsheet1.getMeasuredWidth();
					cheatsheetDialog.setContentView(cheatsheet2);
					return true;
				}
			});
			cheatsheet2.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					cheatsheetDialog.setContentView(cheatsheet1);
					return true;
				}
			});
			Window window = this.cheatsheetDialog.getWindow();
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.token = this.getWindowToken();
			lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
			window.setAttributes(lp);
			window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		}
	}
	
	public void showCheatSheet() {
		createCheatSheet();
		this.cheatsheetDialog.show();
	}
	
	public void closeCheatSheet() {
        if (cheatsheetDialog != null) {
                cheatsheetDialog.dismiss();
        }
	}
	
	@Override
	protected boolean onLongPress(Keyboard.Key key) {
        if (key.codes[0] == 62) {
            showCheatSheet();
            return true;
        }
        else {
            return super.onLongPress(key);
        }
	}
	
	
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas); 

		mMorseChart = new MorseChart(getContext(), mTeclaMorse);
		mMorseChart.ll.measure(canvas.getWidth(), canvas.getHeight());
		mMorseChart.ll.layout(0, 0, canvas.getWidth(), canvas.getHeight());
		mMorseChart.ll.draw(canvas);			

		mUpdated = false;
        
	}
	
	
}