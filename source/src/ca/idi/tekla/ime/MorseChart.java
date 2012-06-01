package ca.idi.tekla.ime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

public class MorseChart {
	

	public LinearLayout ll;
	private TableLayout tl1;
	private TableLayout tl2;
	private TableLayout tl3;
	private TableLayout tl4;
	private TableLayout tl5;
	private TableLayout tl6;
	private int tableSelector = 0;

	public MorseChart(Context context, TeclaMorse mTeclaMorse) {
		
		TableRow.LayoutParams trParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
		TableLayout.LayoutParams tlParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//trParams.setMargins(0, 0, 10, 10);
		llParams.setMargins(0, 0, 20, 0);
		
		ll = new LinearLayout(context);
		ll.setLayoutParams(llParams);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		
		tl1 = new TableLayout(context);
        tl1.setLayoutParams(tlParams);
        
		tl2 = new TableLayout(context);
        tl2.setLayoutParams(tlParams);
        
		tl3= new TableLayout(context);
        tl3.setLayoutParams(tlParams);		
        
        tl4 = new TableLayout(context);
        tl4.setLayoutParams(tlParams);
        
		tl5 = new TableLayout(context);
        tl5.setLayoutParams(tlParams);
        
		tl6= new TableLayout(context);
        tl6.setLayoutParams(tlParams);
        
        ll.addView(tl1, llParams);
        ll.addView(tl2, llParams);
        ll.addView(tl3, llParams);
        ll.addView(tl4, llParams);
        ll.addView(tl5, llParams);
        ll.addView(tl6, llParams);
        
        
        String s = mTeclaMorse.getCurrentChar();
        Log.d(TeclaApp.TAG, "CurrentChar: " + s);
        
        if (!s.equals("")) {
        	//Populate the HUD according to the 1st typed Morse character
        	LinkedHashMap<String,String> map = mTeclaMorse.getMorseDictionary().mMorseChart;
        	//LinkedHashMap<String,String> map = mTeclaMorse.getMorseDictionary().startsWith(s.charAt(0));
        	Log.d(TeclaApp.TAG, "Map: " + map.toString());
        	Iterator<Entry<String,String>> it = map.entrySet().iterator();
        	
        	int i = 0;
        	while (it.hasNext()) {
    			Entry<String,String> entry = it.next();
    			
        		TableRow tr = new TableRow(context);
            	tr.setId(100+i);
            	tr.setLayoutParams(trParams);
            	tr.setBaselineAligned(true);
            	
            	TextView charTV = new TextView(context);
            	charTV.setId(200+i);
            	charTV.setText(entry.getValue());
            	charTV.setTextColor(0xFF5887ED);
            	//charTV.setTextColor(Color.WHITE);
            	//charTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            	tr.addView(charTV);
            	
            	TextView morseTV = new TextView(context);
            	morseTV.setId(300+i);
            	morseTV.setText(entry.getKey());
            	morseTV.setTextColor(Color.WHITE);
            	//morseTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            	tr.addView(morseTV);
            	
            	addViewToNextTable(tr, tlParams);
            	i++;
            	tableSelector++;
        	}
        	
        }
        
	}

	private void addViewToNextTable(View v, TableLayout.LayoutParams tlParams) {
		switch (tableSelector % 6) {
		
		case 0:
			tl1.addView(v, tlParams);
			break;
		case 1:
			tl2.addView(v, tlParams);
			break;
		case 2:
			tl3.addView(v, tlParams);
			break;	
		case 3:
			tl4.addView(v, tlParams);
			break;
		case 4:
			tl5.addView(v, tlParams);
			break;
		case 5:
			tl6.addView(v, tlParams);
			break;
		default:
			break;
		}
	}
	

	


	
}
