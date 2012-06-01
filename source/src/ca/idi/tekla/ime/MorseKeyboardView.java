package ca.idi.tekla.ime;


import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.inputmethodservice.KeyboardView;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;


public class MorseKeyboardView extends KeyboardView {

	private TeclaMorse mTeclaMorse;
	private Resources mResources;

	public static final int KBD_NONE = 0;
	public static final int KBD_DITDAH = 1;
	
	private int scroll = 0;

	public void setResources(Resources res) {
		mResources = res;
	}
	
	public void setTeclaMorse(TeclaMorse tm) {
		mTeclaMorse = tm;
	}

	public MorseKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPreviewEnabled(false);
	}

	public MorseKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setPreviewEnabled(false);
	}
	
	@Override
	public MorseKeyboard getKeyboard() {
		return (MorseKeyboard) super.getKeyboard();
	}
	
	
	@Override
	public void onDraw(Canvas canvas) {
		Log.d(TeclaApp.TAG, "ONDRAW METHOD");
		
        MorseChart mc = new MorseChart(getContext(), mTeclaMorse);
        mc.tl.measure(canvas.getWidth(), canvas.getHeight());
        mc.tl.layout(0, 0, canvas.getWidth(), canvas.getHeight());
        
        //super.onDraw(canvas); 
        mc.tl.draw(canvas);
        
	}
	
	
}