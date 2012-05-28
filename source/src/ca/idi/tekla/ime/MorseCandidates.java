package ca.idi.tekla.ime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

public class MorseCandidates extends View {
	
	//public Canvas mCanvas;
	public ShapeDrawable mDrawable;
	
	public MorseCandidates(Context context){
		super(context);
		//mCanvas = new Canvas();
		int x = 20;
		int y = 20;
		int w = 300;
		int h = 100;
		
		mDrawable = new ShapeDrawable(new OvalShape());
		mDrawable.getPaint().setColor(0xff74Ac23);
		mDrawable.setBounds(x, y, x + w, y + h);
	}

	@Override
	public void draw(Canvas canvas) {
		//mCanvas.draw();
		mDrawable.draw(canvas);

	}

}
