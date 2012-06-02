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

public class MorseChart {
	
	private int NB_COLUMNS = 5;
	private int index = 0;
	public LinearLayout ll;
	private TableLayout[] tls = new TableLayout[NB_COLUMNS];
	private TableRow.LayoutParams trParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
	private TableLayout.LayoutParams tlParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

	public MorseChart(Context context, TeclaMorse mTeclaMorse) {
		
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
        if (!s.equals("")) {
        	Log.d(TeclaApp.TAG, "Filling HUD");
        	//Populate the HUD according to the 1st typed Morse character
        	LinkedHashMap<String,String> map = mTeclaMorse.getMorseDictionary().startsWith(s.charAt(0));
        	fillHUD(context, map);
        }
        else
        	Log.d(TeclaApp.TAG, "Empty field");
        
	}
	
	public void fillHUD(Context context, LinkedHashMap<String,String> chart) {
		Iterator<Entry<String,String>> it = chart.entrySet().iterator();
		
    	while (it.hasNext()) {
			Entry<String,String> entry = it.next();
			
    		TableRow tr = new TableRow(context);
        	tr.setId(100 + index);
        	tr.setLayoutParams(trParams);
        	tr.setBaselineAligned(true);
        	
        	TextView charTV = new TextView(context);
        	charTV.setId(200 + index);
        	charTV.setText(entry.getValue());
        	charTV.setTextSize(16.0f);
        	charTV.setTextColor(0xFF5887ED);
        	tr.addView(charTV);
        	
        	TextView morseTV = new TextView(context);
        	morseTV.setId(300 + index);
        	morseTV.setText(entry.getKey());
        	morseTV.setTextSize(16.0f);
        	morseTV.setTextColor(Color.WHITE);
        	tr.addView(morseTV);
        	
        	addViewToNextTable(tr, tlParams);
        	index++;
    	}
		
	}
	
	

	private void addViewToNextTable(View v, TableLayout.LayoutParams tlParams) {
		tls[index % NB_COLUMNS].addView(v, tlParams);
	}
	

	


	
}
