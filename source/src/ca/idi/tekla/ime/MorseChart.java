package ca.idi.tekla.ime;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import ca.idi.tekla.TeclaApp;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

public class MorseChart {
	

	public LinearLayout ll;
	private TableLayout[] tls = new TableLayout[6];
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
		
		for (int i = 0; i < tls.length; i++) {
			tls[i] = new TableLayout(context);
	        tls[i].setLayoutParams(tlParams);
	        ll.addView(tls[i], llParams);
		}
        
        
        String s = mTeclaMorse.getCurrentChar();
        Log.d(TeclaApp.TAG, "CurrentChar: " + s);
        
        if (!s.equals("")) {
        	//Populate the HUD according to the 1st typed Morse character
        	LinkedHashMap<String,String> map = mTeclaMorse.getMorseDictionary().mMorseChart;
        	//LinkedHashMap<String,String> map = mTeclaMorse.getMorseDictionary().startsWith(s.charAt(0));
        	Log.d(TeclaApp.TAG, "Map: " + map.toString());
        	Iterator<Entry<String,String>> it = map.entrySet().iterator();
        	
        	int j = 0;
        	while (it.hasNext()) {
    			Entry<String,String> entry = it.next();
    			
        		TableRow tr = new TableRow(context);
            	tr.setId(100+j);
            	tr.setLayoutParams(trParams);
            	tr.setBaselineAligned(true);
            	
            	TextView charTV = new TextView(context);
            	charTV.setId(200+j);
            	charTV.setText(entry.getValue());
            	charTV.setTextColor(0xFF5887ED);
            	//charTV.setTextColor(Color.WHITE);
            	//charTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            	tr.addView(charTV);
            	
            	TextView morseTV = new TextView(context);
            	morseTV.setId(300+j);
            	morseTV.setText(entry.getKey());
            	morseTV.setTextColor(Color.WHITE);
            	//morseTV.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            	tr.addView(morseTV);
            	
            	addViewToNextTable(tr, tlParams);
            	j++;
            	tableSelector++;
        	}
        	
        }
        
	}

	private void addViewToNextTable(View v, TableLayout.LayoutParams tlParams) {
		int index = tableSelector % 6;
		tls[index].addView(v, tlParams);
	}
	

	


	
}
