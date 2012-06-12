package ca.idi.tekla.ime;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MorseChart {
	
	private Context mContext;
	private TeclaMorse mTeclaMorse;
	
	private int NB_COLUMNS = 5;
	private int index;
	private boolean updated = false;
	
	public LinearLayout layout;
	private LinearLayout ll_save;
	private TableLayout[] mTableLayout;
	
	private TableRow.LayoutParams trParams = new TableRow.LayoutParams(
			TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
	
	private TableLayout.LayoutParams tlParams = new TableLayout.LayoutParams(
			TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
	
	private LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

	
	public MorseChart(Context context, TeclaMorse teclaMorse) {
		mContext = context;
		mTeclaMorse = teclaMorse;
		
		llParams.setMargins(0, 0, 25, 0);
		
		layout = new LinearLayout(mContext);
		layout.setLayoutParams(llParams);
		layout.setOrientation(LinearLayout.HORIZONTAL);
	}
	
	public void setViews() {
		mTableLayout = new TableLayout[NB_COLUMNS];
		for (int i = 0; i < NB_COLUMNS; i++) {
			mTableLayout[i] = new TableLayout(mContext);
			mTableLayout[i].setLayoutParams(tlParams);
			layout.addView(mTableLayout[i], llParams);
			index = 0;
		}
	}
	
	public void update() {
        String s = mTeclaMorse.getCurrentChar();
        if ((s.equals("•") || s.equals("-")) && !updated) {
        	//Populate the HUD according to the 1st typed Morse character
        	LinkedHashMap<String,String> map = mTeclaMorse.getMorseDictionary().startsWith(s.charAt(0));
        	fillHUD(map);
        	updated = true;
        }
        else if (s.equals("")) {
        	layout.removeAllViews();
        	setViews();
        	updated = false;
        }
	}
	
	
	public void hide() {
		ll_save = new LinearLayout(mContext);
		ll_save.setLayoutParams(llParams);
		
		//Retrieve the child views
		TableLayout temp[] = new TableLayout[NB_COLUMNS];
		for (int i = 0; i < NB_COLUMNS; i++)
			temp[i] = (TableLayout) layout.getChildAt(i);
		
		layout.removeAllViews();
		
		//Save the views
		for (int i = 0; i < NB_COLUMNS; i++)
			ll_save.addView(temp[i], llParams);
	}
	
	public void restore() {
		if (ll_save != null)
			layout = ll_save;
	}
	
	public void fillHUD(LinkedHashMap<String,String> chart) {
		Iterator<Entry<String,String>> it = chart.entrySet().iterator();
		
    	while (it.hasNext()) {
			Entry<String,String> entry = it.next();
			
    		TableRow tr = new TableRow(mContext);
        	tr.setId(100 + index);
        	tr.setLayoutParams(trParams);
        	tr.setBaselineAligned(true);
        	
        	TextView charTV = new TextView(mContext);
        	charTV.setId(200 + index);
        	
        	if (entry.getValue().equals("\\n"))
        		charTV.setText(entry.getValue());
        	else
        		charTV.setText(entry.getValue().toUpperCase());
        	
        	charTV.setTextSize(16.0f);
        	charTV.setTextColor(0xFF77A8D4);
        	tr.addView(charTV);
        	
        	TextView morseTV = new TextView(mContext);
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
		mTableLayout[index % NB_COLUMNS].addView(v, tlParams);
	}
	
	public void configChanged(Configuration conf) {
		if (conf.orientation == conf.ORIENTATION_LANDSCAPE)
			NB_COLUMNS = 8;
		else
			NB_COLUMNS = 5;
		
		layout.removeAllViews();
    	setViews();
    	updated = false;
	}
	
}
