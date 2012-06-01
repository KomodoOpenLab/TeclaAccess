package ca.idi.tekla.ime;

import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

public class MorseChart {
	

	public TableLayout tl;

	public MorseChart(Context context, TeclaMorse mTeclaMorse) {
		
		TableRow.LayoutParams trParams = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		TableLayout.LayoutParams tlParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
		//trParams.setMargins(0, 0, 10, 10);
		
		tl = new TableLayout(context);
        tl.setLayoutParams(tlParams);
        
        String s = mTeclaMorse.getCurrentChar();
        //if (s.startsWith("-")) {
        	
        	
        //}
		
        for (int i = 0; i < 10; i++) {
        	
        	TableRow tr = new TableRow(context);
        	tr.setId(100+i);
        	tr.setLayoutParams(trParams);
        	tr.setBaselineAligned(true);
        	
        	TextView charTV = new TextView(context);
        	charTV.setId(200+i);
        	charTV.setText("Hello " + i);
        	charTV.setTextColor(Color.RED);
        	//charTV.setWidth(280);
        	//charTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        	tr.addView(charTV);
        	
        	TextView morseTV = new TextView(context);
        	morseTV.setId(300+i);
        	morseTV.setText("world " + i);
        	morseTV.setTextColor(Color.CYAN);
        	//morseTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        	tr.addView(morseTV);
        	
        	tl.addView(tr, tlParams);
        }
        
	}
	


	
}
